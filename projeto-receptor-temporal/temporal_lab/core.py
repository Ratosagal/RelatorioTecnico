"""Modelos e registros auditáveis; não implementa retrocausalidade física."""
from __future__ import annotations
import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


def iso(dt: datetime) -> str:
    if dt.tzinfo is None:
        raise ValueError("O horário precisa incluir fuso horário")
    return dt.astimezone(timezone.utc).isoformat(timespec="microseconds")


def parse_iso(value: str) -> datetime:
    dt = datetime.fromisoformat(value.replace("Z", "+00:00"))
    if dt.tzinfo is None:
        raise ValueError("Horário sem timezone não aceito")
    return dt.astimezone(timezone.utc)


def canonical_json(obj: dict[str, Any]) -> str:
    return json.dumps(obj, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def audit_append(path: Path, event: dict[str, Any]) -> dict[str, Any]:
    """Adiciona evento à cadeia hash. Não substitui carimbo externo confiável."""
    path.parent.mkdir(parents=True, exist_ok=True)
    previous = "0" * 64
    count = 0
    if path.exists():
        valid, reason, count, last = audit_verify(path)
        if not valid:
            raise ValueError(f"Registro inválido: {reason}")
        previous = last
    record = {"seq": count + 1, "prev_sha256": previous, "event": event}
    record["sha256"] = hashlib.sha256(canonical_json(record).encode("utf-8")).hexdigest()
    with path.open("a", encoding="utf-8") as f:
        f.write(canonical_json(record) + "\n")
        f.flush()
    return record


def audit_verify(path: Path) -> tuple[bool, str, int, str]:
    previous = "0" * 64
    count = 0
    if not path.exists():
        return True, "arquivo ainda inexistente", 0, previous
    with path.open(encoding="utf-8") as f:
        for line in f:
            count += 1
            try:
                obj = json.loads(line)
                supplied = obj.pop("sha256")
                actual = hashlib.sha256(canonical_json(obj).encode("utf-8")).hexdigest()
                if obj["seq"] != count or obj["prev_sha256"] != previous or supplied != actual:
                    return False, f"falha na linha {count}", count, previous
                previous = supplied
            except (ValueError, TypeError, KeyError):
                return False, f"linha {count} inválida", count, previous
    return True, "cadeia válida", count, previous


def simulate(days: int = 7, shift_hours: float = 0.1, hypothesis: bool = False) -> list[dict[str, Any]]:
    """O cenário contrafactual é IMPOSTO pelo modelo; não é uma medição real."""
    if not 1 <= days <= 366:
        raise ValueError("days deve estar entre 1 e 366")
    if shift_hours <= 0:
        raise ValueError("shift_hours deve ser positivo")
    from secrets import token_hex
    first = datetime(2026, 10, 14, 12, 0, tzinfo=timezone.utc)
    output = []
    for day in range(days):
        sent = first + timedelta(days=day)
        received = sent + timedelta(hours=-shift_hours if hypothesis else shift_hours)
        output.append({
            "mode": "contrafactual_injetado" if hypothesis else "causal_convencional",
            "message_id": token_hex(16),
            "sent_at_utc": iso(sent),
            "received_at_utc": iso(received),
            "received_before_sent": received < sent,
            "evidence_of_time_travel": False,
        })
    return output


def classify(received_at: str, sent_at: str) -> str:
    """Sinalização preliminar; horário do transmissor não é prova independente."""
    return "anomalia_de_horario_a_investigar" if parse_iso(received_at) < parse_iso(sent_at) else "ordem_causal_normal"
