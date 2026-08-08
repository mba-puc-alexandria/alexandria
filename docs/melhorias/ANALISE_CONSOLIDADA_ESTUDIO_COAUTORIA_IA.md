# Estúdio de Criação Literária Assistida por IA

## Documento Consolidado de Visão, Viabilidade e Estratégia de Desenvolvimento

### Versão 2.0 — Projeto Independente

> Este documento substitui e consolida `ESPECIFICACAO_MODULO_COAUTORIA_IA.md`, `ANALISE_VIABILIDADE_MODULO_COAUTORIA_IA.md` e `estudio_criacao_literaria_assistida_por_ia.md`.
>
> **Decisão de direção (2026-06-21):** o Estúdio de Criação Literária deixa de ser tratado como módulo do Alexandria e passa a ser um **projeto novo e independente**. Os dois documentos originais assumiam reaproveitamento direto da stack do Alexandria (MySQL, monólito Spring Boot, infraestrutura AWS/Kubernetes existente); essa premissa foi descartada. O motivo prático: o módulo tem requisitos de dados (vetorial/embeddings) e de produto (chat contínuo, geração assíncrona) que não se encaixam bem no monólito de biblioteca existente, e acoplar os dois sistemas criaria dependência mútua sem benefício real.

---

# 1. Visão Geral

Plataforma de coautoria literária assistida por Inteligência Artificial — não um gerador de texto, mas um ambiente completo de criação onde a IA atua como:

* Coautora
* Editora
* Revisora
* Consultora criativa
* Gestora do conhecimento da obra
* Especialista em consistência narrativa

A plataforma permite que autores desenvolvam obras longas de forma estruturada, mantendo contexto, coerência e histórico durante todo o processo de escrita.

## Relação com o Alexandria

Projeto **tecnicamente independente**, com banco de dados, backend e hospedagem próprios. A única integração prevista — e opcional, não estrutural — é consumir o catálogo de obras de domínio público do Alexandria via API pública, como fonte de referência literária (estilo, estrutura, tom) para os autores. Não há compartilhamento de banco de dados, autenticação ou infraestrutura entre os dois sistemas. Essa decisão evita acoplar o roadmap do Estúdio ao roadmap do Alexandria e permite escolher a stack mais adequada ao problema (banco vetorial, streaming, custos de IA) sem restrição herdada de um sistema legado.

---

# 2. Problema de Mercado

Ferramentas de IA para escritores hoje apresentam limitações recorrentes:

## Perda de contexto
A IA esquece personagens, eventos passados, detalhes do universo e regras estabelecidas anteriormente.

## Inconsistências narrativas
Mudanças repentinas de personalidade, erros cronológicos, contradições de enredo, alterações indevidas de fatos estabelecidos.

## Falta de planejamento
A maioria das ferramentas parte direto para a escrita ("Escreva um capítulo de fantasia") sem planejamento da obra, modelagem de personagens, construção de universo ou definição de arcos narrativos.

## Ausência de gestão do conhecimento
Informações da obra ficam espalhadas em chats, documentos e anotações soltas, sem base de conhecimento estruturada.

---

# 3. Soluções Existentes no Mercado

| Ferramenta | Pontos fortes | Limitações |
|---|---|---|
| **Sudowrite** | brainstorming, geração de cenas, expansão de textos | memória limitada, gestão de universo simplificada |
| **Novelcrafter** | lorebook, personagens, contexto persistente | foco em organização > coautoria ativa, agentes limitados |
| **Campfire** | worldbuilding, personagens, cronologias | não atua como coautor inteligente, pouca automação |
| **Plottr** | timeline, planejamento narrativo | praticamente sem inteligência criativa |
| **ChatGPT / Claude** | qualidade de escrita, criatividade, revisão | sem estrutura específica para livros, memória limitada |

---

# 4. Diferencial Competitivo

A proposta une recursos hoje fragmentados entre essas ferramentas. A IA não é apenas geradora de texto — é participante permanente da construção da obra.

## Entrevista Criativa Inicial
A IA entrevista o autor antes da escrita (mensagem da obra, protagonista, conflito central, transformação esperada) e gera automaticamente a fundação conceitual da obra.

## Bíblia da Obra Automatizada
Base de conhecimento viva: personagens, locais, organizações, regras do universo, cronologia.

## IA Especializada por Função
Agente Coautor (sugere cenas/conflitos/personagens), Agente Editor (revisa gramática/estilo/clareza), Agente Crítico (analisa furos de roteiro, inconsistências, ritmo), Agente Planejador (organiza capítulos/arcos/estrutura). **Na v1, um único modelo desempenha todos os papéis via prompts diferentes** — ver Seção 9.

## Memória Persistente com RAG
Consulta a capítulos anteriores, personagens, eventos e regras do universo para reduzir esquecimentos e contradições. **Adiado para fase posterior** — ver Seção 9.

## Versionamento
Inspirado em Git: cada alteração gera versão, histórico e comparação, com possibilidade de recuperação de versões anteriores. **Versão simplificada (snapshot completo) na v1.**

---

# 5. Arquitetura — Projeto Independente

Como projeto novo, a stack é escolhida livremente, sem restrição do legado MySQL/Spring do Alexandria.

## Componentes

### Backend principal
Responsável por autenticação, usuários, projetos, capítulos, bíblia da obra.

### Serviço/módulo de IA
Responsável por prompts, orquestração de agentes, RAG e embeddings — pode nascer **dentro do mesmo backend** na v1 (sem necessidade de microsserviço desde o dia 1) e ser extraído depois, quando o volume justificar.

## Por que não monólito único com o Alexandria
- Requisitos de dados divergentes: o Estúdio precisa de busca vetorial e geração assíncrona de longa duração; o Alexandria é CRUD simples de catálogo de livros.
- Ciclos de release independentes: o Estúdio vai iterar rápido em produto (IA, prompts, UX de escrita); acoplar ao monólito do Alexandria criaria fricção de deploy e risco de regressão na biblioteca.
- Sem ganho real de reaproveitamento: como apurado na análise do código do Alexandria, não há serviço de storage de arquivo, RAG ou IA reaproveitável — o único "reaproveitamento" seria o padrão arquitetural (hexagonal), que pode ser replicado como decisão de design, não como dependência de código.

---

# 6. MVP Recomendado

## Objetivo
Validar interesse de mercado com menor investimento possível.

## Funcionalidades essenciais

- **Projetos literários:** criar, editar, organizar capítulos.
- **Entrevista criativa:** perguntas guiadas, respostas armazenadas no banco.
- **Bíblia da obra (simples):** personagens, locais, organizações, regras do universo — sem embeddings ou banco vetorial nesta fase.
- **Editor de capítulos:** criar, editar, solicitar sugestões da IA.
- **Chat contextualizado:** a IA recebe como contexto as respostas da entrevista, personagens cadastrados, bíblia da obra e capítulos recentes mais recentes — **sem RAG**, contexto montado diretamente no prompt.
- **Exportação:** PDF e DOCX.

## Não prioritário no MVP
Timeline automática, agentes especializados separados, aprendizado do perfil do autor, marketplace, colaboração multiusuário.

---

# 7. Tecnologias Recomendadas

## Backend
Java Spring Boot (reaproveita conhecimento da equipe que já mantém o Alexandria) ou NestJS, como decisão de equipe — sem restrição técnica herdada.

## Banco de dados principal
PostgreSQL — escolhido livremente por ser projeto novo; sem motivo para herdar MySQL do Alexandria, e Postgres facilita a evolução futura para `pgvector` no mesmo banco.

## Banco vetorial (fase futura)
`pgvector` (mesma instância Postgres, menor custo operacional inicial) ou Qdrant, se o volume justificar instância dedicada.

## Provedor de IA
- **Principal:** OpenAI (modelos de raciocínio e escrita).
- **Alternativa:** Anthropic Claude — bom para consistência em textos longos.

## Framework RAG (fase futura)
LlamaIndex ou LangChain.

---

# 8. Infraestrutura e Custos Estimados

Considerando desenvolvimento inicial por equipe pequena/individual, infraestrutura própria e independente do Alexandria:

| Item | Custo estimado |
|---|---|
| PostgreSQL (instância pequena) | US$ 10–30/mês |
| Banco vetorial (Qdrant Cloud, fase futura) | US$ 0–30/mês |
| Hospedagem backend (VPS simples) | US$ 10–25/mês |
| IA — 50 usuários ativos | US$ 20–100/mês |
| IA — 100 usuários ativos | US$ 50–300/mês |
| IA — 500 usuários ativos | US$ 300–1.500/mês |

O custo de IA é o maior componente financeiro e escala com o tamanho médio do contexto enviado por chamada — motivo adicional para adiar RAG/sumarização até validar uso real.

---

# 9. Estratégia de Desenvolvimento de Baixo Custo

## Princípio fundamental
Embora a visão de longo prazo inclua RAG, memória persistente sofisticada, agentes especializados e aprendizado do perfil do autor, a recomendação é **não construir nada disso na v1**. Validar o problema e o interesse do mercado com o menor investimento possível.

## Erro comum a evitar
Tentar resolver memória persistente, agentes especializados, vetorização, conhecimento complexo e múltiplos modelos de IA simultaneamente — aumenta tempo, custo, complexidade e risco de abandono antes da validação.

## O que adiar deliberadamente
- RAG (banco vetorial, embeddings, recuperação semântica)
- Agentes especializados separados (uma única IA cobre os papéis via prompt)
- Aprendizado do perfil do autor
- Timeline inteligente com detecção automática de inconsistências
- Versionamento avançado tipo Git (comparação/diff)

## Benefícios da abordagem enxuta
Lançamento em poucas semanas, arquitetura simples, custo operacional concentrado em hospedagem + banco + chamadas de IA, feedback rápido sobre as perguntas que importam: escritores usariam a plataforma? a entrevista criativa gera valor? a bíblia da obra é útil? a coautoria é percebida como diferencial?

---

# 10. Desafios Técnicos

## Consistência narrativa (maior desafio do projeto)
Requer RAG, memória persistente e recuperação contextual eficiente — mas só na fase em que o produto já tiver tração suficiente para justificar o investimento.

## Controle de custos de IA
Obras longas geram milhares de interações e contextos extensos. Será necessário resumir históricos, estruturar conhecimento e minimizar tokens enviados por chamada.

## Escalabilidade
Conforme o número de obras/usuários cresce, embeddings, buscas vetoriais e consultas contextuais aumentam — dimensionar para esse crescimento só depois que a Etapa de RAG entrar (ver roadmap).

## Geração assíncrona/streaming
Geração de capítulos por IA pode levar 10–60s — exige desenho de API com streaming (SSE/WebSocket) desde o início, mesmo no MVP, para não comprometer a UX.

## Versionamento de texto longo
Diff e histórico de texto longo em SQL relacional não é trivial — começar com snapshot completo por versão e adiar diff real.

## UX
O sistema deve parecer uma oficina literária, não apenas um chat — esse pode ser um dos maiores diferenciais competitivos frente a ChatGPT/Claude genéricos.

---

# 11. Roadmap

| Fase | Entregas | Prazo estimado |
|---|---|---|
| **1 — MVP** | projetos, capítulos, entrevista criativa, bíblia básica, chat IA | 4–8 semanas |
| **2** | personagens avançados, timeline, organizações, universo expandido | 4 semanas |
| **3** | RAG, embeddings, memória persistente | 4–6 semanas |
| **4** | agentes especializados, crítico narrativo, editor inteligente | 6 semanas |
| **5** | perfil do autor, adaptação automática ao estilo de escrita | 4 semanas |

---

# 12. Avaliação Final

## Viabilidade técnica
Alta. Nenhum componente exige pesquisa científica inédita; as tecnologias necessárias já existem e são maduras.

## Complexidade
Média-alta, principalmente pela integração entre IA, banco vetorial, memória narrativa e experiência de usuário — mas a complexidade real só aparece a partir da Fase 3 (RAG), não no MVP.

## Risco tecnológico
Baixo. O maior risco não é técnico — é validar se autores percebem valor suficiente para trocar suas ferramentas atuais (Sudowrite, Novelcrafter, ChatGPT/Claude usado de forma manual).

---

# 13. Conclusão

Existe espaço de mercado para uma plataforma que vá além da geração de texto e funcione como ambiente real de coautoria literária. O diferencial não está na IA em si, mas na combinação de memória persistente, gestão de conhecimento da obra, planejamento narrativo, agentes especializados e experiência de escrita focada em autores.

A recomendação é tratar este projeto como **iniciativa independente do Alexandria**, com stack própria (Postgres, sem dependência do MySQL/infra do Alexandria), iniciar por um MVP enxuto sem RAG, validar a aceitação dos usuários, e evoluir progressivamente conforme o roadmap da Seção 11 — integrando o Alexandria apenas como fonte opcional de referências literárias de domínio público, nunca como dependência estrutural.
