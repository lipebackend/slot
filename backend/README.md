# Slot

Backend educacional de agendamento. Uma aplicação atende vários organizadores. Visitantes reservam sem criar conta.

O contrato está em [docs/contract.md](docs/contract.md). Deploy, rollback, backup e restauração estão em [docs/operations.md](docs/operations.md).

## Executar localmente

Com JDK 21 e Docker:

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

A API fica em http://localhost:8080. O PostgreSQL local usa banco, usuário e senha `slot`. Outros ambientes definem `SLOT_DB_URL`, `SLOT_DB_USER` e `SLOT_DB_PASSWORD`. O arquivo `.env.example` lista as variáveis de produção.

Para subir a imagem junto com o banco:

```powershell
docker compose up -d --build
```

Esse comando também sobe o frontend Angular em http://localhost:4200, servido pelo Nginx com proxy de `/api` para o backend. Não execute `npm start` na mesma porta. O mesmo comando funciona na raiz do projeto; veja [as instruções completas](../README.md).

## Testes

```powershell
.\mvnw.cmd clean verify
```

Os testes de integração usam PostgreSQL temporário via Testcontainers e precisam do Docker. Eles não alteram o banco do Compose.

Sem Docker, só as regras locais:

```powershell
.\mvnw.cmd "-Dtest=DayScheduleTest,SlotGridTest,RegistrationRulesTest,BookingRulesTest,DatabaseErrorsTest,AvailabilityControllerTest" test
```

## Perfis

O perfil padrão serve o desenvolvimento em HTTP, com cookie de sessão `HttpOnly` e `SameSite=Lax`. O perfil `prod` exige HTTPS, marca o cookie como `Secure` e não tem senha de banco padrão. Segredos entram só por variável de ambiente.
