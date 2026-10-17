# Agente de IA no atendimento — Alexandria

Assistente virtual na página **Suporte** que responde dúvidas, busca livros no acervo e consulta
a assinatura e a biblioteca do usuário logado. Implementação acadêmica baseada no caso
"Agente de IA no Atendimento" (TelcoNova), adaptada ao tamanho do Alexandria.

## Arquitetura

```
Frontend (Next.js, /suporte)
   │  POST /support/chat  { messages: [...] }   (JWT)
   ▼
SupportController ──► ChatWithSupportUseCase ──► SupportAgent (porta)
                                                    │
                                     SpringAiSupportAgent (adapter, Spring AI)
                                        │                      │
                          ferramentas (SupportTools)     modelo de linguagem
                          - buscarLivros                 - dev:  Ollama (qwen2.5:3b, local)
                          - minhaAssinatura              - prod: Amazon Bedrock (Claude Haiku 4.5)
                          - meusLivros
```

- **Camada de abstração (lock-in):** a troca de provedor é só configuração
  (`AI_PROVIDER=ollama` ou `AI_PROVIDER=bedrock-converse`). O código do fluxo não muda.
- **Menor privilégio:** o `userId` das ferramentas vem do JWT e é fixado no servidor. O modelo
  não escolhe de qual usuário lê dados — pedir "a assinatura do usuário 1" devolve só os dados
  de quem está logado.
- **Falha do modelo:** a resposta vira uma mensagem com o contato humano
  (`suporte@alexandria.pucsp.br`), equivalente à "transferência para humano" do caso.
- **Transparência (LGPD):** a tela avisa que o usuário fala com uma IA; o prompt proíbe pedir
  senha, CPF ou cartão.
- **Sem estado no servidor:** o frontend reenvia a conversa; o backend usa as últimas 10 mensagens
  (até 1000 caracteres cada).

Código: `alexandria-backend/src/main/java/com/pucsp/alexandria/`
`domain/support/`, `application/support/`, `adapter/out/ai/`, `adapter/in/rest/support/`.

## Rodando local (Ollama, custo zero)

```bash
docker compose up -d --build
```

O serviço `ollama` sobe junto e o backend baixa o modelo `qwen2.5:3b` (~2 GB) no primeiro start.
Para baixar manualmente: `docker exec alexandria-ollama ollama pull qwen2.5:3b`.
Modelo maior (melhor qualidade, mais lento sem GPU): `OLLAMA_MODEL=qwen2.5:7b`.

Rodando o backend fora do Docker: suba o Ollama e use `OLLAMA_URL=http://localhost:11434`.

## Produção na AWS (Amazon Bedrock)

Produção hoje: 1 EC2 em **us-east-2** com os containers, RDS MySQL e Cloudflare. A EC2 não tem
memória/GPU para rodar um modelo, então em produção o modelo vem do Bedrock (pago por token).

1. **Habilitar o modelo:** console do Bedrock (us-east-2) → *Model access* → Anthropic Claude Haiku 4.5.
   Anote o ID do *inference profile* (ex.: `us.anthropic.claude-haiku-4-5-20251001-v1:0`).
2. **Permissão via IAM role (sem chave no código):** criar uma role para EC2 com a política:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [{
       "Effect": "Allow",
       "Action": ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"],
       "Resource": "*"
     }]
   }
   ```
   e anexar à instância: *EC2 → Actions → Security → Modify IAM role*.
3. **Container acessar as credenciais da instância (IMDSv2):**
   ```bash
   aws ec2 modify-instance-metadata-options --instance-id <id> \
     --http-put-response-hop-limit 2 --http-endpoint enabled
   ```
4. **Subir o backend** com o perfil `rds` (já usa `bedrock-converse` por padrão):
   ```bash
   docker run -d ... -e SPRING_PROFILES_ACTIVE=rds \
     -e AWS_REGION=us-east-2 \
     -e BEDROCK_MODEL_ID=<id do passo 1> \
     alexandria-backend
   ```
5. **Controle de custo:** AWS Budgets com alerta (ex.: US$ 10/mês). As respostas têm limite
   de 1024 tokens.

### Custo estimado (Claude Haiku 4.5: US$ 1 / US$ 5 por milhão de tokens de entrada/saída)

Uma conversa de ~5 trocas usa ~15 mil tokens de entrada e ~1,5 mil de saída: **~US$ 0,02**.
500 conversas de teste ≈ US$ 11. Confira o preço no Bedrock da sua região.

## Paralelo com o caso TelcoNova

| Caso TelcoNova | Alexandria |
|---|---|
| Amazon Bedrock (LLM) | Bedrock em produção, Ollama em dev |
| Camada de abstração contra lock-in | Porta `SupportAgent` + Spring AI; troca por config |
| Lambda orquestrador | Backend Spring Boot na EC2 (`SpringAiSupportAgent`) |
| Consulta ao legado (fatura, rede) | Ferramentas: assinatura, biblioteca, busca no acervo |
| Menor privilégio / dados só do cliente | `userId` do JWT fixado nas ferramentas |
| Falha do LLM → atendente humano | Mensagem de fallback com contato humano |
| Informar que é IA (LGPD) | Aviso na tela + regra no prompt |
| IAM role, sem credencial no código | Instance profile da EC2 |
| AWS Budgets / FinOps | Alerta de orçamento + `max-tokens` |

**Fora do escopo (evolução):** Bedrock Guardrails (filtro de PII e prompt injection),
Knowledge Bases/RAG (o FAQ cabe no prompt), registro das conversas para auditoria,
limite de mensagens por usuário, respostas em streaming, Terraform.
