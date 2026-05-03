package com.wishlistApp.core

object JwtConfig {

    private const val secret = "SUPER_SECRET_KEY_123"
    private const val issuer = "wishlist-app"
    private const val audience = "wishlist-users"

    private const val accessLifetime = 15 * 60 * 1000
    private const val refreshLifetime = 7 * 24 * 60 * 60 * 1000L

    fun generateAccessToken(userId: Int): String {
        return com.auth0.jwt.JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + accessLifetime))
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))
    }

    fun generateRefreshToken(userId: Int): String {
        return com.auth0.jwt.JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + refreshLifetime))
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))
    }

    fun verifier(): com.auth0.jwt.JWTVerifier {
        return com.auth0.jwt.JWT
            .require(com.auth0.jwt.algorithms.Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }
}