# Instruções para trabalho no Codex

Este projeto estuda um **protocolo de medição**, não demonstra viagem no tempo.
- Nunca descrever dados simulados ou pacotes UDP locais como prova de retrocausalidade.
- Diferenciar explicitamente: simulação causal, cenário contrafactual artificial, observação física.
- Testar cada alteração com `python -m unittest discover -s tests -v`.
- Manter documentação e critérios falsificáveis.
- Rejeitar anomalia baseada apenas em timestamp de um pacote; relógios/cabeçalhos não são prova independente.
- Próxima tarefa: implementar analisador de logs e relatório CSV com testes, sem alegar transporte de dados ao passado.
- Não emitir RF real nem instruções para violar regulação.
