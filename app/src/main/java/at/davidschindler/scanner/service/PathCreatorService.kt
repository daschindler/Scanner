package at.davidschindler.scanner.service

import android.os.Environment

/**
 * Provides methods for finding an unique path to store an image to
 * */
class PathCreatorService {
    companion object {
        private var latestPath: String? = null

        fun getLatestPath(): String {
            return latestPath ?: createJpgPathFromName(TimeService.getUniqueDateTimeString())
        }

        fun getPathToDownloadsDir(): String {
            return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString()
        }

        fun createJpgPathFromName(name: String) : String {
            latestPath = "/$name.jpg"
            return "/$name.jpg"
        }
    }
}