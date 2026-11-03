package uz.davrbank.compliance.integration.davrhub

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@Service
class DavrHubTokenStore {
    private val storedToken = AtomicReference<StoredToken?>()

    fun getToken(): DavrHubAccessToken? {
        val token = storedToken.get() ?: return null
        if (!token.isValidAt(Instant.now())) {
            storedToken.compareAndSet(token, null)
            return null
        }

        return token.accessToken
    }

    fun storeToken(accessToken: DavrHubAccessToken, ttl: Duration) {
        val effectiveTtl = if (ttl.isNegative || ttl.isZero) Duration.ofSeconds(1) else ttl
        storedToken.set(
            StoredToken(
                accessToken = accessToken,
                expiresAt = Instant.now().plus(effectiveTtl),
            )
        )
    }

    fun evictToken() {
        storedToken.set(null)
    }

    private data class StoredToken(
        val accessToken: DavrHubAccessToken,
        val expiresAt: Instant,
    ) {
        fun isValidAt(now: Instant): Boolean = expiresAt.isAfter(now)
    }
}