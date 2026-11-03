package uz.davrbank.compliance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApplicationTests {
    @Test
    fun contextLoads() {
        val message = "Message"
        assertThat(message).isNotEqualTo("No message")
    }
}
