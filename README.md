# Meu Ateliê — Back-end + Front-end

Sistema web de apoio à gestão de encomendas e controle de custos para ateliês artesanais (TCC · ADS · FEMA).

```
backend/    API REST — Java 21 + Spring Boot + PostgreSQL
frontend/   SPA — Angular 19 (servido por Nginx)
.github/workflows/deploy.yml   CI (build) + deploy no Coolify
docker-compose.yml             teste local de tudo junto
```

## Variáveis de ambiente

### Back-end
| Variável | Para quê | Exemplo |
|---|---|---|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://HOST:5432/meu_atelie` |
| `DB_USERNAME` / `DB_PASSWORD` | credenciais do banco | |
| `JWT_SECRET` | chave de assinatura do token (**mín. 32 caracteres**) | gere uma aleatória |
| `CORS_ALLOWED_ORIGINS` | URL(s) do front, separadas por vírgula | `https://meuatelie.exemplo.com` |
| `DDL_AUTO` | (opcional) `update` cria/atualiza as tabelas | padrão `update` |

> As imagens enviadas ficam em `/app/uploads`: configure um **volume persistente** nesse caminho.

### Front-end
| Variável | Para quê | Exemplo |
|---|---|---|
| `API_URL` | URL pública da API (lida ao iniciar o container) | `https://api.meuatelie.exemplo.com` |

## Rodar localmente com Docker
```bash
cp .env.example .env      # edite os valores
docker compose -f docker-compose.local.yml up --build
# front: http://localhost:4200   API: http://localhost:8080
```

## Deploy
Veja o passo a passo completo no arquivo `PASSO_A_PASSO_DEPLOY.md`.
