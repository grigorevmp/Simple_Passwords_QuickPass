package com.mikhailgrigorev.quickpass
import java.security.SecureRandom
import kotlin.math.max

class PasswordManager {
    private val letters : String = "abcdefghijklmnopqrstuvwxyz"
    private val uppercaseLetters : String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers : String = "0123456789"
    private val special : String = "@#=+!£$%&?"
    private val maxPasswordLength : Float = 20F //Max password length that my app creates
    private val maxPasswordFactor : Float = 13F //Max password factor based on chars inside password

    fun generatePassword(isWithLetters: Boolean,
                         isWithUppercase: Boolean,
                         isWithNumbers: Boolean,
                         isWithSpecial: Boolean,
                         length: Int) : String {

        var result = ""
        var i = 0

        if(isWithLetters){ result += this.letters }
        if(isWithUppercase){ result += this.uppercaseLetters }
        if(isWithNumbers){ result += this.numbers }
        if(isWithSpecial){ result += this.special }

        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(length)

        while (i < length) {
            val randomInt : Int = rnd.nextInt(result.length)
            sb.append(result[randomInt])
            i++
        }

        return sb.toString()
    }

    fun evaluatePassword(passwordToTest: String) : Float {

        var factor = 0
        val length = passwordToTest.length

        if( passwordToTest.matches( Regex(".*["+this.letters+"].*") ) ) { factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.uppercaseLetters+"].*") ) ){ factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.numbers+"].*") ) ){ factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.special+"].*") ) ){ factor += 4 }

        if((passwordToTest.length == 4) and (isNumbers(passwordToTest))
            and (!isLetters(passwordToTest))
            and (!isUpperCase(passwordToTest))
            and (!isSymbols(passwordToTest)))
            return 1F


        return (factor*length)/(maxPasswordFactor*max(maxPasswordLength, length.toFloat()))
    }

    fun evaluatePasswordString(passwordToTest: String) : String {

        var factor = 0
        val length = passwordToTest.length

        if( passwordToTest.matches( Regex(".*["+this.letters+"].*") ) ) { factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.uppercaseLetters+"].*") ) ){ factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.numbers+"].*") ) ){ factor += 3 }
        if( passwordToTest.matches( Regex(".*["+this.special+"].*") ) ){ factor += 4 }

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


    fun isLetters(passwordToTest: String) : Boolean{
        return passwordToTest.matches( Regex(".*["+this.letters+"].*") )
    }

    fun isUpperCase(passwordToTest: String) : Boolean{
        return passwordToTest.matches( Regex(".*["+this.uppercaseLetters+"].*") )
    }

    fun isNumbers(passwordToTest: String) : Boolean{
        return passwordToTest.matches( Regex(".*["+this.numbers+"].*") )
    }

    fun isSymbols(passwordToTest: String) : Boolean{
        return passwordToTest.matches( Regex(".*["+this.special+"].*") )
    }
}