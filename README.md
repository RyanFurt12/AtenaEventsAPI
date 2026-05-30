# AtenaEvents API

API REST de gestão de eventos do **AtenaEvents**, em **Spring Boot 4 / Java 17**.

Faz parte de um projeto maior orquestrado via Docker Compose. Para subir tudo (API + frontend + banco) de uma vez, veja o repositório raiz. Este README cobre **a API isoladamente**.

---

## 1. Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4 (Web, Data JPA, Security, Validation) |
| Banco | PostgreSQL 16 (Hibernate `ddl-auto=update`) |
| Auth | JWT (jjwt) + OAuth2 Client (Google/GitHub) |
| Email | Spring Mail (MailHog em dev, SMTP real em prod) |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Maven (`./mvnw`) |

---

## 2. Rodar

### Recomendado — via Docker Compose (no repositório raiz)

O modo suportado de execução é pelo `docker compose up --build` do repositório raiz, que sobe a API junto com o Postgres e o frontend já conectados. **Não rode a API isolada para testar o sistema completo.**

### Build local (apenas checagem — não sobe o sistema)

```bash
# Gerar o JAR (pula testes)
./mvnw package -DskipTests

# Rodar a aplicação (exige Postgres acessível e as variáveis de ambiente abaixo)
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

---

## 3. Variáveis de ambiente

Todas são lidas pelo `application.properties`. No Docker, vêm do `.env` do repositório raiz.

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `DATABASE_URL` | ✅ | — | JDBC URL do Postgres (ex.: `jdbc:postgresql://db:5432/atena_events`) |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | ✅ | — | Credenciais do banco |
| `JWT_SECRET` | ✅ | — | Chave de assinatura JWT (mín. 32 chars) |
| `API_URL` | ✅ | — | URL pública da API (usada no `redirect-uri` do OAuth2) |
| `FRONTEND_URL` | ✅ | — | URL do frontend (CORS + redirect pós-OAuth) |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | — | `disabled` | OAuth Google (botão de login social fica inativo sem isso) |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | — | `disabled` | OAuth GitHub |
| `MAIL_HOST` / `MAIL_PORT` | — | `localhost` / `1025` | SMTP (MailHog em dev) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | — | vazio | Credenciais SMTP (prod) |
| `MAIL_SMTP_AUTH` / `MAIL_SMTP_STARTTLS` | — | `false` | Flags SMTP (prod = `true`) |
| `MAIL_FROM` | — | `no-reply@atenaevents.local` | Remetente dos emails |

Tokens: JWT de acesso expira em 15 min; token de convidado em 2 h; tokens de reset/troca de email em 30 min.

---

## 4. Documentação da API (Swagger)

Com a API no ar:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## 5. Arquitetura

Arquitetura em camadas padrão Spring: `controller → service → repository`.

```
com.atena.events
├── config/      → BeanConfig, GlobalExceptionHandler
├── controller/  → Auth, Event, Participation, Comment, User
├── model/       → entidades JPA + enum AccountType
│   └── dto/     → DTOs de request/response
├── repository/  → Spring Data JPA repositories
├── security/    → JwtService, JwtAuthFilter, SecurityConfig, OAuthSuccessHandler
└── service/     → regras de negócio (Event, Participation, Guest, Comment, User, Mail)
```

### Entidades

| Entidade | Tabela | Descrição |
|---|---|---|
| `User` | `users` | Usuário. `accountType`: `PASSWORD \| GUEST \| GOOGLE \| GITHUB` |
| `Event` | `event` | Evento, com `owner` (FK→User) |
| `Participation` | `participation` | Inscrição de um usuário num evento |
| `Comment` | `comment` | Comentário num evento |
| `RefreshToken` | `refresh_token` | Refresh token JWT |
| `PasswordResetToken` | — | Token de recuperação de senha |
| `EmailChangeToken` | — | Token de confirmação de troca de email |

> Imagens/avatares enviados são armazenados como base64 em colunas `TEXT`; avatares de OAuth são URLs (`avatarUrl`).

### Endpoints (resumo)

| Controller | Base | Principais |
|---|---|---|
| `AuthController` | `/auth` | `login`, `register`, `refresh`, `logout`, `guest`, `upgrade/password`, `merge-guest`, recuperação/troca de senha e email |
| `EventController` | `/events` | CRUD, `recommended`, `created_by/{id}`, `participated_by/{id}`, `{id}/participants` (dono) |
| `ParticipationController` | `/participate` | `toggle/event/{e}/user/{u}`, listagens por evento/usuário |
| `CommentController` | `/comments` | CRUD de comentários por evento |
| `UserController` | `/users` | get/update/delete, upload de avatar |

### Autenticação

- **JWT** stateless via header `Authorization: Bearer <token>` (`JwtAuthFilter`).
- **OAuth2** (Google/GitHub) via sessão HTTP; ao final, `OAuthSuccessHandler` redireciona para `{FRONTEND_URL}/oauth-callback?accessToken=...&refreshToken=...`.
- **Contas de convidado** recebem `ROLE_GUEST` (acesso restrito); demais contas, `ROLE_USER`.

---

## 6. Banco de dados

- Schema gerenciado pelo Hibernate (`ddl-auto=update`) — tabelas criadas/atualizadas automaticamente.
- Uma migração manual idempotente (`db-init.sql`, no repositório raiz) remove o `NOT NULL` das colunas `email`/`password` em `users`, necessário para contas de convidado e OAuth.
