# Alexandria

API REST para gerenciamento de biblioteca digital, permitindo o cadastro e controle de livros, autores e editoras.

## 📋 Sobre o Projeto

Alexandria é uma aplicação Spring Boot que fornece uma API RESTful completa para gerenciar:
- **Livros**: título e gênero
- **Autores**: nome e nacionalidade  
- **Editoras**: nome e endereço

A aplicação utiliza banco de dados MySQL e segue padrões de arquitetura em camadas (Controller, Service, Repository).

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 4.0.3**
- **Spring Data JPA** - Persistência de dados
- **Spring Web MVC** - API REST
- **Spring Boot Actuator** - Monitoramento e métricas
- **MySQL 8.0.32** - Banco de dados
- **Maven** - Gerenciamento de dependências
- **Docker Compose** - Containerização do banco de dados

## 📦 Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose (para o banco de dados)

## ⚙️ Configuração e Instalação

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd alexandria
```

### 2. Inicie o banco de dados MySQL

```bash
docker-compose up -d
```

Isso iniciará um container MySQL na porta 3306 com:
- Database: `alexandriadb`
- Usuário: `root`
- Senha: `root`

### 3. Execute a aplicação

```bash
./mvnw spring-boot:run
```

Ou no Windows:

```bash
mvnw.cmd spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 📚 Endpoints da API

### Books (Livros)

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| GET | `/books` | Lista todos os livros | - |
| GET | `/books/{id}` | Busca livro por ID | - |
| POST | `/books` | Cria novo livro | `{"title": "string", "genre": "string"}` |
| PUT | `/books/{id}` | Atualiza livro | `{"title": "string", "genre": "string"}` |
| DELETE | `/books/{id}` | Remove livro | - |

### Authors (Autores)

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| GET | `/authors` | Lista todos os autores | - |
| GET | `/authors/{id}` | Busca autor por ID | - |
| POST | `/authors` | Cria novo autor | `{"name": "string", "nationality": "string"}` |
| PUT | `/authors/{id}` | Atualiza autor | `{"name": "string", "nationality": "string"}` |
| DELETE | `/authors/{id}` | Remove autor | - |

### Publishers (Editoras)

| Método | Endpoint | Descrição | Body |
|--------|----------|-----------|------|
| GET | `/publishers` | Lista todas as editoras | - |
| GET | `/publishers/{id}` | Busca editora por ID | - |
| POST | `/publishers` | Cria nova editora | `{"name": "string", "address": "string"}` |
| PUT | `/publishers/{id}` | Atualiza editora | `{"name": "string", "address": "string"}` |
| DELETE | `/publishers/{id}` | Remove editora | - |

## 🔍 Exemplos de Uso

### Criar um novo livro

```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title": "1984", "genre": "Ficção Científica"}'
```

### Listar todos os autores

```bash
curl http://localhost:8080/authors
```

### Buscar editora por ID

```bash
curl http://localhost:8080/publishers/1
```

## 🏗️ Estrutura do Projeto

```
alexandria/
├── src/main/java/com/betrybe/alexandria/
│   ├── controller/          # Controllers REST
│   │   └── dto/            # Data Transfer Objects
│   ├── entity/             # Entidades JPA
│   ├── exception/          # Tratamento de exceções
│   ├── repository/         # Repositórios JPA
│   └── service/            # Lógica de negócio
├── src/main/resources/
│   └── application.properties  # Configurações da aplicação
├── docker-compose.yaml     # Configuração do MySQL
└── pom.xml                # Dependências Maven
```

## 🔧 Configuração do Banco de Dados

As configurações do banco estão em `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/alexandriadb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

O Hibernate criará automaticamente as tabelas no banco de dados na primeira execução.

## 📊 Monitoramento

A aplicação possui Spring Boot Actuator habilitado para monitoramento. Endpoints de saúde e métricas estarão disponíveis em `/actuator`.

## 🛠️ Desenvolvimento

### Compilar o projeto

```bash
./mvnw clean install
```

### Executar testes

```bash
./mvnw test
```

## 📝 Tratamento de Erros

A API utiliza um `GlobalExceptionHandler` para tratar exceções de forma centralizada:
- `AuthorNotFoundException` - Autor não encontrado
- `BookNotFoundException` - Livro não encontrado
- `PublisherNotFoundException` - Editora não encontrada

Todas as respostas de erro seguem um padrão consistente com códigos HTTP apropriados.

## 📄 Licença

Este projeto foi desenvolvido como parte de um estudo de Spring Boot.

---

Desenvolvido com ☕ e Spring Boot