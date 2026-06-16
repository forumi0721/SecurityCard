import kr.stonecold.securitycard.core.security.CryptoManager
import kotlinx.coroutines.runBlocking

fun main() {
    val cryptoManager = CryptoManager()
    val encrypted = cryptoManager.encrypt("Test Bank")
    println("Encrypted: \$encrypted")
    val decrypted = cryptoManager.decrypt(encrypted)
    println("Decrypted: \$decrypted")
}
