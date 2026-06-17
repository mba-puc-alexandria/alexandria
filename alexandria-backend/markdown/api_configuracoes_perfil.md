# API de Configurações do Perfil

## Endpoints

### `GET /profile/me`

Carrega os dados do perfil do usuário autenticado.

**Headers:**
```
Authorization: Bearer <token>
```

**Response `200 OK`:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "createdAt": "2026-01-15T10:30:00"
}
```

---

### `PUT /profile/me`

Atualiza username, nome e/ou sobrenome do usuário autenticado.

Todos os campos são **opcionais** — envie apenas os que deseja alterar.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request body:**
```json
{
  "username": "john_novo",
  "firstName": "John",
  "lastName": "Silva"
}
```

**Response `200 OK`:**
```json
{
  "userId": 1,
  "username": "john_novo",
  "firstName": "John",
  "lastName": "Silva",
  "email": "john@example.com",
  "createdAt": "2026-01-15T10:30:00"
}
```

**Erros possíveis:**

| Status | Motivo |
|---|---|
| `400 Bad Request` | Campo inválido (ex: username vazio, nome muito longo) |
| `401 Unauthorized` | Token ausente ou inválido |
| `409 Conflict` | Username já está em uso por outro usuário |

---

### `PUT /profile/password`

Altera a senha do usuário autenticado.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request body:**
```json
{
  "currentPassword": "minhaSenhaAtual",
  "newPassword": "minhaNovaSenha123"
}
```

> ⚠️ A nova senha deve ter **no mínimo 8 caracteres**.

**Response `204 No Content`** (sem body no retorno)

**Erros possíveis:**

| Status | Motivo |
|---|---|
| `400 Bad Request` | Nova senha não atende aos requisitos (menos de 8 caracteres) |
| `401 Unauthorized` | Senha atual incorreta ou token inválido |

---

## Regras de validação (aplicadas pelo backend)

| Campo | Regra |
|---|---|
| `username` | Obrigatório, máximo 255 caracteres |
| `firstName` | Obrigatório, máximo 255 caracteres |
| `lastName` | Obrigatório, máximo 255 caracteres |
| `newPassword` | Mínimo 8 caracteres, máximo 255 caracteres |

> ⚠️ Os campos `username`, `firstName` e `lastName` são obrigatórios na entidade, mas em requisições parciais (`PUT /profile/me`) você pode enviar `null` para manter o valor atual. O backend preserva os valores não enviados.
