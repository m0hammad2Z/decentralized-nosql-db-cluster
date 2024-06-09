package org.dbsim.node.util.security;

import org.dbsim.node.exception.InvalidTokenException;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.util.GlobalVar;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


public class JwtUtil {
    private static final String SECRET = GlobalVar.SECRET;

    @Autowired
    private MainNode mainNode;


    // Generate token with username, expiration time, and role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("nodeId", mainNode.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + GlobalVar.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // Get username from token
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // Get role from token
    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // Get node ID from token
    public int getNodeId(String token) {
        return (int) parseClaims(token).get("nodeId");
    }

    // Validate token - combines checks and throws specific exceptions
    public void validateToken(String token) throws InvalidTokenException {
        try {
            Claims claims = parseClaims(token);
            validateClaimPresence(claims);
            validateRole(getRole(token));
            validateNodeId(getNodeId(token));
            validateExpiration(claims.getExpiration());
        } catch (SignatureException e) {
            throw new InvalidTokenException("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("Invalid JWT token");
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("JWT claims string is empty");
        }
    }

    // Resolve token from header
    public String resolveToken(String header) {
        return header.replace("Bearer ", "");
    }


    // Helper method to parse claims with consistent secret key
    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }

    // Helper method to validate claim presence
    private void validateClaimPresence(Claims claims) throws InvalidTokenException {
        if (claims.getSubject() == null) {
            throw new InvalidTokenException("Invalid JWT token (missing claim: " + "username" + ")");
        }
        if (claims.get("role") == null) {
            throw new InvalidTokenException("Invalid JWT token (missing claim: " + "role" + ")");
        }

        if (claims.get("nodeId") == null) {
            throw new InvalidTokenException("Invalid JWT token (missing claim: " + "nodeId" + ")");
        }
    }

    // Helper method to validate role
    private  void validateRole(String role) throws InvalidTokenException {
        if (!role.equals("ADMIN") && !role.equals("USER")) {
            throw new InvalidTokenException("Invalid JWT token (invalid role)");
        }
    }


    // Helper method to node ID
    private void validateNodeId(int nodeId) throws InvalidTokenException {
        if (nodeId != mainNode.getId()) {
            throw new InvalidTokenException("Invalid JWT token (invalid node ID)");
        }
    }

    // Helper method to validate expiration
    private void validateExpiration(Date expiration) throws InvalidTokenException {
        if (expiration.before(new Date())) {
            throw new InvalidTokenException("Expired JWT token");
        }
    }
}

