package io.salad109.medicalofficemanager.auth.internal

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.salad109.medicalofficemanager.auth.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey = run {
        val keyBytes = Base64.getDecoder().decode(jwtProperties.secret)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()

        if (userDetails is CustomUserDetails) {
            claims["userId"] = userDetails.userId
            claims["role"] = userDetails.role
        }

        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiration))
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }
}