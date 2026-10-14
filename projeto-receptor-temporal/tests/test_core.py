import json
import tempfile
import unittest
from pathlib import Path
from temporal_lab.core import audit_append, audit_verify, classify, simulate


class TemporalLabTests(unittest.TestCase):
    def test_causal_model_never_predicts_past_receipt(self):
        events = simulate(days=12, shift_hours=24)
        self.assertTrue(all(not event["received_before_sent"] for event in events))
        self.assertTrue(all(not event["evidence_of_time_travel"] for event in events))

    def test_hypothesis_is_marked_as_artificial(self):
        events = simulate(days=3, shift_hours=1, hypothesis=True)
        self.assertTrue(all(e["received_before_sent"] for e in events))
        self.assertTrue(all(e["mode"] == "contrafactual_injetado" for e in events))
        self.assertTrue(all(not e["evidence_of_time_travel"] for e in events))

    def test_hash_log_detects_tampering(self):
        with tempfile.TemporaryDirectory() as d:
            p = Path(d) / "log.jsonl"
            audit_append(p, {"kind": "test", "number": 1})
            audit_append(p, {"kind": "test", "number": 2})
            self.assertTrue(audit_verify(p)[0])
            lines = p.read_text(encoding="utf-8").splitlines()
            first = json.loads(lines[0]); first["event"]["number"] = 100
            lines[0] = json.dumps(first, sort_keys=True)
            p.write_text("\n".join(lines) + "\n", encoding="utf-8")
            self.assertFalse(audit_verify(p)[0])

    def test_time_classification(self):
        self.assertEqual(classify("2026-10-14T12:00:00+00:00", "2026-10-15T12:00:00+00:00"), "anomalia_de_horario_a_investigar")
        self.assertEqual(classify("2026-10-15T12:00:00+00:00", "2026-10-14T12:00:00+00:00"), "ordem_causal_normal")


if __name__ == "__main__":
    unittest.main()
