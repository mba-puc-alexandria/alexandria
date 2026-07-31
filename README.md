# Alexandria — Biblioteca Digital

> Plataforma web para curadoria, gerenciamento e leitura de acervos literários pessoais.

Projeto integrador desenvolvido para o **MBA em Engenharia de Software da PUC-SP**.

---

## Visão Geral

Alexandria é uma biblioteca digital que permite ao usuário organizar sua coleção de livros, acompanhar o progresso de leitura, gerenciar empréstimos e descobrir novas obras por meio de um motor de busca integrado com o **Projeto Gutenberg (Gutendex)**. O projeto é totalmente responsivo, disponível tanto em desktop quanto em dispositivos móveis, com suporte a **PWA** e **modo escuro**.

---

## Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| **Explorar e Adicionar** | Motor de descoberta com busca por título, autor ou assunto, filtro por idioma e integração com Gutendex |
| **Minha Biblioteca** | Acervo pessoal com filtros por status de leitura (Lendo, Concluído, Para Ler) |
| **Dashboard** | Visão geral com estatísticas (livros lidos, lendo, para ler), tempo estimado de leitura e progresso |
| **Controle de Empréstimos** | Registro de livros emprestados com status visual (ativo, atrasado, devolvido) |
| **Leitor de EPUB/PDF** | Visualizador integrado com trackeamento automático de progresso, tempo restante estimado e páginas restantes |
| **Autenticação** | Login/registro com JWT e autenticação via Google OAuth |
| **Perfil e Configurações** | Edição de dados da conta, alteração de senha, preferências |
| **Modo Escuro** | Tema claro/escuro com persistência local |
| **PWA** | Service worker e manifest para instalação como aplicativo |

---

## Stack Tecnológica

### Backend

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 LTS | Linguagem |
| Spring Boot | 3.4.4 | Framework principal |
| Spring Data JPA | — | Acesso a dados |
| Spring Security | — | Autenticação e autorização |
| Spring Web MVC | — | API REST |
| Spring Boot Actuator | — | Monitoramento e health check |
| SpringDoc OpenAPI | 2.7.0 | Documentação Swagger |
| MySQL | 8.0.32 | Banco de dados relacional |
| Flyway | — | Migrations de banco |
| JWT (jjwt) | 0.12.6 | Tokens de autenticação |
| BCrypt | — | Hash de senhas |
| JaCoCo | 0.8.12 | Cobertura de testes |
| Testcontainers | 1.19.0 | Testes de integração |
| Maven | 3.9+ | Build e gerenciamento de dependências |

### Frontend

| Tecnologia | Versão | Uso |
|---|---|---|
| Next.js | 16.2.4 | Framework React com App Router |
| React | 19.2.4 | Biblioteca de interface |
| TypeScript | 5 | Tipagem estática |
| Tailwind CSS | 4.2 | Estilização utilitária |
| Lucide React | 1.9.0 | Biblioteca de ícones |
| @react-oauth/google | 0.13.5 | Autenticação Google |
| react-reader | 2.0.15 | Leitor de EPUB |
| Playwright | 1.60.0 | Testes e2e |

### Infraestrutura e DevOps

| Tecnologia | Uso |
|---|---|
| Docker / Docker Compose | Containerização da aplicação |
| GitHub Actions | CI/CD |
| AWS EC2 | Servidor de produção |
| AWS RDS (MySQL) | Banco de dados em produção |
| SonarCloud | Análise estática de código |

---

## Arquitetura

### Backend — Arquitetura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────┐
│      REST Controllers       │  ← adapter/in/rest
├─────────────────────────────┤
│        Use Cases            │  ← application/
├─────────────────────────────┤
│         Domain              │  ← domain/ (entidades, regras de negócio)
├─────────────────────────────┤
│   Repository Implementations│  ← adapter/out/persistence
├─────────────────────────────┤
│   External APIs (Gutendex)  │  ← adapter/out/persistence/external
└─────────────────────────────┘
```

### Estrutura de Pastas

```
/
├── alexandria-backend/           # Backend Java/Spring
│   ├── src/main/java/.../
│   │   ├── adapter/
│   │   │   ├── in/              # Controllers REST
│   │   │   │   ├── rest/        # BookController, AuthController, ProfileController, etc.
│   │   │   │   └── job/         # SyncGutendexJobService
│   │   │   └── out/persistence/ # JPA repositories, mappers, entidades
│   │   │       └── external/    # Gutendex client
│   │   ├── application/         # Use cases (CreateBookUseCase, Auth, etc.)
│   │   ├── domain/              # Entidades de domínio (Book, User, Author, UserBooks)
│   │   ├── config/              # Security, JWT, CORS, OpenAPI
│   │   └── advice/              # GlobalExceptionHandler
│   ├── src/main/resources/
│   │   ├── db/migration/        # Flyway migrations
│   │   └── application.properties
│   ├── src/test/                # Testes unitários e de integração
│   ├── Dockerfile
│   ├── pom.xml
│   └── docker-compose.yaml      # Banco local MySQL
│
├── alexandria-frontend/         # Frontend Next.js/TypeScript
│   ├── src/
│   │   ├── app/
│   │   │   ├── layout.tsx       # Root layout (fontes, providers)
│   │   │   ├── globals.css      # Tokens de design Tailwind
│   │   │   ├── page.tsx         # Redirect → /explorar
│   │   │   ├── (auth)/          # Páginas de autenticação
│   │   │   │   ├── login/page.tsx
│   │   │   │   └── registrar/page.tsx
│   │   │   ├── (main)/          # Páginas principais
│   │   │   │   ├── explorar/page.tsx          # Descoberta de livros
│   │   │   │   ├── explorar/[id]/page.tsx     # Detalhes do livro
│   │   │   │   ├── biblioteca/page.tsx        # Minha biblioteca
│   │   │   │   ├── dashboard/page.tsx         # Painel estatístico
│   │   │   │   ├── emprestimos/page.tsx       # Controle de empréstimos
│   │   │   │   ├── leitor/page.tsx            # Leitor (mock)
│   │   │   │   ├── leitor/[id]/page.tsx       # Leitor EPUB real
│   │   │   │   ├── configuracoes/page.tsx     # Perfil e preferências
│   │   │   │   └── suporte/page.tsx           # FAQ e contato
│   │   │   └── api/epub/route.ts             # Proxy de EPUB
│   │   ├── components/
│   │   │   ├── Sidebar.tsx, Header.tsx        # Desktop layout
│   │   │   ├── MobileHeader.tsx, BottomNav.tsx # Mobile layout
│   │   │   ├── BookCard.tsx
│   │   │   ├── LoginModal.tsx
│   │   │   ├── AuthModalTrigger.tsx
│   │   │   ├── GoogleProvider.tsx
│   │   │   └── ServiceWorkerRegister.tsx
│   │   ├── contexts/            # AuthContext, AuthModalContext
│   │   ├── hooks/               # useEpub
│   │   ├── lib/                 # api.ts (cliente HTTP)
│   │   └── data/                # Dados mockados
│   ├── public/
│   │   ├── manifest.json        # PWA manifest
│   │   └── sw.js                # Service worker
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml           # Stack completa (mysql + backend + frontend)
├── docker-compose.aws.yml       # Stack para EC2 (RDS)
├── docker-compose.ci.yml        # Stack para CI
├── scripts/
│   ├── deploy-ec2.sh            # Deploy para EC2
│   ├── deploy.sh.example
│   └── setup-ec2.sh
└── .github/workflows/
    ├── ci.yaml                  # CI: guardrails, backend, frontend, integração
    ├── deploy-aws.yml           # Deploy para AWS EC2
    ├── search.yaml              # Scan de libs nos repositórios
    └── busca-libs.yaml          # Scan de libs (v2)
```

---

## Banco de Dados

### Entidades principais

| Tabela | Descrição |
|---|---|
| `books` | Livros (título, autores, capa, download, idioma, assuntos, fonte) |
| `users` | Usuários (username, email, senha hash, role USER/ADMIN) |
| `user_books` | Relação usuário-livro (status: reading/done/toread, progresso %, rating) |
| `authors` | Autores (nome, ano nascimento/falecimento) |

### Migrations (Flyway)

- **V0**: Criação das tabelas iniciais (books, users, user_books)
- **V001**: Adição de campos Gutendex (gutendex_id, download_url, cover_url, languages, subjects, download_count)
- **V002**: Criação da tabela authors e book_authors

---

## API REST (Endpoints)

### Autenticação
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/auth/register` | Registrar novo usuário | Público |
| POST | `/auth/login` | Login (retorna JWT) | Público |
| POST | `/auth/google` | Login com Google OAuth | Público |

### Livros
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/books` | Listar livros (paginado, filtro por idioma) | Público |
| GET | `/books/{id}` | Detalhes do livro | Público |
| GET | `/books/search?query=` | Buscar por título | Público |
| POST | `/books` | Criar livro | ADMIN |
| PUT | `/books/{id}` | Atualizar livro | ADMIN |
| DELETE | `/books/{id}` | Remover livro | ADMIN |

### Biblioteca do Usuário
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/user-books` | Listar livros do usuário | Autenticado |
| POST | `/user-books` | Adicionar livro à biblioteca | Autenticado |
| PUT | `/user-books/{id}` | Atualizar status/progresso | Autenticado |
| DELETE | `/user-books/{id}` | Remover da biblioteca | Autenticado |

### Perfil
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/profile/me` | Dados do perfil | Autenticado |
| PUT | `/profile/me` | Atualizar perfil | Autenticado |
| PUT | `/profile/password` | Alterar senha | Autenticado |

### Jobs (Admin)
| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/api/jobs/sync-gutendex` | Sincronizar livros do Gutendex | ADMIN |

---

## CI/CD

### Pipeline de CI (`ci.yaml`)

1. **Guardrails** — Valida política de branches (feature/* → develop → main)
2. **Backend** — Build, testes unitários e de integração com Maven + JaCoCo + SonarCloud
3. **Frontend** — Lint (ESLint) e build de produção
4. **Integração** — Docker Compose com MySQL + Backend + Frontend + health check

### Pipeline de Deploy (`deploy-aws.yml`)

- Gatilho: CI concluído com sucesso nas branches `develop` ou `main`
- Etapas: Build das imagens Docker → Upload para EC2 → Deploy backend → Health check → Deploy frontend

### Estratégia de Branches

```
feature/* → develop → main
```

---

## Design

### Paleta de Cores

| Token | Valor | Uso |
|---|---|---|
| `cream` | `#fcf9f0` | Background principal |
| `cream-dark` | `#f6f3ea` | Cards e superfícies |
| `cream-border` | `#e5e2da` | Bordas e divisores |
| `cream-book` | `#ebe8df` | Placeholder de capas |
| `brown` | `#300d00` | Texto primário / CTAs |
| `brown-soft` | `#43474d` | Texto secundário |
| `slate` | `#4c6078` | Texto terciário |
| `terra` | `#954925` | Acento / destaques |
| `blue-light` | `#d1e4ff` | Destaques alternativos |

### Modo Escuro

Todas as cores são invertidas dinamicamente via variáveis CSS quando a classe `dark` é aplicada no `<html>`.

### Tipografia

| Fonte | Uso | Peso |
|---|---|---|
| **Manrope** | Textos e interface | Variable |
| **Playfair Display** | Títulos e headings | Variable |
| **Noto Serif** | Marca e destaques | 700 |

---

## Responsividade

- **>= 768px (md):** Layout com sidebar lateral + header superior
- **< 768px:** Layout com header compacto + bottom navigation fixa
- Controle via breakpoints Tailwind (`md:`) sem rotas separadas

### Telas Mapeadas

| Nome | Rota | Descrição |
|---|---|---|
| Explorar | `/explorar` | Descoberta com busca, filtros e grade de livros |
| Detalhes | `/explorar/[id]` | Informações do livro e ações |
| Biblioteca | `/biblioteca` | Grade com filtros, progresso e remoção |
| Dashboard | `/dashboard` | Estatísticas, tempo de leitura e livros em andamento |
| Empréstimos | `/emprestimos` | Registros com status visual e FAB |
| Leitor | `/leitor`, `/leitor/[id]` | Leitor de EPUB com progresso automático |
| Configurações | `/configuracoes` | Perfil, senha, notificações, aparência |
| Suporte | `/suporte` | FAQ, contato e informações |
| Login | `/login` | Autenticação |
| Registrar | `/registrar` | Criação de conta |

---

## Pré-requisitos

- **Java** 17+
- **Maven** 3.6+
- **Node.js** 18+
- **Docker** e **Docker Compose** (para banco de dados local)

---

## Como Rodar (Desenvolvimento Local)

### 1. Subir o banco MySQL

```bash
docker compose -f alexandria-backend/docker-compose.yaml up -d
```

### 2. Backend

```bash
cd alexandria-backend
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

Documentação Swagger: `http://localhost:8080/swagger-ui.html`

### 3. Frontend

```bash
cd alexandria-frontend
cp .env.local.example .env.local   # Ajuste NEXT_PUBLIC_API_URL se necessário
npm install
npm run dev
```

Acesse `http://localhost:3000` — redireciona automaticamente para `/explorar`.

### Stack Completa com Docker

```bash
docker compose up -d --build
```

---

## Testes

### Backend

```bash
cd alexandria-backend
./mvnw clean verify        # Testes unitários + integração + JaCoCo
./mvnw sonar:sonar         # Análise SonarCloud (requer token)
```

### Frontend

```bash
cd alexandria-frontend
npm run lint               # ESLint
npm run test:e2e           # Playwright (headless)
npm run test:e2e:ui        # Playwright (com interface)
```

---

## Deploy

### EC2 (manual)

```bash
./scripts/deploy-ec2.sh
```

### CI/CD Automático

O deploy é automático via GitHub Actions quando o CI passa nas branches `develop` ou `main`.

---

## Licença

Projeto acadêmico — uso educacional.

---

**MBA Engenharia de Software | PUC-SP**


## teste
secret:
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACCI4ixEOS6uro1txGWS9G40866+guajOj+3b1qwT3cpwQAAAIiOzzd0js83
dAAAAAtzc2gtZWQyNTUxOQAAACCI4ixEOS6uro1txGWS9G40866+guajOj+3b1qwT3cpwQ
AAAEAvSNsD41UZVxrQ8uQCRgB7h8alpz7Vnzbm0ZolHSw/84jiLEQ5Lq6ujW3EZZL0bjTz
rr6C5qM6P7dvWrBPdynBAAAABXRlc3Rl
-----END OPENSSH PRIVATE KEY-----
tamiris@nerdquecorre:~/3361-Curso
