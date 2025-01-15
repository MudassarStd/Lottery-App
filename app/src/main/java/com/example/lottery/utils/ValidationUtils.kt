package com.example.lottery.utils

object ValidationUtils {

    /**
     * Validates an email address.
     *
     * @param email The email address to validate.
     * @return True if the email is valid, false otherwise.
     */
    fun isValidEmail(email: String?): Boolean {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates a password.
     *
     * @param password The password to validate.
     * @return True if the password meets the criteria, false otherwise.
     */
    fun isValidPassword(password: String?): Boolean {
        return password != null && password.length >= 6
    }

    /**
     * Validates that the two passwords match.
     *
     * @param password The original password.
     * @param confirmPassword The confirmation password.
     * @return True if both passwords match, false otherwise.
     */
    fun doPasswordsMatch(password: String?, confirmPassword: String?): Boolean {
        return password == confirmPassword
    }

    /**
     * Validates a phone number.
     *
     * @param phone The phone number to validate.
     * @return True if the phone number is valid, false otherwise.
     */
  /**  fun isValidPhone(phone: String?): Boolean {
        return phone != null && phone.length == 11 && android.util.Patterns.PHONE.matcher(phone).matches()
    }*/

    /**
     * Validates that a number is greater than zero.
     *
     * @param number The number to validate.
     * @return True if the number is greater than zero, false otherwise.
     */
    fun isValidPositiveNumber(number: Int?): Boolean {
        return number != null && number > 0
    }

    /**
     * Validates that a text field is not empty.
     *
     * @param text The text to validate.
     * @return True if the text is not empty, false otherwise.
     */
    fun isNotEmpty(text: String?): Boolean {
        return !text.isNullOrBlank()
    }

    /**
     * Validates that the name field contains valid characters (letters and spaces).
     *
     * @param name The name to validate.
     * @return True if the name is valid, false otherwise.
     */
    fun isValidName(name: String?): Boolean {
        // Ensure the name contains only letters (a-z, A-Z) and spaces, with a minimum of 2 characters
        return name != null && name.trim().matches(Regex("^[a-zA-Z\\s]{2,}$"))
    }

    /**
     * Validates that a coin amount is greater than zero.
     *
     * @param amount The coin amount to validate.
     * @return True if the amount is valid (greater than zero), false otherwise.
     */

    fun isValidPhone(phone: String): Boolean {
            val phoneRegex = "^[0-9]{10,13}$".toRegex()
            return phoneRegex.matches(phone)
    }

    fun isValidCoinAmount(amount: Int): Boolean {
        return amount > 0
    }
}
