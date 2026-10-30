package com.huaweimanager.app.data.crypto

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HuaweiCrypto {

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun sha256Base64(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /**
     * Standard HiLink login password generator:
     * Base64(SHA256(username + Base64(SHA256(password)) + token))
     */
    fun createHiLinkPassword(username: String, password: String, token: String): String {
        val pwSha256Base64 = sha256Base64(password)
        val combined = "$username$pwSha256Base64$token"
        return sha256Base64(combined)
    }

    /**
     * Compute SCRAM-SHA-256 Client Proof for modern Huawei routers (e.g., Orbit Star 2/B312/B535)
     */
    fun computeScramClientProof(
        password: String,
        saltHex: String,
        iterations: Int,
        authMsg: String
    ): String {
        try {
            val saltBytes = hexStringToByteArray(saltHex)
            val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, 256)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val saltedPassword = skf.generateSecret(spec).encoded

            val clientKey = hmacSha256(saltedPassword, "Client Key".toByteArray(StandardCharsets.UTF_8))
            val storedKey = MessageDigest.getInstance("SHA-256").digest(clientKey)
            val clientSignature = hmacSha256(storedKey, authMsg.toByteArray(StandardCharsets.UTF_8))

            val clientProof = ByteArray(clientKey.size)
            for (i in clientKey.indices) {
                clientProof[i] = (clientKey[i].toInt() xor clientSignature[i].toInt()).toByte()
            }
            return clientProof.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            return ""
        }
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
