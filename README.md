# Alexandria-sistema

O Projeto Alexandria trata-se de uma Biblioteca Digital para gerenciamento de biblioteca, permitindo o cadastro e controle de livros, autores e edioras ele é o projeto integrador desenvolvido para o MBA em Engenharia de Software da PUC-SP.

## Arquitetura e Stack Tecnológica

A arquitetura do projeto baseia-se no padrão monolítico, utilizando as seguintes tecnologias:

# Backend (Java + Spring)

- Java 17: versão LTS, estável, segura e com bom desempenho.
- Spring Boot 4: acelera o desenvolvimento com configuração automática e menor boilerplate.
- Spring Data JPA: facilita o acesso ao banco com menos código SQL, aumentando produtividade.
- Spring Web MVC: padrão sólido para criar APIs REST bem estruturadas.
- Spring Boot Actuator: permite monitorar saúde, métricas e performance da aplicação facilmente.

# Banco de dados

- MySQL 8: confiável, amplamente utilizado e com ótimo suporte para aplicações web.

# Cache

- Redis: alta performance para sessões e cache

# Infraestrutura e build

- Maven: gerenciamento simples e padronizado de dependências e build.
- Docker Compose: facilita subir o banco e serviços rapidamente, garantindo ambiente consistente.

# Frontend

- Next.js + TypeScript: frontend moderno com SSR/SSG, melhor performance e tipagem forte, reduzindo erros.

# Porque dssa Stack

Essa stack é escolhida porque oferece:

- Alta produtividade no desenvolvimento
- Escalabilidade e manutenção facilitada
- Ferramentas maduras e bem suportadas
- Integração eficiente entre backend e frontend


### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose (para o banco de dados)

---

**Projeto Acadêmico** - MBA Engenharia de Software | PUC-SP
