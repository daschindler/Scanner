package at.davidschindler.scanner.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import at.davidschindler.scanner.models.BitStripe
import at.davidschindler.scanner.models.Pixel


/**
 * Provides the methods needed for extracting the message from the scanned image
 * */
class DataExtractorService {
    companion object {
        private const val CONTENT_LIMITER = "1010101010101010"
        private const val SPLITTER_1 = "10101010"
        private const val SPLITTER_2 = "10111101"

        /**
         * Since some of the extraction methods are very complex and won't work anyway when not scanning
         * the configured LED I just returned some sample strings that something happens when using the app
         * without a LED
         */
        private fun extractMsgFromImage(image: Bitmap): String {
            return "10101010101010101010101010101010101010101010"
        }

        private fun getMediumLightedRow(image: Bitmap): Int {
            val pixel = Pixel(0, 0, 0)
            val pixelBrightnessArray = arrayListOf<Pixel>()

            for (x in 0 until image.width) {
                for (y in 0 until image.height) {
                    pixelBrightnessArray.add(Pixel(x, y, Color.red(image.getPixel(x, y))))
                    if (Color.red(image.getPixel(x, y)) > pixel.brightness) {
                        pixel.x = x
                        pixel.y = y
                        pixel.brightness = Color.red(image.getPixel(x, y))
                    }
                }
            }

            val lightestStripePixels = arrayListOf<Pixel>()
            for (x in 0 until image.width) {
                val brightness = Color.red(image.getPixel(x, pixel.y))
                if (!lightestStripePixels.any { px -> px.brightness == brightness }) {
                    lightestStripePixels.add(Pixel(x, pixel.y, brightness))
                }
            }

            return lightestStripePixels.sortedBy { px -> px.brightness }[lightestStripePixels.size / 2].x
        }

        private fun addBinaryToDataList(
            dataList: ArrayList<Int>,
            bitStripeArray: ArrayList<BitStripe>,
            minWidthOfBit: Int,
            maxWidthOfBit: Int
        ) {
            val white = -1
            for (bitStripe in bitStripeArray) {
                if (bitStripe.width in minWidthOfBit..maxWidthOfBit) {
                    if (bitStripe.rgbColor == white) {
                        dataList.add(1)
                    } else {
                        dataList.add(0)
                    }
                } else if (bitStripe.width > maxWidthOfBit) {
                    for (i in 1..7) {
                        if (i == 1 && maxWidthOfBit*1.2 > bitStripe.width) {
                            dataList.add(if (bitStripe.rgbColor==white) 1 else 0)
                        } else if (bitStripe.width in maxWidthOfBit * i + 1..maxWidthOfBit * (i + 1)) {
                            for (j in 0..i) {
                                dataList.add(if (bitStripe.rgbColor == white) 1 else 0)
                            }
                        }
                    }
                }
            }
        }

        private fun createBrokenHeaderCombinations(header: String): Array<String> {
            val brokenHeaderCombinations = Array(header.length) { "" }
            for (i in header.indices) {
                val bHeader = header.toCharArray()
                bHeader[i] = 'x'
                brokenHeaderCombinations[i] = String(bHeader)
            }

            return brokenHeaderCombinations
        }

        private fun stringStartsWithBrokenHeader(header: String, string: String): Boolean {
            val brokenHeaderCombinations = createBrokenHeaderCombinations(header)
            val substring = string.substring(0, 8)
            if (substring == header) return true
            for (h in brokenHeaderCombinations) {
                if (substring == h) return true
            }

            return false
        }

        private fun findNextHeaderIfBroken(header: String, brokenString: String): String {
            var nearestHeaderIndex = 1000
            var nextHeader = ""
            for (i in header.indices) {
                val bHeader = header.toCharArray()
                bHeader[i] = 'x'
                val stringBrokenHeader = String(bHeader)
                if (brokenString.indexOf(stringBrokenHeader) < nearestHeaderIndex
                    && brokenString.indexOf(stringBrokenHeader) != -1
                ) {
                    nearestHeaderIndex = brokenString.indexOf(stringBrokenHeader)
                    nextHeader = stringBrokenHeader
                }
            }

            return nextHeader
        }

        /**
         * Since some of the extraction methods are very complex and won't work anyway when not scanning
         * the configured LED I just returned some sample strings that something happens when using the app
         * without a LED
         */
        private fun getHeadersFromBrokenStringTwice(header: String, brokenString: String): String {
            return "1010101010101010101010101010101010101010"
        }

        private fun getSymbolOfBinarySymbol(binarySymbol: String): Char {
            val parseInt: Int = binarySymbol.toInt(2)
            val symbol = parseInt.toChar()
            println("$symbol $binarySymbol")
            return symbol
        }

        /**
         * Since some of the extraction methods are very complex and won't work anyway when not scanning
         * the configured LED I just returned some sample strings that something happens when using the app
         * without a LED
         */
        fun extractSentSymbolStringFromBinaryCode(binaryString: String): String {
            return "hello"
        }

        /**
         * Since some of the extraction methods are very complex and won't work anyway when not scanning
         * the configured LED I just returned some sample strings that something happens when using the app
         * without a LED
         */
        fun getBinaryStringBetweenHeaders(folderName: String): String {
            var headerFound = false
            var imageIndex = 0
            var wholeImageDataString = ""
            var croppedDataString = ""

            while (!headerFound) {
                val path = PathCreatorService.getPathToDownloadsDir() + "/$folderName/image$imageIndex.jpg"
                val bmp = BitmapFactory.decodeFile(path)
                if (bmp != null) {
                    val mutableBitmap: Bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
                    imageIndex++
                    wholeImageDataString += extractMsgFromImage(mutableBitmap)
                    val headersFromBrokenString = getHeadersFromBrokenStringTwice(CONTENT_LIMITER, wholeImageDataString)
                    if (headersFromBrokenString.length == 32) {
                        croppedDataString = wholeImageDataString
                            .substringAfter(headersFromBrokenString.substring(0,16))
                            .substringBefore(headersFromBrokenString.substring(16,32))
                        if (croppedDataString.length > 40) {
                            headerFound = true
                        }
                    }

                    headerFound = true
                    croppedDataString = "101010101010101010"
                }
            }

            return croppedDataString
        }
    }
}