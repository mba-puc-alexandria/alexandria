# Script de Inicialização do Ambiente de Desenvolvimento

## Contexto

Para desenvolver localmente é necessário subir três processos em ordem:
1. Banco de dados MySQL (via Docker Compose)
2. Backend Spring Boot (Java)
3. Frontend Next.js

O script `scripts/dev-start.ps1` automatiza essa sequência, verifica pré-requisitos e aguarda cada serviço responder antes de iniciar o próximo.

---

## Arquivo criado

### `scripts/dev-start.ps1`

Script PowerShell executado a partir da raiz do projeto (`alexandria/`).

**O que ele faz, em ordem:**

1. **Verifica pré-requisitos** — falha imediatamente se `docker`, `java` ou `node` não estiverem no PATH
2. **Sobe o MySQL** via `docker compose up -d` na pasta `alexandria-backend/`
3. **Aguarda o MySQL** — polling com `mysqladmin ping` até o banco aceitar conexões (máx. 30 segundos)
4. **Inicia o Spring Boot** em uma nova janela PowerShell via `mvnw.cmd spring-boot:run`
5. **Aguarda o backend** — polling em `http://localhost:8080/actuator/health` (máx. 60 segundos)
6. **Instala dependências npm** se `node_modules` estiver ausente
7. **Inicia o Next.js** em uma nova janela PowerShell via `npm run dev`
8. **Exibe um resumo** com as URLs de cada serviço

---

## Como usar

```powershell
# Na raiz do projeto (alexandria/)
.\scripts\dev-start.ps1
```

Ou via atalho no terminal:

```powershell
cd D:\projetos\projeto_integrador\alexandria
.\scripts\dev-start.ps1
```

---

## Pré-requisitos

| Ferramenta | Versão mínima | Como instalar |
|---|---|---|
| Docker Desktop | qualquer recente | https://www.docker.com/products/docker-desktop |
| Java (JDK) | 17 | https://adoptium.net |
| Node.js | 18 | https://nodejs.org |

---

## Serviços e portas

| Serviço | URL | Observação |
|---|---|---|
| Frontend (Next.js) | `http://localhost:3000` | Redireciona para `/explorar` |
| Backend (Spring Boot) | `http://localhost:8080` | API REST + `/actuator/health` |
| Banco de dados (MySQL) | `localhost:3306` | Database: `alexandriadb`, usuário/senha: `root/root` |

---

## Como parar o ambiente

Feche as duas janelas abertas pelo script (Spring Boot e Next.js) e depois pare o Docker:

```powershell
docker compose -f .\alexandria-backend\docker-compose.yaml down
```

Para remover os dados do banco (reset completo):

```powershell
docker compose -f .\alexandria-backend\docker-compose.yaml down -v
```

---

## Variáveis de ambiente relevantes

O arquivo `.env.local` (na raiz do projeto, criado no step `ambientes-dev-prod.md`) aponta o frontend para o backend local:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Sem esse arquivo o frontend chamaria a instância de produção na EC2.

---

## Estrutura de janelas ao executar

```
Janela original (onde o script foi chamado)
    └── Exibe resumo final e instruções de parada

Nova janela — Backend
    └── mvnw.cmd spring-boot:run
        └── Logs do Spring Boot em tempo real

Nova janela — Frontend
    └── npm run dev
        └── Logs do Next.js em tempo real
```

---

## Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| `[ERRO] Docker não encontrado` | Docker Desktop não está instalado ou não está no PATH | Instale o Docker Desktop e reinicie o terminal |
| MySQL não fica pronto em 30s | Container travou ou porta 3306 ocupada | `docker compose logs db` para ver o erro |
| Backend não responde em 60s | Falha de compilação Maven | Verifique a janela do Spring Boot; pode ser erro de JDK ou dependência |
| `npm run dev` falha | Dependências corrompidas | `Remove-Item node_modules -Recurse -Force` e re-execute o script |
| Frontend não conecta no backend | `.env.local` ausente ou com URL errada | Verifique se `NEXT_PUBLIC_API_URL=http://localhost:8080` está em `.env.local` |
