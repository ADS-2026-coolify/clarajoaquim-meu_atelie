# Passo a passo: GitHub + GitHub Actions + Coolify

## Parte 1 — Subir o projeto no repositório da sala

1. Clone o repositório da sala (se ainda não tiver):
   ```bash
   git clone https://github.com/ORGANIZACAO/REPOSITORIO-DA-SALA.git
   cd REPOSITORIO-DA-SALA
   ```
2. Copie **o conteúdo** da pasta `meu-atelie-deploy` (backend, frontend, .github, docker-compose.yml, README.md, .gitignore, .env.example, PASSO_A_PASSO_DEPLOY.md) para dentro do repositório clonado.
   Atenção: a pasta `.github` é oculta; confira que ela foi copiada.
3. Confirme que nenhum segredo vai junto (não existe `.env` na pasta, e a senha do banco não está mais no código):
   ```bash
   git status
   ```
4. Envie para o GitHub:
   ```bash
   git add .
   git commit -m "Adiciona back-end e front-end do Meu Atelie"
   git push origin main
   ```
   Se o repositório usar outra branch (por exemplo `master`), troque `main` por ela aqui e em `.github/workflows/deploy.yml`.

> O primeiro push já dispara o workflow. Os jobs de build vão rodar, e o job de **deploy vai falhar** até você concluir a Parte 3. Isso é esperado.

## Parte 2 — Criar os recursos no Coolify

### 2.1 Banco de dados
1. No Coolify: **Projects → (seu projeto) → + New → Database → PostgreSQL**.
2. Defina nome do banco, usuário e senha e clique em **Start**.
3. Copie a **conexão interna** (host é o nome do container). Você vai montar a `DB_URL`:
   `jdbc:postgresql://NOME-DO-CONTAINER:5432/NOME-DO-BANCO`

### 2.2 Back-end (API)
1. **+ New → Application →** *Public Repository* (ou *Private Repository* com GitHub App / Deploy Key, se o repositório for privado).
2. Cole a URL do repositório e escolha a branch `main`.
3. **Build Pack: Dockerfile**. **Base Directory: `/backend`**. **Port: `8080`**.
4. Em **Environment Variables**, cadastre:
   | Nome | Valor |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://NOME-DO-CONTAINER:5432/NOME-DO-BANCO` |
   | `DB_USERNAME` | usuário do banco |
   | `DB_PASSWORD` | senha do banco |
   | `JWT_SECRET` | texto aleatório com **32+ caracteres** |
   | `CORS_ALLOWED_ORIGINS` | URL pública do front (ex.: `https://app.seudominio.com`) |
5. Em **Persistent Storage**, adicione um volume com **Destination Path `/app/uploads`** (guarda as imagens dos produtos).
6. Em **Domains**, defina o domínio da API (ex.: `https://api.seudominio.com`).
7. Desmarque **Auto Deploy** (quem vai disparar o deploy é o GitHub Actions).
8. Clique em **Deploy** uma primeira vez e confira nos logs a mensagem `Started MeuAtelieApplication`.

### 2.3 Front-end
1. **+ New → Application →** mesmo repositório e branch.
2. **Build Pack: Dockerfile**. **Base Directory: `/frontend`**. **Port: `80`**.
3. Variável de ambiente (**somente em tempo de execução**, não marque como "Build Variable"):
   | Nome | Valor |
   |---|---|
   | `API_URL` | URL pública da API (ex.: `https://api.seudominio.com`) |
4. **Domains**: defina o domínio do front (ex.: `https://app.seudominio.com`).
5. Desmarque **Auto Deploy** e clique em **Deploy**.

> Os dois domínios precisam estar corretos um no outro: o front aponta para a API (`API_URL`) e a API libera o front (`CORS_ALLOWED_ORIGINS`). Se aparecer erro de CORS no navegador, é aqui.

## Parte 3 — Ligar o GitHub Actions ao Coolify

1. No Coolify: **Settings → Advanced → habilite "API Access"**.
2. **Keys & Tokens → API Tokens → Create**, com permissão de **deploy**. Copie o token (ele só aparece uma vez).
3. Descubra o **UUID** de cada aplicação: abra a aplicação e olhe a URL do navegador (`.../application/<UUID>`).
4. No GitHub: **Repositório → Settings → Secrets and variables → Actions → New repository secret**. Crie:
   | Secret | Valor |
   |---|---|
   | `COOLIFY_URL` | endereço do seu Coolify, sem barra no final (ex.: `https://coolify.seudominio.com`) |
   | `COOLIFY_TOKEN` | token criado no passo 2 |
   | `COOLIFY_BACKEND_UUID` | UUID da aplicação do back-end |
   | `COOLIFY_FRONTEND_UUID` | UUID da aplicação do front-end |
5. Em **Actions**, reexecute o workflow (**Re-run all jobs**) ou faça um novo push.

A partir daí, todo `push` na branch `main` faz: **build do back → build do front → deploy no Coolify**.

## Como testar
- Abra o domínio do front, faça o cadastro de um usuário e login.
- Cadastre um produto com imagem e confira que ela aparece (valida o volume `/app/uploads`).

## Problemas comuns
| Sintoma | Causa provável |
|---|---|
| Erro de CORS no navegador | `CORS_ALLOWED_ORIGINS` diferente do domínio real do front |
| Front chama `localhost:8080` | `API_URL` não definida (ou marcada como Build Variable) |
| API não sobe: `password authentication failed` | `DB_USERNAME`/`DB_PASSWORD`/`DB_URL` incorretos |
| Job de deploy falha com 401/403 | token sem permissão de deploy, ou "API Access" desativado |
| Job de deploy falha com 404 | UUID errado em `COOLIFY_BACKEND_UUID`/`COOLIFY_FRONTEND_UUID` |
| Imagens somem após novo deploy | faltou o volume persistente em `/app/uploads` |
| Usuários são deslogados após trocar a chave | `JWT_SECRET` foi alterada (os tokens antigos deixam de valer) |
| Segurança fraca do token | se `JWT_SECRET` faltar ou tiver menos de 32 caracteres, o sistema usa uma chave padrão do código. Sempre defina a variável. |
