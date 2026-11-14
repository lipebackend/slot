# Contrato da API — Slot V1

Aplicação única para vários organizadores. Fuso fixo `America/Maceio`. Atendimento fixo de 30 minutos. API em `/api`, JSON em camelCase e identificadores opacos em string.

Dias: `MONDAY` a `SUNDAY`. Horários semanais: `HH:mm`, precisão de minutos. Datas: `YYYY-MM-DD`. Instantes: ISO 8601 com offset; a resposta usa o offset de Aracaju. Estados de reserva: `ACTIVE` e `CANCELLED`.

Nome, após trim, tem de 1 a 120 caracteres. E-mail de conta é trim e lowercase, até 254 caracteres, único. E-mail de visitante é trim, formato válido, até 254 caracteres. Senha tem de 8 a 128 caracteres e não sofre trim. Respostas não contêm senha nem hash.

## Erros

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Revise os campos informados.",
  "fieldErrors": [
    { "field": "email", "message": "Informe um e-mail válido." }
  ]
}
```

`fieldErrors` pode ser uma lista vazia. Não há stack trace nem detalhe de banco.

| HTTP | Uso |
| --- | --- |
| 400 | JSON ou dados inválidos. Código `VALIDATION_ERROR`. |
| 401 | Sem sessão (`UNAUTHENTICATED`) ou credenciais inválidas (`INVALID_CREDENTIALS`). |
| 403 | CSRF inválido (`CSRF_INVALID`) ou sessão sem permissão (`FORBIDDEN`). |
| 404 | Recurso inexistente ou de outro organizador (`NOT_FOUND`). |
| 409 | E-mail duplicado (`EMAIL_ALREADY_REGISTERED`) ou horário indisponível (`SLOT_UNAVAILABLE`). |
| 500 | Falha inesperada (`INTERNAL_ERROR`), com mensagem genérica. |

## Organizador

`POST /api/organizers` é público e não autentica. Cria a conta e a semana inicial na mesma transação: segunda a sexta, 09:00–12:00; sábado e domingo fechados. O `bookingPath` é `/book/{id}` e o id é estável.

## Sessão

Cookie de sessão `HttpOnly` e `SameSite=Lax`. Em produção o cookie é `Secure`, o tráfego é HTTPS e a proteção CSRF fica ligada. O token de autenticação não vai para `localStorage`.

- `GET /api/auth/csrf` responde `{ "headerName", "token" }` antes do login.
- `POST /api/auth/login` responde os dados públicos do organizador. Credencial inválida usa mensagem genérica.
- `GET /api/auth/me` exige sessão.
- `POST /api/auth/logout` responde 204 e invalida a sessão. Sem sessão, também pode responder 204.

O frontend renova o CSRF depois do login e do logout. Operações que alteram dados enviam o header devolvido por `/api/auth/csrf`.

## Disponibilidade

`GET /api/availability` e `POST /api/availability` exigem sessão. A consulta devolve `timezone` e exatamente sete dias, de segunda a domingo, com intervalos ordenados por início. O POST substitui só o dia informado. Lista vazia fecha o dia. Lista ausente, início não anterior ao fim, sobreposição ou duplicação rejeitam a operação inteira. Intervalos encostados são válidos e não atravessam a meia-noite. Repetir a mesma configuração não duplica linhas. A alteração não cancela reservas. Atualizações concorrentes do mesmo dia terminam numa lista completa.

## Perfil e horários públicos

`GET /api/public/organizers/{organizerId}` devolve id, nome, fuso e `eventDurationMinutes` 30. Não devolve e-mail nem agenda administrativa.

`GET /api/public/organizers/{organizerId}/slots?date=YYYY-MM-DD` gera blocos de 30 minutos a partir do início de cada intervalo. O bloco cabe inteiro no intervalo, que é início inclusivo e término exclusivo. Não oferece início menor ou igual ao instante atual nem bloco sobreposto a reserva `ACTIVE` daquele organizador. Data passada ou dia sem vaga devolve lista vazia. Data inválida devolve 400. A consulta não reserva a vaga.

## Reservas

`POST /api/public/organizers/{organizerId}/bookings` recebe nome, e-mail e `startsAt` com offset. O servidor calcula o término, normaliza o instante e revalida futuro, grade e disponibilidade. Fora da grade, ocupado ou já iniciado responde 409 `SLOT_UNAVAILABLE`. Reservas `ACTIVE` do mesmo organizador não se sobrepõem no PostgreSQL. A troca de disponibilidade e a criação de reserva do mesmo organizador são serializadas pela trava da linha do organizador. Reserva cancelada não ocupa horário. Não há mensagem nem conta de visitante.

`GET /api/bookings` lista as reservas do organizador autenticado, ativas e canceladas, por início crescente e identificador como desempate.

`POST /api/bookings/{bookingId}/cancel` não tem corpo e responde 204. Só o dono cancela. Recurso de outro organizador ou inexistente responde 404. Repetir o cancelamento responde 204. O horário volta a ser oferecido se ainda for futuro e estiver na configuração atual.

## Fora da V1

Equipes, pagamentos, videochamadas, calendários externos, e-mail, SMS, recorrência, reagendamento, bloqueio por data, recuperação de senha, verificação de e-mail e cancelamento pelo visitante.
