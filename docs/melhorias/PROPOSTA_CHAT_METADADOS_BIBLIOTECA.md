# Proposta — Chat sobre os Metadados da Biblioteca

> **Escopo desta proposta:** um chat em linguagem natural que responde perguntas sobre os **dados estruturados já existentes no banco** do Alexandria (catálogo de livros, acervo do usuário, progresso, avaliações). Não inclui conversar sobre o **conteúdo** dos livros (texto do EPUB) — essa é uma extensão possível, discutida na seção [Semantic search / embeddings](#semantic-search--embeddings-quando-isso-entra-em-jogo).
>
> Não confundir com o [Estúdio de Criação Literária com IA](./ANALISE_CONSOLIDADA_ESTUDIO_COAUTORIA_IA.md) — aquele é um projeto à parte (coautoria de textos originais), com stack e decisão de arquitetura próprias. Esta proposta é um módulo pequeno, **dentro** do monólito Alexandria.

---

## 1. Objetivo

Permitir que o usuário faça perguntas em linguagem natural sobre o que já está no banco, em vez de navegar por telas/filtros. Exemplos:

- "Quais livros eu estou lendo agora?"
- "Quantos livros eu já terminei este ano?"
- "Me mostra os livros de Machado de Assis que eu ainda não li"
- "Qual foi o livro que eu dei nota mais alta?"
- "Tem algum livro de ficção científica no catálogo?"

Tudo isso já é **respondível hoje via SQL** — o chat é uma camada de linguagem natural em cima de consultas que a aplicação já sabe fazer.

---

## 2. Por que isso não precisa de banco vetorial

Os dados relevantes são pequenos e estruturados: três tabelas (`books`, `users`, `user_books`), texto curto por campo, sem necessidade de busca por similaridade semântica em texto longo. Perguntas sobre metadados são resolvidas por **filtros e agregações exatas** (`WHERE status = 'reading'`, `COUNT(*)`, `ORDER BY rating`), não por "encontre algo parecido com isso".

Ou seja: o problema aqui é **tradução de linguagem natural → consulta estruturada**, não busca semântica. Isso muda completamente a arquitetura necessária (ver seção 6).

---

## 3. Escopo proposto (MVP)

| Pergunta do usuário resolve via | Use Case existente |
|---|---|
| Acervo pessoal, status, progresso, notas | `ListUserBooksUseCase` |
| Busca de livro por título/autor | `SearchBookByTitleUseCase` |
| Detalhe de um livro | `GetBookUseCase` |
| Listagem geral do catálogo | `ListBooksUseCase` |

Nenhum use case novo de domínio é necessário no MVP — o chat é uma **camada de orquestração** que decide qual use case chamar e formata a resposta.

Fora de escopo no MVP:
- Alterar dados via chat (ex.: "marca esse livro como lido") — decidir depois, é uma extensão natural mas exige mais cuidado com confirmação/segurança.
- Perguntas sobre o conteúdo/enredo dos livros.
- Recomendação por similaridade ("livros parecidos com X").

---

## 4. Arquitetura proposta

Segue o padrão hexagonal já usado no projeto (ver [ARCHITECTURE.md](../arquitetura/ARCHITECTURE.md)): um novo agregado `chat`, com use cases puros e um adapter de saída para o provedor de LLM.

```
domain/chat/
├── ChatSession.java              # Aggregate Root — histórico de uma conversa
├── ChatSessionId.java
├── ChatMessage.java              # Value Object: role (user|assistant) + conteúdo
├── ChatSessionRepository.java    # Porta de saída (persistência)
└── external/
    └── ChatCompletionPort.java   # Porta de saída (chamada ao provedor de LLM)

application/chat/
├── SendChatMessageUseCase.java   # Orquestra: monta contexto, chama LLM, executa tools, salva histórico
├── ListChatSessionsUseCase.java
└── dto/
    ├── SendChatMessageInput.java
    └── ChatMessageOutput.java

adapter/in/rest/
└── ChatController.java           # POST /chat/sessions/{id}/messages

adapter/out/
├── persistence/entity/ChatSessionEntity.java, ChatMessageEntity.java
└── external/llm/
    ├── AnthropicChatClient.java  # Implementa ChatCompletionPort
    └── tools/
        ├── ListUserBooksTool.java     # wrappers "function calling" em cima dos use cases
        ├── SearchBookTool.java
        └── GetBookTool.java
```

### Fluxo de uma mensagem

```
1. Usuário autenticado envia mensagem → POST /chat/sessions/{id}/messages
2. SendChatMessageUseCase monta o histórico da sessão + a nova mensagem
3. Chama o LLM com "tools" declaradas (function calling):
   - list_user_books(status?)
   - search_book(query)
   - get_book(id)
4. Se o LLM pedir uma tool, o UseCase executa o UseCase de domínio correspondente
   (nunca SQL direto) e devolve o resultado pro LLM continuar
5. Resposta final em linguagem natural é salva no histórico e retornada
```

**Ponto importante de segurança:** as tools que tocam dados do usuário (`list_user_books`) recebem o `userId` do contexto autenticado (JWT), nunca do LLM — o modelo escolhe *que* tool chamar, mas não escolhe *de quem* são os dados. Isso evita que uma mensagem manipulada faça o chat vazar dados de outro usuário.

### Modelo de dados novo

```sql
chat_sessions
├── id            BIGINT PK
├── user_id       BIGINT NOT NULL FK → users.id
├── created_at    DATETIME

chat_messages
├── id            BIGINT PK
├── session_id    BIGINT NOT NULL FK → chat_sessions.id
├── role          VARCHAR(20)   -- 'user' | 'assistant' | 'tool'
├── content       LONGTEXT
├── created_at    DATETIME
```

Cabe nas tabelas relacionais existentes via Flyway/Hibernate, sem infraestrutura nova além do provedor de LLM.

### Endpoint (rascunho)

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/chat/sessions` | Cria nova sessão de chat |
| `POST` | `/chat/sessions/{id}/messages` | Envia mensagem, retorna resposta da IA |
| `GET` | `/chat/sessions/{id}/messages` | Histórico da sessão |

Todos autenticados (mesmo padrão JWT já usado em `/user-books`).

---

## 5. Semantic search / embeddings — quando isso entra em jogo

O MVP acima **não precisa** de embeddings porque resolve tudo com consultas exatas sobre campos estruturados. Embeddings/busca semântica só se tornam necessários quando a pergunta não pode ser respondida por um filtro — quando é preciso comparar **significado**, não valores.

### O que é, rapidamente

Um embedding é um vetor numérico que representa o "significado" de um texto. Textos com sentido parecido geram vetores próximos entre si (por distância de cosseno, por exemplo). Busca semântica = converter a pergunta do usuário em vetor e procurar, num banco vetorial, os textos com vetores mais próximos — em vez de bater `LIKE '%palavra%'` ou `WHERE campo = valor`.

### Onde isso apareceria no Alexandria, se crescer

| Caso de uso | Precisa de embeddings? | Por quê |
|---|---|---|
| "Quais livros estou lendo?" | Não | Filtro exato em `user_books.status` |
| "Livros de ficção científica" | Talvez não | Se `subjects` já tiver a categoria, é filtro exato; só precisaria de embeddings se o usuário usar termos que não batem literalmente com os `subjects` cadastrados (ex.: "algo tipo space opera") |
| "Livros parecidos com Dom Casmurro" | **Sim** | Similaridade não é um campo do banco — exige comparar o "sentido" das obras |
| "Nesse livro, o que acontece no capítulo 3?" | **Sim** | Precisa buscar o trecho relevante dentro do texto do EPUB (não existe hoje nenhuma indexação do conteúdo dos livros) |
| Recomendação por gosto do usuário | **Sim** (geralmente) | Combina embeddings de livros lidos/avaliados bem para sugerir próximos |

### Trade-off de adicionar isso agora vs. depois

- **Custo de infraestrutura:** exige um banco vetorial (ex.: pgvector, se migrasse pra Postgres, ou um serviço externo como Pinecone/Qdrant) — o MySQL atual não tem suporte nativo maduro pra isso.
- **Custo de processamento:** cada livro (ou cada capítulo, se for granular) precisa ser processado uma vez para gerar embeddings, e reprocessado se o texto mudar.
- **Precedente do projeto:** a análise do Estúdio de Criação Literária já identificou essa mesma necessidade (dados vetoriais + chat contínuo) e **decidiu não acoplar isso ao monólito Alexandria**, tratando como projeto separado. Se o chat de metadados evoluir para incluir busca sobre conteúdo de livros, vale reavaliar se entra no Alexandria ou se é outro serviço, pelo mesmo motivo: acoplar dado vetorial a um monólito relacional cria duas fontes de verdade e dependência mútua sem necessidade.

**Recomendação:** implementar o MVP de metadados sem embeddings (ele resolve a maior parte das perguntas úteis com esforço bem menor), e tratar "chat sobre conteúdo/recomendação por similaridade" como uma fase 2, avaliando nesse momento se cabe como serviço à parte — mesma linha de raciocínio já usada na decisão do Estúdio.

---

## 6. Fases sugeridas

| Fase | Entrega | Esforço |
|---|---|---|
| **1 — MVP** | Chat com function-calling sobre os 4 use cases existentes, histórico persistido, endpoint autenticado | Pequeno — reaproveita use cases prontos |
| **2 — Metadados avançados** | Tools de agregação (ex.: "quantos livros li por mês"), refinar prompts, rate limiting | Pequeno/médio |
| **3 — Conteúdo dos livros (opcional)** | Indexação de texto dos EPUBs, banco vetorial, busca semântica e recomendação por similaridade | Grande — reavaliar se fica no monólito ou vira serviço separado |

---

## 7. Riscos e pontos em aberto

- **Custo do provedor de LLM** por requisição — precisa de rate limiting por usuário/sessão.
- **Alucinação:** o modelo pode inventar dados se não usar as tools corretamente — mitigar restringindo a resposta a só usar informação vinda das tools (não "conhecimento geral" do modelo sobre livros).
- **Qual provedor de LLM** (Anthropic, OpenAI etc.) e onde ficam as credenciais — seguir o mesmo padrão de config externa já usado (AWS Parameter Store).
- Ainda não há decisão de produto sobre chat **alterar** dados (ex. marcar como lido) — sugiro manter só leitura no MVP.
