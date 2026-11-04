package tcc.meu_atelie.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.models.Usuario;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {

    private final Key chave;

    public TokenService() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            secret = "minha_chave_secreta_muito_longa_e_segura_123456";
        }
        this.chave = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("idUsuario", usuario.getIdUsuario())
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(chave)
                .compact();
    }

    public boolean isTokenValido(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(chave).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(chave)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String extrairEmail(String token) {
        return getEmailFromToken(token);
    }

    public boolean tokenValido(String token, Usuario usuario) {
        final String email = extrairEmail(token);
        return (email != null && email.equals(usuario.getEmail()) && isTokenValido(token));
    }
}