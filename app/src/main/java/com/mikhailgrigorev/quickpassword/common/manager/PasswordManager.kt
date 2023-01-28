package com.mikhailgrigorev.simple_password.common.manager

import android.util.Base64
import com.mikhailgrigorev.simple_password.common.utils.iv
import com.mikhailgrigorev.simple_password.common.utils.salt
import com.mikhailgrigorev.simple_password.common.utils.secretKey
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs
import kotlin.math.max

class PasswordManager {
    private val letters: String = "abcdefghijklmnopqrstuvwxyz"
    private val uppercaseLetters: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers: String = "0123456789"
    private val special: String = "@#=+!£$%&?"
    private val maxPasswordLength: Float = 20F
    private val maxPasswordFactor: Float = 13F

    fun generatePassword(
        isWithLetters: Boolean,
        isWithUppercase: Boolean,
        isWithNumbers: Boolean,
        isWithSpecial: Boolean,
        length: Int
    ): String {

        var result = ""
        var i = 0

        if (isWithLetters)
            result += this.letters
        if (isWithUppercase)
            result += this.uppercaseLetters
        if (isWithNumbers)
            result += this.numbers
        if (isWithSpecial)
            result += this.special

        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(length)

        while (i < length) {
            val randomInt: Int = rnd.nextInt(result.length)
            sb.append(result[randomInt])
            i++
        }

        return sb.toString()
    }

    fun evaluateDate(date: String): Boolean {
        val sdf3 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)

        try {
            val sameDate = sdf3.parse(date)

            val calendar = Calendar.getInstance()
            calendar.time = sameDate!!

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DATE)

            val current = Calendar.getInstance()
            val yearCurrent = current.get(Calendar.YEAR)
            val monthCurrent = current.get(Calendar.MONTH)
            val dayCurrent = current.get(Calendar.DAY_OF_MONTH)

            return when {
                yearCurrent > year -> true
                monthCurrent > month + 4 -> true
                else -> (monthCurrent > month + 3) && (dayCurrent > day)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun evaluatePassword(passwordToTest: String) : Float {

        var factor = 0
        val length = passwordToTest.length

        if (passwordToTest.matches(Regex(".*[" + this.letters + "].*")))
            factor += 3
        if (passwordToTest.matches(Regex(".*[" + this.uppercaseLetters + "].*")))
            factor += 3
        if (passwordToTest.matches(Regex(".*[" + this.numbers + "].*")))
            factor += 3
        if (passwordToTest.matches(Regex(".*[" + this.special + "].*")))
            factor += 4

        if ((passwordToTest.length == 4) and (isNumbers(passwordToTest))
            and (!isLetters(passwordToTest))
            and (!isUpperCase(passwordToTest))
            and (!isSymbols(passwordToTest))
        )
            return 1F


        return (factor * length) / (maxPasswordFactor * max(maxPasswordLength, length.toFloat()))
    }

    fun popularPasswords(passwordToTest: String): Boolean {
        val list = listOf(
        "123456",
        "password",
        "123456789",
        "12345",
        "12345678",
        "qwerty",
        "1234567",
        "111111",
        "1234567890",
        "123123",
        "abc123",
        "1234",
        "password1",
        "iloveyou",
        "1q2w3e4r",
        "000000",
        "qwerty123",
        "zaq12wsx",
        "dragon",
        "sunshine",
        "princess",
        "letmein",
        "654321",
        "monkey",
        "27653",
        "1qaz2wsx",
        "123321",
        "qwertyuiop",
        "superman",
        "asdfghjkl")
        return passwordToTest in list
    }

    fun popularPin(passwordToTest: String) : Boolean {
        var popular = false
        if (passwordToTest.length == 4) {

            if (((abs(passwordToTest[0].code - passwordToTest[1].code) < 2)
                        and (abs(passwordToTest[1].code - passwordToTest[2].code) < 2))
                or
                ((abs(passwordToTest[1].code - passwordToTest[2].code) < 2)
                        and (abs(passwordToTest[2].code - passwordToTest[3].code) < 2))
                or
                ((abs(passwordToTest[0].code - passwordToTest[3].code) < 2)
                        and (abs(passwordToTest[2].code - passwordToTest[3].code) < 2))
                or
                ((abs(passwordToTest[0].code - passwordToTest[1].code) < 2)
                        and (abs(passwordToTest[1].code - passwordToTest[3].code) < 2))
            )
                popular = true
        }

        return popular
    }

    fun evaluatePasswordString(passwordToTest: String) : String {

        var factor = 0
        val length = passwordToTest.length

        if( passwordToTest.matches(Regex(".*[" + this.letters + "].*")) ) { factor += 3 }
        if( passwordToTest.matches(Regex(".*[" + this.uppercaseLetters + "].*")) ){ factor += 3 }
        if( passwordToTest.matches(Regex(".*[" + this.numbers + "].*")) ){ factor += 3 }
        if( passwordToTest.matches(Regex(".*[" + this.special + "].*")) ){ factor += 4 }

        val strong = (factor*length)/(maxPasswordFactor*max(maxPasswordLength, length.toFloat()))

        if((passwordToTest.length == 4) and (isNumbers(passwordToTest))
            and (!isLetters(passwordToTest))
            and (!isUpperCase(passwordToTest))
            and (!isSymbols(passwordToTest)))
            return "high"

        return when {
            strong < 0.33 -> "low"
            strong < 0.66 -> "medium"
            else -> "high"
        }
    }


    fun isLetters(passwordToTest: String) =
            passwordToTest.matches(Regex(".*[" + this.letters + "].*"))

    fun isUpperCase(passwordToTest: String) =
            passwordToTest.matches(Regex(".*[" + this.uppercaseLetters + "].*"))

    fun isNumbers(passwordToTest: String) =
            passwordToTest.matches(Regex(".*[" + this.numbers + "].*"))

    fun isSymbols(passwordToTest: String) =
            passwordToTest.matches(Regex(".*[" + this.special + "].*"))

    fun encrypt(strToEncrypt: String): String? {
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(
                    secretKey.toCharArray(),
                    Base64.decode(salt, Base64.NO_PADDING),
                    10000,
                    256
            )
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt : String) : String? {
        try
        {

            val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        }
        catch (e : Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }

}