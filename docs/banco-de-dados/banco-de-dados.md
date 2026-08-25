# Banco de Dados — Tabelas e Conexão com Client MySQL

## Credenciais (ambiente local)

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Porta | `3306` |
| Database | `alexandriadb` |
| Usuário | `root` |
| Senha | `root` |

---

## Tabelas

O Hibernate cria as tabelas automaticamente (`ddl-auto=update`) com base nas entidades JPA.  
São três tabelas:

---

### `users`

Cadastro de usuários da aplicação.

| Coluna | Tipo | Restrições |
|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `username` | VARCHAR | NOT NULL, UNIQUE |
| `first_name` | VARCHAR | NOT NULL |
| `last_name` | VARCHAR | NOT NULL |
| `email` | VARCHAR | NOT NULL, UNIQUE |
| `password` | VARCHAR | NOT NULL (hash BCrypt) |
| `created_at` | DATETIME | NOT NULL, não atualizável |

---

### `books`

Catálogo de livros — tanto os adicionados manualmente quanto os importados do Gutendex.

| Coluna | Tipo | Restrições |
|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `title` | VARCHAR(500) | NOT NULL |
| `author` | VARCHAR(500) | NOT NULL |
| `source` | VARCHAR | NOT NULL (`GUTENDEX` ou `MANUAL`) |
| `gutendex_id` | BIGINT | UNIQUE, pode ser NULL |
| `download_url` | LONGTEXT | pode ser NULL |
| `cover_url` | LONGTEXT | pode ser NULL |
| `languages` | LONGTEXT | pode ser NULL |
| `subjects` | LONGTEXT | pode ser NULL |
| `download_count` | INT | pode ser NULL |
| `publisher_id` | BIGINT | pode ser NULL |

---

### `user_books`

Relacionamento entre usuário e livro — o acervo pessoal de cada usuário.

| Coluna | Tipo | Restrições |
|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → `users.id`, NOT NULL |
| `book_id` | BIGINT | FK → `books.id`, NOT NULL |
| `status` | VARCHAR(20) | NOT NULL, default `toread` |
| `progress` | INT | pode ser NULL (0–100) |
| `rating` | INT | pode ser NULL |
| `created_at` | DATETIME | NOT NULL, não atualizável |

Constraint única: `(user_id, book_id)` — um usuário não pode adicionar o mesmo livro duas vezes.

**Valores possíveis de `status`:** `toread`, `reading`, `read`, `borrowed`

---

### Diagrama de relacionamento

```
users ──────────────── user_books ──────────────── books
  id (PK)               id (PK)                     id (PK)
  username              user_id (FK)                title
  email                 book_id (FK)                author
  password              status                      source
  ...                   progress                    cover_url
                        rating                      ...
                        created_at
```

---

## Como conectar com um client MySQL

### Opção 1 — DBeaver (recomendado, gratuito)

1. Baixe em https://dbeaver.io/download/
2. Abra o DBeaver → clique em **New Database Connection** (ícone de tomada)
3. Selecione **MySQL** → clique em **Next**
4. Preencha:
   - **Server Host:** `localhost`
   - **Port:** `3306`
   - **Database:** `alexandriadb`
   - **Username:** `root`
   - **Password:** `root`
5. Clique em **Test Connection** — se aparecer "Connected", clique em **Finish**
6. No painel esquerdo: `alexandria` → `alexandriadb` → `Tables`

---

### Opção 2 — TablePlus (gratuito com limite)

1. Baixe em https://tableplus.com/
2. Clique em **+** para nova conexão → selecione **MySQL**
3. Preencha:
   - **Name:** Alexandria Local
   - **Host:** `localhost`
   - **Port:** `3306`
   - **User:** `root`
   - **Password:** `root`
   - **Database:** `alexandriadb`
4. Clique em **Test** → **Connect**

---

### Opção 3 — MySQL Workbench (oficial da Oracle)

1. Baixe em https://dev.mysql.com/downloads/workbench/
2. Na tela inicial, clique em **+** ao lado de "MySQL Connections"
3. Preencha:
   - **Connection Name:** Alexandria Local
   - **Hostname:** `127.0.0.1`
   - **Port:** `3306`
   - **Username:** `root`
4. Clique em **Store in Vault** para salvar a senha `root`
5. Clique em **Test Connection** → **OK**

---

### Opção 4 — terminal (sem instalar nada)

Se o Docker estiver rodando, você pode acessar o MySQL diretamente pelo container:

```powershell
# Abre o shell MySQL dentro do container
docker compose -f .\alexandria-backend\docker-compose.yaml exec mysql mysql -u root -proot alexandriadb
```

Dentro do shell MySQL:

```sql
-- Listar tabelas
SHOW TABLES;

-- Ver estrutura de uma tabela
DESCRIBE users;
DESCRIBE books;
DESCRIBE user_books;

-- Consultar dados
SELECT id, username, email, created_at FROM users;
SELECT id, title, author, source FROM books LIMIT 20;
SELECT ub.id, u.username, b.title, ub.status, ub.progress
  FROM user_books ub
  JOIN users u ON u.id = ub.user_id
  JOIN books b ON b.id = ub.book_id;
```

---

## Queries úteis para debug

```sql
-- Quantos usuários cadastrados
SELECT COUNT(*) AS total_users FROM users;

-- Livros por origem
SELECT source, COUNT(*) AS total FROM books GROUP BY source;

-- Acervo de um usuário específico
SELECT b.title, b.author, ub.status, ub.progress, ub.rating
FROM user_books ub
JOIN books b ON b.id = ub.book_id
JOIN users u ON u.id = ub.user_id
WHERE u.username = 'nome_do_usuario';

-- Livros com status "reading"
SELECT u.username, b.title, ub.progress
FROM user_books ub
JOIN users u ON u.id = ub.user_id
JOIN books b ON b.id = ub.book_id
WHERE ub.status = 'reading'
ORDER BY ub.progress DESC;
```

---

## Observação — senha dos usuários

A coluna `password` em `users` armazena o hash BCrypt, nunca a senha em texto puro.  
Exemplo de valor: `$2a$10$...` — isso é esperado e correto.
