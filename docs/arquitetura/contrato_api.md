API_CONTRACT.md 2026-04-30

> Alexandria — Contrato de API (MVP)

||
||
||

> Fluxo geral
>
> EXPLORAR
>
> usuário busca "hamlet"
>
> → GET /books/search?title=hamlet
>
> → backend chama Gutendex (https://gutendex.com/books?search=hamlet) →
> retorna lista (sem gravar nada no banco)
>
> → usuário clica "Adicionar à Biblioteca" → POST /user-books {
> gutenbergId: 1787 }
>
> → backend salva o livro no MySQL e cria vínculo com o usuário
>
> BIBLIOTECA
>
> usuário abre a página → GET /user-books
>
> → backend lê do MySQL
>
> → retorna os livros que o usuário adicionou
>
> Modelos de dados
>
> Book (livro salvo no MySQL)
>
> {
>
> id: number; gutenbergId: number; title: string; author: string; cover:
> string; downloadUrl: string; languages: string\[\]; subjects:
> string\[\];
>
> downloadCount: number;
>
> }

// ID do Project Gutenberg

// nome do autor principal

// URL da capa (vinda do Gutendex) // link para leitura

// ex: \["en"\], \["pt"\]

// ex: \["Fiction", "Drama"\]

> UserBook (vínculo entre usuário e livro)
>
> {
>
> id: number;
>
> 1 / 9

API_CONTRACT.md 2026-04-30

> book: Book;
>
> status: "reading" \| "toread" \| "done";
>
> progress?: number; rating?: number;
>
> }

// 0–100, somente quando status="reading"

// 0–5, somente quando status="done"

> Paginação (padrão Spring)
>
> {
>
> content: T\[\]; totalElements: number; totalPages: number;
>
> number: number; // página atual (0-based) size: number;
>
> }
>
> Erro (padrão para todos os endpoints)
>
> {
>
> message: string; status: number;
>
> }
>
> Endpoints
>
> Explorar — busca no Gutendex
>
> **GET** **/books/search?title={titulo}**
>
> Busca livros na API Gutendex. **Não** **grava** **nada** **no**
> **banco.**
>
> **Query** **params:**
>
> **Param** **Tipo**
>
> title string

**Obrigatório**

> ✅

**Descrição**

Título ou palavra-chave

> **Response** **200:**
>
> \[
>
> {
>
> "gutenbergId": 1787,
>
> "title": "The Tragedy of Hamlet, Prince of Denmark", "author":
> "Shakespeare, William",
>
> 2 / 9

API_CONTRACT.md 2026-04-30

> "cover":
> "https://www.gutenberg.org/cache/epub/1787/pg1787.cover.medium.jpg",
> "downloadUrl": "https://www.gutenberg.org/ebooks/1787",
>
> "languages": \["en"\],
>
> "subjects": \["Princes -- Drama", "Tragedy"\], "downloadCount": 163590
>
> } \]
>
> **Erros:**
>
> **Status** **Quando**
>
> 400 title ausente ou em branco
>
> 502 Gutendex indisponível
>
> Biblioteca — gerenciar coleção do usuário
>
> **GET** **/user-books?page=0&size=10&status={status}**
>
> Lista os livros que o usuário adicionou à biblioteca.
>
> **Query** **params:**
>
> **Param** **Tipo**
>
> page number
>
> size number
>
> status string

**Obrigatório**

> ❌
>
> ❌
>
> ❌

**Descrição**

Página (default: 0)

Itens por página (default: 10)

Filtrar por: reading, toread, done

> **Response** **200:**
>
> {
>
> "content": \[ {
>
> "id": 1, "book": {
>
> "id": 1, "gutenbergId": 1787,
>
> "title": "The Tragedy of Hamlet, Prince of Denmark", "author":
> "Shakespeare, William",
>
> "cover": "https://...", "downloadUrl": "https://..."
>
> },
>
> "status": "reading", "progress": 45
>
> }, {
>
> "id": 2,
>
> 3 / 9

API_CONTRACT.md 2026-04-30

> "book": { "id": 2,
>
> "gutenbergId": 84, "title": "Frankenstein",
>
> "author": "Shelley, Mary Wollstonecraft", "cover": "https://...",
>
> "downloadUrl": "https://..." },
>
> "status": "toread" }
>
> \],
>
> "totalElements": 2, "totalPages": 1, "number": 0, "size": 10
>
> }
>
> **POST** **/user-books**
>
> Adiciona um livro à biblioteca do usuário. Se o livro ainda não
> estiver no banco, o backend busca os dados no Gutendex e salva
> automaticamente.
>
> **Body:**
>
> {
>
> "gutenbergId": 1787, "status": "toread"
>
> }
>
> **Campo**
>
> gutenbergId
>
> status

**Tipo** **Obrigatório**

number ✅

string ❌

**Descrição**

ID do livro no Gutendex

toread (default), reading, done

> **Response** **201:**
>
> {
>
> "id": 1, "book": {
>
> "id": 1, "gutenbergId": 1787,
>
> "title": "The Tragedy of Hamlet, Prince of Denmark", "author":
> "Shakespeare, William",
>
> "cover": "https://...", "downloadUrl": "https://..."
>
> },
>
> 4 / 9

API_CONTRACT.md 2026-04-30

> "status": "toread" }
>
> **Erros:**
>
> **Status** **Quando**
>
> 400 gutenbergId ausente
>
> 409 Livro já está na biblioteca do usuário
>
> 502 Gutendex indisponível ao tentar buscar dados do livro
>
> **PUT** **/user-books/{id}**
>
> Atualiza o status ou progresso de leitura de um livro na biblioteca.
>
> **Path** **param:** id = ID do UserBook
>
> **Body:**
>
> {
>
> "status": "reading", "progress": 60
>
> }
>
> ou ao concluir:
>
> {
>
> "status": "done", "rating": 5
>
> }
>
> **Campo**
>
> status
>
> progress
>
> rating

**Tipo** **Obrigatório**

string ❌

number ❌

number ❌

**Descrição**

reading, toread, done

0–100, só faz sentido com status=reading

0–5, só faz sentido com status=done

> **Response** **200:**
>
> {
>
> "id": 1,
>
> "book": { ... }, "status": "done",
>
> 5 / 9

API_CONTRACT.md 2026-04-30

> "rating": 5 }
>
> **Erros:**
>
> **Status** **Quando**
>
> 404 UserBook não encontrado
>
> **DELETE** **/user-books/{id}**
>
> Remove um livro da biblioteca do usuário. **Não** **apaga** **o**
> **livro** **do** **MySQL**, só o vínculo.
>
> **Path** **param:** id = ID do UserBook
>
> **Response** **204** **No** **Content**
>
> **Erros:**
>
> **Status** **Quando**
>
> 404 UserBook não encontrado
>
> Health check
>
> **GET** **/actuator/health**
>
> **Response** **200:**
>
> { "status": "UP" }
>
> Resumo dos endpoints (MVP)
>
> **Método** **Rota**
>
> GET /books/search?title=
>
> GET /user-books
>
> POST /user-books
>
> PUT /user-books/{id}
>
> DELETE /user-books/{id}
>
> GET /actuator/health

**Página**

Explorar

Biblioteca

Explorar

Biblioteca

Biblioteca

—

**Descrição**

Busca no Gutendex, sem gravar

Lista livros do usuário

Adiciona livro à biblioteca

Atualiza status/progresso

Remove da biblioteca

Health check

> Implementação no banco (sugestão de tabelas) 6 / 9

API_CONTRACT.md 2026-04-30

> -- Livros vindos do Gutendex (gravados ao adicionar à biblioteca)
> CREATE TABLE books (
>
> id gutenberg_id title author cover download_url languages
>
> subjects
>
> BIGINT AUTO_INCREMENT PRIMARY KEY, INT UNIQUE NOT NULL,
>
> VARCHAR(500) NOT NULL, VARCHAR(255),

TEXT, TEXT,

> VARCHAR(100),

TEXT,

> download_count INT );
>
> -- Vínculo usuário ↔ livro CREATE TABLE user_books (
>
> id book_id status progress
>
> rating
>
> BIGINT AUTO_INCREMENT PRIMARY KEY, BIGINT NOT NULL REFERENCES
> books(id),

ENUM('reading', 'toread', 'done') DEFAULT 'toread', TINYINT DEFAULT 0,

TINYINT,

> created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP );
>
> O que já existe no histórico git do backend
>
> O commit 8aca018 já implementa:
>
> GutendexClient — integração com https://gutendex.com GET
> /books/search?title= — busca no Gutendex
>
> POST /books — salva livro no MySQL
>
> O time precisa adaptar o POST /books para virar POST /user-books,
> adicionando a lógica de vínculo com o usuário.
>
> Pontos críticos para o backend funcionar
>
> 1\. CORS — obrigatório para o frontend conseguir chamar a API
>
> O Next.js roda em localhost:3000 e o backend em localhost:8080. Sem
> CORS configurado, o browser bloqueia todas as chamadas.
>
> O backend precisa de uma configuração assim:
>
> @Configuration
>
> public class CorsConfig {
>
> @Bean
>
> public WebMvcConfigurer corsConfigurer() {
>
> 7 / 9

API_CONTRACT.md 2026-04-30

> return new WebMvcConfigurer() { @Override
>
> public void addCorsMappings(CorsRegistry registry) {
> registry.addMapping("/\*\*")
>
> .allowedOrigins("http://localhost:3000") .allowedMethods("GET",
> "POST", "PUT", "DELETE", "OPTIONS") .allowedHeaders("\*");
>
> } };
>
> } }
>
> 2\. Spring Security — desabilitar para o MVP
>
> O pom.xml tem spring-boot-starter-security. Por padrão, o Spring
> Security bloqueia **todos** **os** **endpoints** com HTTP 401, mesmo
> sem nenhuma configuração.
>
> Para o MVP, o time deve desabilitar a segurança explicitamente:
>
> @Configuration @EnableWebSecurity
>
> public class SecurityConfig {
>
> @Bean
>
> public SecurityFilterChain filterChain(HttpSecurity http) throws
> Exception { http
>
> .csrf(csrf -\> csrf.disable())
>
> .authorizeHttpRequests(auth -\> auth.anyRequest().permitAll()); return
> http.build();
>
> } }

||
||
||

> 3\. application.properties — usar as credenciais do docker-compose
>
> O application.properties deve bater com o que está no
> docker-compose.yml:
>
> spring.application.name=alexandria-backend
>
> \# MySQL (credenciais do docker-compose)
> spring.datasource.url=jdbc:mysql://localhost:3306/alexandriadb?
> createDatabaseIfNotExist=true
>
> spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
> spring.datasource.username=alexandria
> spring.datasource.password=alexandria123
>
> 8 / 9

API_CONTRACT.md 2026-04-30

> \# JPA
>
> spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
> spring.jpa.hibernate.ddl-auto=update
>
> spring.jpa.show-sql=true
>
> \# Paginação
>
> spring.data.web.pageable.max-page-size=100
>
> 4\. user_books sem autenticação — MVP single-user
>
> Como o MVP não tem login, a tabela user_books não precisa de user_id
> por enquanto. Trata o sistema como single-user.
>
> Quando autenticação for adicionada no futuro, basta incluir o user_id
> na tabela e filtrar pelos livros do usuário logado.
>
> -- MVP: sem user_id CREATE TABLE user_books (
>
> id book_id status progress
>
> rating
>
> BIGINT AUTO_INCREMENT PRIMARY KEY, BIGINT NOT NULL REFERENCES
> books(id),

ENUM('reading', 'toread', 'done') DEFAULT 'toread', TINYINT DEFAULT 0,

TINYINT,

> created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP );
>
> 5\. Atenção ao package name
>
> O commit 8aca018 usa com.pucsp.alexandria.
>
> O projeto atual usa com.alexandria.alexandria_backend.
>
> O time deve escolher um e manter consistente em todos os arquivos.
>
> 9 / 9
