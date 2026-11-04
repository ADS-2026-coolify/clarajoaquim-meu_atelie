# Passo a passo: GitHub + GitHub Actions + Coolify (Docker Compose)

O `docker-compose.yml` da raiz sobe **banco + back-end + front-end** em um único recurso do Coolify.
O Coolify gera sozinho: domínios com HTTPS, usuário e senha do banco. Você só informa o `JWT_SECRET`.

## Parte 1 — Atualizar o repositório

Copie para o seu repositório (sobrescrevendo) estes arquivos da pasta `meu-atelie-deploy`:
`docker-compose.yml`, `docker-compose.local.yml` (novo), `.github/workflows/deploy.yml`, `README.md` e `PASSO_A_PASSO_DEPLOY.md`.

```bash
git add .
git commit -m "Ajusta docker compose para o Coolify"
git push origin main
```

## Parte 2 — Criar o recurso no Coolify

Se você já criou um recurso em modo Dockerfile, **apague-o** (Configuration → Danger Zone → Delete) para não sobrar configuração antiga.

1. No projeto/ambiente: **+ New → Application →** escolha o repositório (Public Repository) e a branch `main`.
2. Em **Build Pack**, escolha **Docker Compose**.
3. **Base Directory:** `/`   **Docker Compose Location:** `/docker-compose.yml`
4. Clique em **Continue**.
5. Se aparecer a janela "Import environment variables" (do `.env.example`), **feche sem importar**.
6. Abra **Environment Variables**. Devem aparecer as variáveis do compose; preencha:
   | Variável | Valor |
   |---|---|
   | `JWT_SECRET` | texto aleatório com **32+ caracteres** (veja abaixo como gerar) |

   As variáveis `SERVICE_*` (usuário/senha do banco e URLs) o Coolify gera sozinho. Não precisa preencher.

   Para gerar a `JWT_SECRET` no PowerShell:
   ```powershell
   [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
   ```
7. Abra a aba **Configuration** (a principal) e localize os campos de domínio:
   - **Domains for frontend:** o domínio do front (ex.: `https://meuatelie.SEU-DOMINIO`). Use o gerado pelo Coolify se não tiver um.
   - **Domains for backend:** o domínio da API (ex.: `https://api.SEU-DOMINIO`).
   O front usa o domínio do back como `API_URL`, e o back libera o domínio do front no CORS. Isso é automático.
8. Desmarque **Auto Deploy** (o GitHub Actions vai disparar o deploy).
9. Clique em **Save** e depois em **Deploy**.
10. Acompanhe os **Logs** do `backend` até aparecer `Started MeuAtelieApplication`.

> O primeiro deploy demora (build do Maven e do Angular).
> Se alterar domínio ou variável depois, clique em **Redeploy**.

## Parte 3 — Ligar o GitHub Actions ao Coolify

1. No Coolify: **Settings → Advanced → habilite "API Access"**.
2. **Keys & Tokens → API Tokens → Create**, com permissão de **deploy**. Copie o token (aparece uma vez).
3. Copie o **UUID** do recurso: abra a aplicação e olhe a URL do navegador (`.../application/<UUID>`).
4. No GitHub: **Repositório → Settings → Secrets and variables → Actions → New repository secret**. Crie:
   | Secret | Valor |
   |---|---|
   | `COOLIFY_URL` | endereço do seu Coolify, sem barra no final |
   | `COOLIFY_TOKEN` | token criado no passo 2 |
   | `COOLIFY_APP_UUID` | UUID do recurso (passo 3) |
5. Em **Actions**, use **Re-run all jobs** (ou faça um novo push).

A partir daí, todo `push` na `main` faz: **build do back → build do front → deploy no Coolify**.

## Como testar
- Abra o domínio do front, cadastre um usuário e faça login.
- Cadastre um produto com imagem e confira que ela aparece (valida o volume de uploads).

## Problemas comuns
| Sintoma | Causa provável |
|---|---|
| Erro de CORS no navegador | domínio do front mudou depois do deploy: faça **Redeploy** |
| Front chama `localhost:8080` | domínio do back não definido na aba Configuration; defina e faça **Redeploy** |
| Back não sobe: `password authentication failed` | volume do banco antigo com outra senha: apague o volume `db_data` (Storages) e faça Redeploy |
| `JWT_SECRET` vazia | variável não preenchida no passo 2.6 |
| Deploy do Actions falha 401/403 | token sem permissão de deploy ou "API Access" desativado |
| Deploy do Actions falha 404 | `COOLIFY_APP_UUID` errado |
| Imagens somem após novo deploy | o volume `uploads_data` precisa existir (já está no compose); não apague em Storages |
