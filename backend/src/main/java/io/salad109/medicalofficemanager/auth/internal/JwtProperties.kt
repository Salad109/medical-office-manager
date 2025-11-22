package io.salad109.medicalofficemanager.auth.internal

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    var secret: String = ""
    var expiration: Long = 0
}