# Documentação do Projeto — Alexandria: Biblioteca Digital

---

## Sumário

1. [Identificação do Projeto](#1-identificação-do-projeto)
2. [Introdução](#2-introdução)
3. [Objetivos](#3-objetivos)
4. [Público-Alvo](#4-público-alvo)
5. [Requisitos Funcionais](#5-requisitos-funcionais)
6. [Requisitos Não Funcionais](#6-requisitos-não-funcionais)
7. [Descrição das Telas](#7-descrição-das-telas)
   - 7.1 [Telas Desktop](#71-telas-desktop)
   - 7.2 [Telas Mobile](#72-telas-mobile)
8. [Arquitetura e Tecnologias](#8-arquitetura-e-tecnologias)
9. [Design e Interface](#9-design-e-interface)
10. [Estrutura do Projeto](#10-estrutura-do-projeto)
11. [Como Executar o Projeto](#11-como-executar-o-projeto)
12. [Considerações Finais](#12-considerações-finais)

---

## 1. Identificação do Projeto

| Campo | Informação |
|---|---|
| **Nome do Projeto** | Alexandria |
| **Subtítulo** | Biblioteca Digital |
| **Tipo** | Aplicação Web Responsiva |
| **Categoria** | Gerenciamento de Acervo Literário Pessoal |
| **Tecnologia Principal** | Next.js + TypeScript + Tailwind CSS |
| **Plataformas** | Desktop (Web) e Mobile Web |

---

## 2. Introdução

O Alexandria é uma plataforma web desenvolvida para suprir uma necessidade comum entre leitores ávidos: a falta de um sistema centralizado, elegante e funcional para gerenciar acervos literários pessoais.

Inspirado nas grandes bibliotecas históricas — em especial na lendária Biblioteca de Alexandria, símbolo de conhecimento e curadoria intelectual — o sistema oferece ao usuário uma experiência editorial sofisticada para catalogar, explorar e acompanhar seus livros.

O projeto nasce da observação de que muitos leitores recorrem a planilhas ou aplicativos genéricos para organizar seus livros, sem acesso a recursos como curadoria temática, controle de empréstimos ou metas de leitura. O Alexandria resolve esse problema ao reunir essas funcionalidades em uma interface visualmente refinada e responsiva.

---

## 3. Objetivos

### 3.1 Objetivo Geral

Desenvolver uma aplicação web responsiva que permita ao usuário gerenciar seu acervo literário pessoal de forma centralizada, com suporte a leitura, curadoria e controle de empréstimos.

### 3.2 Objetivos Específicos

- Permitir o cadastro, organização e visualização do acervo pessoal de livros
- Oferecer um motor de busca e descoberta de novas obras por título, autor ou ISBN
- Exibir curadorias editoriais temáticas para inspirar novas leituras
- Acompanhar o progresso de leitura de cada livro com indicadores visuais
- Registrar e monitorar empréstimos realizados para terceiros
- Disponibilizar um leitor de documentos PDF integrado
- Apresentar um dashboard com métricas e metas de leitura pessoal
- Garantir experiência de uso adequada em dispositivos desktop e mobile

---

## 4. Público-Alvo

O Alexandria é voltado para:

- **Leitores frequentes** que possuem um acervo considerável e desejam organizá-lo digitalmente
- **Estudantes e pesquisadores** que precisam controlar leituras acadêmicas e referências bibliográficas
- **Clubes de leitura** e bibliotecas pessoais que realizam empréstimos informais entre membros
- **Colecionadores de livros** interessados em catalogar edições e raridades

**Perfil demográfico:** usuários com habilidade básica em navegação web, de 16 a 60 anos, com interesse em literatura e organização pessoal.

---

## 5. Requisitos Funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF01 | O sistema deve exibir uma tela de exploração com curadorias editoriais | Alta |
| RF02 | O sistema deve permitir busca de livros por título, autor ou ISBN | Alta |
| RF03 | O sistema deve exibir o acervo pessoal do usuário com filtros por status | Alta |
| RF04 | O sistema deve indicar o progresso de leitura de cada livro (percentual) | Média |
| RF05 | O sistema deve exibir um dashboard com estatísticas de leitura e metas | Média |
| RF06 | O sistema deve permitir o registro de empréstimos com nome do portador e prazo | Alta |
| RF07 | O sistema deve sinalizar visualmente empréstimos atrasados | Alta |
| RF08 | O sistema deve oferecer um leitor de PDF integrado | Média |
| RF09 | O sistema deve apresentar sugestões personalizadas de leitura | Baixa |
| RF10 | O sistema deve funcionar adequadamente em dispositivos móveis | Alta |

---

## 6. Requisitos Não Funcionais

| ID | Requisito | Categoria |
|---|---|---|
| RNF01 | A interface deve carregar em menos de 3 segundos em conexões padrão | Desempenho |
| RNF02 | O sistema deve ser responsivo para telas a partir de 390px de largura | Usabilidade |
| RNF03 | O código deve ser escrito em TypeScript com tipagem estrita | Manutenibilidade |
| RNF04 | Os componentes devem ser reutilizáveis e desacoplados | Manutenibilidade |
| RNF05 | A paleta de cores deve seguir os tokens de design definidos no Figma | Consistência |
| RNF06 | O sistema deve utilizar fontes do Google Fonts com carregamento otimizado | Desempenho |
| RNF07 | A navegação deve funcionar sem recarregamento de página (SPA) | Usabilidade |

---

## 7. Descrição das Telas

### 7.1 Telas Desktop

As telas desktop são exibidas em dispositivos com largura de tela igual ou superior a 768px. O layout é composto por uma **sidebar lateral fixa** (256px) de navegação e uma **área de conteúdo principal** com header superior.

---

#### 7.1.1 Explorar e Adicionar (`/explorar`)

**Objetivo:** Tela inicial da aplicação e principal ponto de descoberta de novos livros.

**Componentes principais:**
- **Hero editorial** com título de impacto em três linhas (`Expanda seu / Estudo / Particular`) e texto descritivo lateral
- **Campo de busca assimétrico** com input de texto amplo e botão de ação
- **Seção de curadorias em destaque** no estilo Bento Grid, com:
  - Card principal (Filosofia Clássica) ocupando 2 colunas e 2 linhas
  - Cards secundários menores com temas variados
- **Grade de recomendações** com 5 cards de livros por linha, contendo capa, avaliação, título, autor e botão de adição

**Comportamento:** A sidebar exibe "Explorar" como item ativo (estado destacado).

---

#### 7.1.2 Minha Biblioteca (`/biblioteca`)

**Objetivo:** Visualização e gerenciamento do acervo pessoal do usuário.

**Componentes principais:**
- Título da página com contador total de livros
- **Filtros horizontais** por status: Todos, Lendo, Disponíveis, Emprestados
- **Grade de livros** com 4 colunas, cada card exibindo:
  - Capa do livro
  - Título e autor
  - Indicador de status (lendo, concluído, para ler, emprestado)
  - Barra de progresso (quando aplicável)
- Botões de alternância entre visualização em grade e lista

---

#### 7.1.3 Dashboard (`/dashboard`)

**Objetivo:** Painel de controle com visão geral da atividade de leitura do usuário.

**Componentes principais:**
- **Bento grid de estatísticas:**
  - Meta pessoal anual (ex: 24/50 livros) com barra de progresso
  - Minutos totais de leitura
  - Número de empréstimos ativos
- **Seção "Lendo Agora"** com cards dos livros em andamento, incluindo:
  - Capa, gênero, título
  - Percentual concluído e páginas restantes
  - Barra de progresso visual
  - Botão "Retomar Leitura"
- **Card de sugestão do curador** em destaque escuro com recomendação personalizada baseada no histórico de leitura
- **Adições recentes** com lista dos últimos livros adicionados ao acervo

---

#### 7.1.4 Controle de Empréstimos (`/emprestimos`)

**Objetivo:** Registro e acompanhamento de livros emprestados pelo usuário para terceiros.

**Componentes principais:**
- **Cards de estatísticas rápidas:** total ativo e total atrasado
- **Lista de registros** com cards individuais por empréstimo, contendo:
  - Capa, título e autor do livro
  - Badge de status com cor semântica:
    - Vermelho: Atrasado
    - Neutro: No prazo
    - Verde: Devolvido
  - Data de vencimento e nome do portador
- **Botão de ação flutuante (FAB)** para registrar novo empréstimo

---

#### 7.1.5 Leitor de PDF (`/leitor`)

**Objetivo:** Visualização de livros em formato PDF diretamente no navegador, sem necessidade de aplicativo externo.

**Componentes principais:**
- **Header do leitor** com título, autor, controles de zoom e botão de marcador
- **Área de visualização** centralizada com o conteúdo do documento em fundo neutro
- **Barra de navegação inferior** com botões de página anterior/próxima e indicador de progresso (ex: "Página 1 de 256")

---

### 7.2 Telas Mobile

As telas mobile são exibidas em dispositivos com largura inferior a 768px. O layout substitui a sidebar por um **header compacto fixo** no topo e uma **barra de navegação inferior** com as quatro seções principais.

---

#### 7.2.1 Painel — Mobile (`/dashboard`)

**Objetivo:** Versão compacta do dashboard adaptada para leitura vertical em smartphones.

**Diferenças em relação ao desktop:**
- Bento grid em 2 colunas com cards de estatísticas empilhados verticalmente
- Meta pessoal ocupa a linha superior completa com maior destaque
- Cards "Lendo Agora" exibidos em lista vertical com capa, gênero e progresso
- Card do curador com destaque escuro e botão de adição em largura total

---

#### 7.2.2 Explorar — Mobile (`/explorar`)

**Objetivo:** Motor de busca e descoberta adaptado para uso mobile.

**Diferenças em relação ao desktop:**
- Hero editorial simplificado: apenas título "Explorar" sem o texto em três linhas
- Curadorias em grid assimétrico de 12 colunas: card grande (12 cols) + dois cards pequenos (6 cols cada)
- Lista de recomendados em cards horizontais (capa + título + autor + botão) em vez de grade
- Botão de sugestão personalizada centralizado na parte inferior

---

#### 7.2.3 Empréstimos — Mobile (`/emprestimos`)

**Objetivo:** Controle de empréstimos adaptado para telas pequenas.

**Diferenças em relação ao desktop:**
- Cards de estatísticas em grid 2×1 compacto (128px de altura)
- Cards de empréstimo com informações em layout vertical sequencial
- FAB (botão flutuante) posicionado acima da bottom navigation

---

#### 7.2.4 Biblioteca — Mobile (`/biblioteca`)

**Objetivo:** Acervo pessoal em grade compacta otimizada para toque.

**Diferenças em relação ao desktop:**
- Grade de livros em 2 colunas (em vez de 4)
- Filtros em scroll horizontal para não ocupar múltiplas linhas
- Capas com altura fixa de 239px para consistência visual
- FAB para adicionar novo livro

---

#### 7.2.5 Leitor de PDF — Mobile (`/leitor`)

**Objetivo:** Leitura de documentos em tela pequena com controles acessíveis.

**Diferenças em relação ao desktop:**
- Área de visualização em largura total sem margens laterais
- Controles de zoom acessíveis por toque
- Navegação de páginas por deslize horizontal (swipe)

---

## 8. Arquitetura e Tecnologias

### 8.1 Arquitetura Geral

O projeto utiliza a arquitetura **App Router** do Next.js 16, com renderização estática de páginas (Static Site Generation) e componentes React no modelo Server/Client Components.

```
Cliente (Browser)
    └── Next.js App Router
            ├── Server Components (páginas, layouts)
            └── Client Components (sidebar, navegação, interatividade)
```

### 8.2 Justificativa das Tecnologias

| Tecnologia | Justificativa |
|---|---|
| **Next.js 16** | Framework moderno com App Router, roteamento por pasta, layouts compartilhados e otimização de fontes integrada. Elimina configuração de React Router separado. |
| **TypeScript** | Tipagem estática reduz erros em tempo de desenvolvimento e documenta contratos de dados (ex: tipo `Book`, tipo `Collection`). |
| **Tailwind CSS v4** | Utilitários inline permitem estilizar sem arquivos CSS separados. A diretiva `@theme` do Tailwind v4 centraliza os tokens de design em um único arquivo. |
| **Lucide React** | Biblioteca de ícones consistente e leve, com tree-shaking automático — apenas os ícones utilizados são incluídos no bundle. |
| **Google Fonts (next/font)** | Carregamento otimizado de fontes via Next.js: sem layout shift, sem requisição extra ao Google em runtime. |

### 8.3 Padrões Adotados

- **Route Groups** (`(main)/`) — agrupa rotas que compartilham o mesmo layout sem afetar a URL
- **Server/Client split** — páginas são Server Components por padrão; apenas componentes com estado ou eventos de browser recebem `"use client"`
- **Dados mockados** (`src/data/books.ts`) — estrutura preparada para substituição por API REST futura
- **Tokens de design** — cores definidas em `globals.css` com `@theme`, utilizadas via classes Tailwind em todo o projeto

---

## 9. Design e Interface

### 9.1 Origem do Design

O design do Alexandria foi elaborado no **Figma** e integrado ao desenvolvimento via **Figma MCP** (Model Context Protocol), que permitiu a extração automática de:

- Tokens de cor e tipografia
- Estrutura de componentes
- Imagens e assets
- Código de referência React + Tailwind

### 9.2 Sistema de Design

**Paleta de Cores:**

| Nome | Hex | Aplicação |
|---|---|---|
| Cream | `#fcf9f0` | Background principal, sidebar |
| Cream Dark | `#f6f3ea` | Inputs, cards secundários |
| Cream Active | `#f2eee1` | Item de navegação ativo |
| Cream Border | `#e5e2da` | Bordas, divisores |
| Cream Book | `#ebe8df` | Background de capas de livros |
| Brown | `#300d00` | Texto principal, botões primários |
| Brown Soft | `#43474d` | Texto secundário escuro |
| Slate | `#4c6078` | Texto de apoio, ícones |
| Terra | `#954925` | Acento, estrelas de avaliação, destaques |
| Blue Light | `#d1e4ff` | Badges, texto sobre fundos escuros |

**Tipografia:**

| Fonte | Peso | Aplicação |
|---|---|---|
| Manrope | 300–800 | Corpo de texto, labels, botões |
| Playfair Display | 400, 700 | Títulos de seção, headings de livro |
| Noto Serif | 700 | Logotipo, título hero |

**Espaçamento e Grid:**
- Sidebar: 256px (fixa)
- Conteúdo: largura máxima de 1280px
- Padding de página: 32px (desktop), 24px (mobile)
- Gap padrão de seção: 64px (desktop), 48px (mobile)

### 9.3 Padrões de Componentes

- **Rounded corners:** `rounded` (4px) para cards de livro, `rounded-lg` (8px) para seções, `rounded-xl` (12px) para botões e inputs
- **Sombras:** `shadow-sm` para capas, sem sombra no layout base
- **Transições:** `transition-colors` em todos os elementos interativos

---

## 10. Estrutura do Projeto

```
alexandria/
├── public/                     # Assets estáticos
├── src/
│   ├── app/
│   │   ├── globals.css         # Tokens de design global (Tailwind @theme)
│   │   ├── layout.tsx          # Root layout: fontes, metadata, html/body
│   │   ├── page.tsx            # Página raiz → redirect para /explorar
│   │   └── (main)/             # Route group: rotas com sidebar/header
│   │       ├── layout.tsx      # Layout responsivo: sidebar + header + bottom nav
│   │       ├── explorar/
│   │       │   └── page.tsx    # Tela de exploração e descoberta
│   │       ├── biblioteca/
│   │       │   └── page.tsx    # Acervo pessoal
│   │       ├── dashboard/
│   │       │   └── page.tsx    # Painel de estatísticas
│   │       ├── emprestimos/
│   │       │   └── page.tsx    # Controle de empréstimos
│   │       └── leitor/
│   │           └── page.tsx    # Leitor de PDF
│   ├── components/
│   │   ├── Sidebar.tsx         # Navegação lateral (desktop, md+)
│   │   ├── Header.tsx          # Barra superior com busca (desktop, md+)
│   │   ├── MobileHeader.tsx    # Header compacto (mobile, < md)
│   │   ├── BottomNav.tsx       # Navegação inferior (mobile, < md)
│   │   └── BookCard.tsx        # Card reutilizável de livro
│   └── data/
│       └── books.ts            # Tipos e dados mockados
├── docs/
│   └── documentacao.md         # Este documento
├── README.md                   # Documentação técnica do repositório
├── package.json
├── tsconfig.json
└── next.config.ts
```

---

## 11. Como Executar o Projeto

### 11.1 Pré-requisitos

- **Node.js** versão 18 ou superior
- **npm** (instalado junto com o Node.js)
- Conexão com a internet (para carregamento das fontes Google na primeira execução)

### 11.2 Instalação

```bash
# 1. Acesse a pasta do projeto
cd alexandria

# 2. Instale as dependências
npm install
```

### 11.3 Execução em Desenvolvimento

```bash
npm run dev
```

Acesse `http://localhost:3000` no navegador. A aplicação redireciona automaticamente para a tela de Explorar (`/explorar`).

### 11.4 Build de Produção

```bash
# Gerar build otimizado
npm run build

# Iniciar servidor de produção
npm start
```

### 11.5 Verificação de Tipos

```bash
# Checar erros TypeScript
npx tsc --noEmit
```

---

## 12. Considerações Finais

O projeto Alexandria representa uma aplicação web moderna que integra boas práticas de desenvolvimento frontend com um design cuidadosamente elaborado. Entre os principais pontos de destaque:

**Pontos fortes:**
- Design fiel ao protótipo Figma, com tokens de cor e tipografia consistentes
- Arquitetura responsiva que adapta o layout sem duplicar rotas
- Código TypeScript tipado e componentes reutilizáveis
- Performance otimizada com Next.js (fontes, imagens, bundle splitting)

**Possibilidades de evolução:**
- Integração com API REST (ex: Open Library API) para busca real de livros
- Autenticação de usuário com NextAuth ou Clerk
- Banco de dados para persistência do acervo (ex: Supabase, PlanetScale)
- Upload de arquivos PDF para o leitor integrado
- Notificações de empréstimos próximos ao vencimento
- Exportação do acervo em CSV ou PDF

---

*Documentação gerada em abril de 2026.*
