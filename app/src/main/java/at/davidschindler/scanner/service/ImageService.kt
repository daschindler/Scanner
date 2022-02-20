package at.davidschindler.scanner.service

import android.graphics.Bitmap
import android.graphics.Color
import android.media.ImageReader
import at.davidschindler.scanner.CameraActivity
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * This imageservice includes methods to create a bitmap from a bytebuffer or get only luminance
 * values of a bytebuffer. Also it offers possibilites to write the bitmaps
 */
class ImageService {
    companion object {
        private fun createGreyscaleBitmapFromBuffer(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
            val greyscale = decodeGreyscale(buffer, width, height)
            return Bitmap.createBitmap(greyscale, width, height, Bitmap.Config.ARGB_8888)
        }

        private fun decodeGreyscale(nv21: ByteBuffer, width: Int, height: Int): IntArray {
            val pixelCount = width * height
            val out = IntArray(pixelCount)
            for (i in 0 until pixelCount) {
                try {
                    val luminance = (nv21[i].toInt() and 0xFF)
                    out[i] = Color.argb(0xFF, luminance, luminance, luminance)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return out
        }

        fun createLuminanceArray(nv21: ByteBuffer, width: Int, height: Int): IntArray {
            val pixelCount = width * height
            val luminanceArray = IntArray(pixelCount)
            for (i in 0 until pixelCount) {
                try {
                    val luminance = (nv21[i].toInt() and 0xFF)
                    luminanceArray[i] = luminance
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return luminanceArray
        }

        fun writeLatestImageFromImageReader(rdr: ImageReader, folderName: String, imNr: Int) {
            val image = rdr.acquireLatestImage() ?: return
            val yBuffer = image.planes[0].buffer

            val bitmap = ImageService.createGreyscaleBitmapFromBuffer(yBuffer,
                CameraActivity.DEFAULT_WIDTH,
                CameraActivity.DEFAULT_HEIGHT
            )
            val file = File(
                PathCreatorService.getPathToDownloadsDir(),
                PathCreatorService.createJpgPathFromName("/$folderName/image$imNr")
            ).also { file -> file.parentFile.mkdirs() }
            file.writeBitmap(bitmap, Bitmap.CompressFormat.JPEG, 80)

            image.close()
        }

        private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
            outputStream().use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
        }
    }
}