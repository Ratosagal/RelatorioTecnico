# Projeto Receptor Temporal — v0.1

Estudo exploratório de um protocolo para investigar a hipótese de receber informação antes de seu envio. **Não existe neste projeto qualquer canal retrocausal demonstrado.** A opção de simulação contrafactual impõe um atraso negativo por programação; NÃO é evidência de viagem no tempo.

## Execução (Python 3.10+, sem dependências)

```bash
python -m unittest discover -s tests -v
python -m temporal_lab simulate --days 7 --shift-hours 24
python -m temporal_lab simulate --days 7 --shift-hours 24 --hypothesis --output data/counterfactual.jsonl
```

Teste local com dois terminais:
- Terminal A: `python -m temporal_lab listen`
- Terminal B: `python -m temporal_lab send`
- Verificação: `python -m temporal_lab verify data/received.jsonl` e `python -m temporal_lab verify data/sent.jsonl`

Transmissor gera token aleatório novo de 128 bits somente quando executado e envia por UDP LOCAL (127.0.0.1). Receptor registra hora da chegada em UTC. Cabeçalhos enviados podem ser falsificados; relógios podem falhar; hashes sem ancoragem externa não provam data anterior.

Estrutura: `temporal_lab/` (código), `tests/` (4 testes), `docs/protocolo.md` (protocolo de investigação), `AGENTS.md` (orientações para Codex).

## Contexto

Ideia original discutida em 14 de outubro de 2026: deixar receptor contínuo enquanto um transmissor realiza envios diários de novas mensagens. Repetir tentativas não cria mecanismo de retrocausalidade. Primeiro passo é construir testes falsificáveis e eliminar erros e vazamentos.

## Próximo desenvolvimento

Analisador pareado de eventos enviados/recebidos; isolamento entre máquinas; assinatura independente e ancoragem externa dos registros; planejamento estatístico pré-registrado. Acesso de rádio ou computação quântica não implica comunicação com o passado.
