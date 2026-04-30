export type Book = {
  id: string;
  title: string;
  author: string;
  rating: number;
  cover: string;
  genre?: string;
  year?: number;
  description?: string;
  pages?: number;
  status?: "available" | "borrowed" | "reading";
};

export type Collection = {
  id: string;
  name: string;
  description: string;
  bookCount: number;
  color: string;
  type: "main" | "secondary" | "dark" | "light";
};

export const books: Book[] = [
  {
    id: "1",
    title: "The Alchemist",
    author: "Paulo Coelho",
    rating: 4.9,
    cover: "https://www.figma.com/api/mcp/asset/92c33214-2a27-452d-96fa-3e7fb13aaf41",
    genre: "Ficção",
    year: 1988,
    description: "A história de Santiago, um jovem pastor andaluz que sonha com um tesouro escondido nas pirâmides do Egito.",
    pages: 208,
    status: "available",
  },
  {
    id: "2",
    title: "Meditations",
    author: "Marcus Aurelius",
    rating: 4.7,
    cover: "https://www.figma.com/api/mcp/asset/aefa1138-9bc9-4152-9cbf-2cf10a7e41d6",
    genre: "Filosofia",
    year: 180,
    description: "Reflexões pessoais do imperador romano Marco Aurélio sobre filosofia estoica.",
    pages: 256,
    status: "reading",
  },
  {
    id: "3",
    title: "Beyond Good & Evil",
    author: "Friedrich Nietzsche",
    rating: 5.0,
    cover: "https://www.figma.com/api/mcp/asset/1cae5c80-fe17-4151-8513-7071a1c4d96b",
    genre: "Filosofia",
    year: 1886,
    description: "Uma crítica à filosofia passada e uma exploração de novos valores e moralidades.",
    pages: 336,
    status: "available",
  },
  {
    id: "4",
    title: "The Republic",
    author: "Plato",
    rating: 4.8,
    cover: "https://www.figma.com/api/mcp/asset/1b18602d-f468-4881-b182-5d956d04e9b4",
    genre: "Filosofia",
    year: -375,
    description: "O diálogo socrático sobre justiça, a cidade ideal e o papel do filósofo.",
    pages: 416,
    status: "available",
  },
  {
    id: "5",
    title: "Notes from Underground",
    author: "Fyodor Dostoevsky",
    rating: 4.6,
    cover: "https://www.figma.com/api/mcp/asset/9980d6ad-7265-4f3b-9fce-006672d2a31f",
    genre: "Literatura",
    year: 1864,
    description: "A confissão de um funcionário público aposentado e ressentido na Rússia do século XIX.",
    pages: 136,
    status: "borrowed",
  },
  {
    id: "6",
    title: "Assim Falou Zaratustra",
    author: "Friedrich Nietzsche",
    rating: 4.7,
    cover: "https://www.figma.com/api/mcp/asset/92c33214-2a27-452d-96fa-3e7fb13aaf41",
    genre: "Filosofia",
    year: 1883,
    description: "A obra mais conhecida de Nietzsche, apresentando as ideias do Übermensch e do eterno retorno.",
    pages: 352,
    status: "available",
  },
  {
    id: "7",
    title: "Crime e Castigo",
    author: "Fyodor Dostoevsky",
    rating: 4.9,
    cover: "https://www.figma.com/api/mcp/asset/aefa1138-9bc9-4152-9cbf-2cf10a7e41d6",
    genre: "Literatura",
    year: 1866,
    description: "A história de Raskolnikov, um estudante que comete um assassinato e lida com as consequências psicológicas.",
    pages: 592,
    status: "available",
  },
  {
    id: "8",
    title: "O Príncipe",
    author: "Nicolau Maquiavel",
    rating: 4.5,
    cover: "https://www.figma.com/api/mcp/asset/1cae5c80-fe17-4151-8513-7071a1c4d96b",
    genre: "Política",
    year: 1532,
    description: "Um tratado político que examina como um governante pode adquirir e manter poder.",
    pages: 140,
    status: "reading",
  },
];

export const collections: Collection[] = [
  {
    id: "1",
    name: "Filosofia Clássica",
    description: "Um caminho curado através dos pensamentos fundamentais da civilização ocidental.",
    bookCount: 24,
    color: "#4c6078",
    type: "main",
  },
  {
    id: "2",
    name: "Minimalismo Moderno",
    description: "12 Livros sobre design e espaço.",
    bookCount: 12,
    color: "#e5e2da",
    type: "secondary",
  },
  {
    id: "3",
    name: "Escolha dos Críticos 2024",
    description: "Os livros mais aclamados do ano.",
    bookCount: 10,
    color: "#300d00",
    type: "dark",
  },
  {
    id: "4",
    name: "Edições Raras",
    description: "Obras difíceis de encontrar e de grande valor literário.",
    bookCount: 8,
    color: "#ebe8df",
    type: "light",
  },
];

export const borrowedBooks: (Book & { dueDate: string; borrower: string })[] = [
  {
    ...books[4],
    dueDate: "2026-05-01",
    borrower: "Ana Lima",
  },
];
