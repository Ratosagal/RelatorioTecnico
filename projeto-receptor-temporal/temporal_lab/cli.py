"""CLI stdlib para simulações e teste UDP local de ponta a ponta."""
from __future__ import annotations
import argparse
import json
import secrets
import socket
from pathlib import Path
from .core import audit_append, audit_verify, classify, iso, simulate, utc_now


def main() -> None:
    parser = argparse.ArgumentParser(description="Projeto Receptor Temporal — prova de conceito, SEM comunicação com o passado")
    sub = parser.add_subparsers(dest="cmd", required=True)
    model = sub.add_parser("simulate", help="Simula causalidade ou cenário contrafactual imposto")
    model.add_argument("--days", type=int, default=7)
    model.add_argument("--shift-hours", type=float, default=24.0)
    model.add_argument("--hypothesis", action="store_true")
    model.add_argument("--output", type=Path, default=Path("data/simulation.jsonl"))
    receiver = sub.add_parser("listen", help="Escuta UDP LOCAL, sem canal temporal")
    receiver.add_argument("--port", type=int, default=50123)
    receiver.add_argument("--log", type=Path, default=Path("data/received.jsonl"))
    sender = sub.add_parser("send", help="Envia pacote UDP LOCAL com código aleatório")
    sender.add_argument("--port", type=int, default=50123)
    sender.add_argument("--log", type=Path, default=Path("data/sent.jsonl"))
    verifier = sub.add_parser("verify", help="Verifica cadeia de hashes do registro")
    verifier.add_argument("path", type=Path)
    args = parser.parse_args()

    if args.cmd == "simulate":
        entries = simulate(args.days, args.shift_hours, args.hypothesis)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with args.output.open("w", encoding="utf-8") as f:
            for entry in entries:
                f.write(json.dumps(entry, ensure_ascii=False, sort_keys=True) + "\n")
        print(f"Simulação salva: {args.output} | {len(entries)} eventos | hipótese injetada: {args.hypothesis}")
    elif args.cmd == "listen":
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.bind(("127.0.0.1", args.port))
            print(f"Receptor ativo em 127.0.0.1:{args.port}. Ctrl+C encerra.", flush=True)
            try:
                while True:
                    payload, address = sock.recvfrom(4096)
                    received_at = iso(utc_now())
                    try:
                        message = json.loads(payload.decode("utf-8"))
                        if not isinstance(message, dict) or not isinstance(message.get("token"), str) or not isinstance(message.get("sent_at_utc"), str):
                            raise ValueError("campos ausentes")
                        classification = classify(received_at, message["sent_at_utc"])
                        event = {"kind": "received", "received_at_utc": received_at, "from": address[0], "packet": message, "classification": classification}
                    except (UnicodeDecodeError, ValueError, TypeError, KeyError) as exc:
                        event = {"kind": "invalid_packet", "received_at_utc": received_at, "error": type(exc).__name__}
                    audit_append(args.log, event)
                    print(json.dumps(event, ensure_ascii=False, sort_keys=True), flush=True)
            except KeyboardInterrupt:
                print("Receptor encerrado.")
    elif args.cmd == "send":
        packet = {"token": secrets.token_hex(16), "sent_at_utc": iso(utc_now()), "message": "Oi, tudo bem?"}
        encoded = json.dumps(packet, ensure_ascii=False, sort_keys=True).encode("utf-8")
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            bytes_sent = sock.sendto(encoded, ("127.0.0.1", args.port))
        audit_append(args.log, {"kind": "sent", "packet": packet, "bytes_sent": bytes_sent, "recorded_at_utc": iso(utc_now())})
        print(json.dumps(packet, ensure_ascii=False, sort_keys=True))
    elif args.cmd == "verify":
        ok, reason, count, digest = audit_verify(args.path)
        print(f"válido={ok} | motivo={reason} | eventos={count} | último_sha256={digest}")
        if not ok:
            raise SystemExit(1)


if __name__ == "__main__":
    main()
