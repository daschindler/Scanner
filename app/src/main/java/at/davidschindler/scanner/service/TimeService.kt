package at.davidschindler.scanner.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Provides a string which includes a detailed current datetime and has "image" in front of it
 * */
class TimeService {
    companion object {
        fun getUniqueDateTimeString() : String {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")

            return "image" + current.format(formatter)
        }
    }
}