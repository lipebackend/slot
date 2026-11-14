# Slot

Aplicação de agendamento inspirada no fluxo básico do Cal.com, composta por **Angular 19** no frontend e **Spring Boot (Java 21)** com **PostgreSQL** no backend.

## Funcionalidades

- **Organizadores**: cadastro, autenticação por sessão (com proteção CSRF) e configuração de disponibilidade semanal por intervalos de horário.
- **Página pública de agendamento**: visualização de horários disponíveis para um organizador e criação de reservas.
- **Gestão de agendamentos**: listagem e gerenciamento de reservas realizadas.
- **Empacotamento unificado**: em produção, o frontend é compilado e servido diretamente pelo backend Spring Boot na mesma origem.

## Stack

- **Frontend**: Angular 19, TypeScript
- **Backend**: Java 21, Spring Boot, Spring Security, Spring Data JDBC / Flyway
- **Banco de dados**: PostgreSQL
- **Infraestrutura**: Docker, Docker Compose, GitHub Actions

## Estrutura do Projeto

```text
├── backend/          # API Spring Boot, migrations Flyway e testes
├── frontend/         # Aplicação Angular 19
├── Dockerfile        # Build unificado com JRE customizada (jlink)
├── Dockerfile.native # Build unificado com GraalVM Native Image
└── compose.yaml      # Orquestração local com Docker Compose
```

## Como executar localmente

Com o Docker iniciado, execute na raiz do projeto:

```bash
docker compose up -d --build
```

- **Aplicação**: `http://localhost:4200`
- **Healthcheck da API**: `http://localhost:8080/api/health`

Para rodar backend e frontend separadamente em desenvolvimento, consulte [backend/README.md](backend/README.md) e [frontend/README.md](frontend/README.md).
