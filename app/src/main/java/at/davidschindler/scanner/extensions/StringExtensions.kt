package at.davidschindler.scanner.extensions

/**
 * This extension function XOR's two strings with each other, needed for error correction
 */
fun String.xor(string: String): String {
    var ans = ""

    // Loop to iterate over the
    // Binary Strings
    for (i in string.indices) {
        // If the Character matches
        ans += if (this[i] == string[i]) "0" else "1"
    }
    return ans
}