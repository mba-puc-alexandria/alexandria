# Alexandria — Biblioteca Digital

> Plataforma web para curadoria, gerenciamento e leitura de acervos literários pessoais.

---

## Visão Geral

Alexandria é uma biblioteca digital que permite ao usuário organizar sua coleção de livros, acompanhar o progresso de leitura, gerenciar empréstimos e descobrir novas obras por meio de um motor de busca e curadoria editorial. O projeto foi desenvolvido com foco em uma experiência elegante e funcional, disponível tanto em desktop quanto em dispositivos móveis.

---

## Objetivo

Oferecer uma solução digital centralizada para leitores que desejam:

- Organizar e catalogar seus livros pessoais
- Acompanhar metas e progresso de leitura
- Controlar empréstimos realizados para terceiros
- Descobrir novas obras por meio de curadorias temáticas

---

## Funcionalidades

| Funcionalidade | Descrição |
|---|---|
| Explorar e Adicionar | Motor de descoberta com busca por título, autor ou ISBN e curadorias em destaque |
| Minha Biblioteca | Acervo pessoal com filtros por status de leitura |
| Dashboard | Visão geral com metas, progresso, tempo de leitura e sugestões do curador |
| Controle de Empréstimos | Registro de livros emprestados com status (ativo, atrasado, devolvido) |
| Leitor de PDF | Visualizador integrado de livros em formato PDF |

---

## Telas

### Desktop (1280px)

| Tela | Rota | Descrição |
|---|---|---|
| Explorar e Adicionar | `/explorar` | Tela principal com hero editorial, busca avançada, curadorias bento e recomendações |
| Minha Biblioteca | `/biblioteca` | Grade de livros com filtros e indicadores de progresso |
| Dashboard | `/dashboard` | Painel com estatísticas, livros em leitura e sugestão do curador |
| Controle de Empréstimos | `/emprestimos` | Lista de empréstimos ativos e histórico com status visual |
| Leitor de PDF | `/leitor` | Visualizador de documentos com controle de páginas e marcadores |

### Mobile Web (390px)

| Tela | Descrição |
|---|---|
| Painel | Bento grid com meta pessoal, minutos de leitura e empréstimos ativos |
| Explorar | Busca compacta, curadorias assimétricas e lista de recomendados |
| Empréstimos | Cards de empréstimo com status e botão de ação flutuante |
| Biblioteca | Grade 2 colunas com progress bars, badges de status e FAB |
| Leitor de PDF | Visualizador adaptado para tela pequena |

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| [Next.js](https://nextjs.org) | 16.2.4 | Framework React com App Router |
| [React](https://react.dev) | 19 | Biblioteca de interface |
| [TypeScript](https://www.typescriptlang.org) | 5 | Tipagem estática |
| [Tailwind CSS](https://tailwindcss.com) | 4.2 | Estilização utilitária |
| [Lucide React](https://lucide.dev) | latest | Biblioteca de ícones |

**Fontes (Google Fonts):**
- `Manrope` — textos e interface
- `Playfair Display` — títulos e headings
- `Noto Serif` — marca e destaques

---

## Design

O design foi criado no **Figma** (projeto Alexandria) e implementado com fidelidade ao protótipo original utilizando o Figma MCP para extração automática de tokens de cor, tipografia e componentes.

**Paleta de cores:**

| Token | Valor | Uso |
|---|---|---|
| `cream` | `#fcf9f0` | Background principal |
| `brown` | `#300d00` | Texto primário / CTA |
| `terra` | `#954925` | Acento / destaques |
| `slate` | `#4c6078` | Texto secundário |
| `cream-border` | `#e5e2da` | Bordas e divisores |

---

## Estrutura de Pastas

```
src/
├── app/
│   ├── layout.tsx              # Root layout (fontes, metadata)
│   ├── globals.css             # Tokens de design (Tailwind @theme)
│   ├── page.tsx                # Redirect → /explorar
│   └── (main)/
│       ├── layout.tsx          # Layout compartilhado (sidebar + header)
│       ├── explorar/page.tsx
│       ├── biblioteca/page.tsx
│       ├── dashboard/page.tsx
│       ├── emprestimos/page.tsx
│       └── leitor/page.tsx
├── components/
│   ├── Sidebar.tsx             # Navegação lateral (desktop)
│   ├── Header.tsx              # Barra superior (desktop)
│   ├── MobileHeader.tsx        # Header compacto (mobile)
│   ├── BottomNav.tsx           # Navegação inferior (mobile)
│   └── BookCard.tsx            # Card de livro reutilizável
└── data/
    └── books.ts                # Dados mockados (livros e coleções)
```

---

## Como Rodar

**Pré-requisitos:** Node.js 18+

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd alexandria

# Instalar dependências
npm install

# Servidor de desenvolvimento
npm run dev

# Build de produção
npm run build
npm start
```

Acesse `http://localhost:3000` — redireciona automaticamente para `/explorar`.

---

## Responsividade

O projeto é totalmente responsivo. A estratégia utilizada:

- **>= 768px (md):** Layout com sidebar lateral + header superior
- **< 768px:** Layout com header compacto + bottom navigation fixa

Todas as páginas possuem duas versões de layout controladas via breakpoints Tailwind (`md:`), sem necessidade de rotas separadas.

---

## Licença

Projeto acadêmico — uso educacional.
