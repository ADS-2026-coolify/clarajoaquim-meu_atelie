#!/bin/sh
# Gera o config.js com a URL da API informada na variável de ambiente API_URL
echo "window.APP_CONFIG = { apiUrl: '${API_URL:-http://localhost:8080}' };" > /usr/share/nginx/html/config.js
exec nginx -g 'daemon off;'
