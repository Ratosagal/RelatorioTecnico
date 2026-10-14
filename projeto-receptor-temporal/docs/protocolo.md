# Protocolo inicial — v0.1

**Pergunta:** há evidências reproduzíveis de informação recebida antes de sua criação, sob controle de relógio e vazamento?

H0: não há canal retrocausal demonstrado. Recepção com timestamp anterior pode resultar de simulação, erro de relógio, fraude, spoofing, vazamentos ou falsos positivos.

H1: existe anomalia temporal reproduzível após exclusão das causas conhecidas. Mesmo uma anomalia autêntica não prova automaticamente viagem no tempo.

## Simulações
- `python -m temporal_lab simulate --days 14 --shift-hours 24`: simula apenas recepção posterior.
- `python -m temporal_lab simulate --days 14 --shift-hours 24 --hypothesis`: injeta recepção anterior POR DEFINIÇÃO NO CÓDIGO; não envolve mecanismo físico.

## Teste de bancada local
1. Iniciar `python -m temporal_lab listen`.
2. Em outro terminal, executar `python -m temporal_lab send`.
3. Verificar registros: `python -m temporal_lab verify data/received.jsonl` e `python -m temporal_lab verify data/sent.jsonl`.
4. UDP local **não** transmite dados ao passado; valida somente envio, recepção e logging.

## Protocolo de pesquisa futura
- Pré-registrar método, frequência, tamanho de tokens aleatórios, janela temporal, critérios estatísticos e controles antes das coletas.
- Gerar tokens de forma independente no instante do envio, jamais disponibilizados previamente ao receptor.
- Separar sistemas e conferir relógios independentes e seus intervalos de incerteza.
- Ancorar hashes e assinaturas externamente. Cadeia hash local pode ser reescrita integralmente.
- Usar controles cegos, períodos de silêncio, calibração, auditoria independente e replicação.
- Correspondência de um token anterior à sua geração exige excluir vazamento, relógio falso, coincidência e interferência.

**Limite:** não existe mecanismo experimental estabelecido para comunicação retrocausal. Repetir envio diário não altera as equações de propagação eletromagnética.
