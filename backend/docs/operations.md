# Operação

Segredos ficam fora do código. Produção usa o perfil `prod` e as variáveis `SLOT_DB_URL`, `SLOT_DB_USER` e `SLOT_DB_PASSWORD`. O cookie de sessão sai `Secure` e a aplicação exige HTTPS, honrando `X-Forwarded-Proto` atrás do proxy. O endpoint `GET /api/health` responde só `{"status":"UP"}` ou `{"status":"DOWN"}`.

Logs registram método, caminho, status e `X-Request-Id`. Não registram senha, hash, token CSRF nem o corpo da requisição.

Para ambientes com pouca memória (como Free Tier), o backend aceita ajuste do pool e threads sem recompilar:

- `SLOT_TOMCAT_MAX_THREADS` (padrão `20`) e `SLOT_TOMCAT_MIN_THREADS` (padrão `2`)
- `SLOT_DB_POOL_SIZE` (padrão `4`) e `SLOT_DB_MIN_IDLE` (padrão `1`)

## Deploy

1. Construir a imagem unificada (Frontend Angular + Backend juntos, menor uso de recursos) a partir da **raiz do repositório**:

```powershell
# Opção A: Custom JRE (jlink) + SerialGC (build rápido, ~100-130 MB RAM)
docker build -t slot-app:release .

# Opção B: GraalVM Native Image + UPX + Distroless (~40-60 MB RAM, startup < 0.2s)
docker build -f Dockerfile.native -t slot-app:native .
```

Para construir apenas o backend isolado (ex.: com proxy reverso externo servindo estáticos):

```powershell
docker build -t slot-backend:release backend
```

2. Publicar o PostgreSQL com volume persistente e senha forte. Não usar a senha `slot` fora do desenvolvimento.
3. Subir a aplicação com `SPRING_PROFILES_ACTIVE=prod` e as três variáveis de banco (`SLOT_DB_URL`, `SLOT_DB_USER`, `SLOT_DB_PASSWORD`). O proxy (ou o roteador do IBM Cloud Code Engine) termina TLS (`X-Forwarded-Proto: https`) e encaminha a mesma origem.
4. Conferir `GET /api/health`.

O workflow `.github/workflows/backend.yml` roda os testes, constrói a imagem e, no `main`, publica em `ghcr.io/<owner>/slot-backend`.

## Rollback

A imagem anterior permanece tagueada pelo SHA do commit. Para voltar:

1. Parar o container da versão atual.
2. Subir a tag anterior com as mesmas variáveis e o mesmo volume do PostgreSQL.
3. Conferir `GET /api/health` e uma leitura autenticada que já existia antes do deploy.

Não há migração destrutiva depois da V2. Se uma migração futura for incompatível, restaurar o backup anterior antes de subir a imagem antiga.

## Backup

Com o container `slot-postgres` no ar:

```powershell
docker exec slot-postgres pg_dump -U slot -d slot --clean --if-exists > slot-backup.sql
```

Guardar o arquivo fora da máquina da aplicação. Ele contém nomes, e-mails e hashes de senha.

## Restauração

```powershell
docker exec slot-postgres psql -U slot -d postgres -c "CREATE DATABASE slot_restore;"
Get-Content slot-backup.sql | docker exec -i slot-postgres psql -U slot -d slot_restore
```

Conferir a contagem de `organizer`, `availability_day` e `booking`. Para substituir a base ativa, parar a aplicação, restaurar sobre um banco novo e apontar `SLOT_DB_URL` para ele. Só então religar a aplicação.

## Exercício

O exercício local de backup, restauração e rollback está registrado abaixo quando for executado nesta máquina.
