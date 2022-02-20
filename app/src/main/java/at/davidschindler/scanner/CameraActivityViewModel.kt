package at.davidschindler.scanner

import android.app.Application
import android.media.ImageReader
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.davidschindler.scanner.db.ScanObject
import at.davidschindler.scanner.db.ScanObjectDatabase
import at.davidschindler.scanner.service.DataExtractorService
import at.davidschindler.scanner.service.ImageService
import at.davidschindler.scanner.service.TimeService

class CameraActivityViewModel(application: Application) : AndroidViewModel(application) {
    private var exposureVal = MutableLiveData<Long>()
    private val isoVal = MutableLiveData<Int>()
    private var dataText = MutableLiveData<String>()
    private var isRecording = false
    private var imageNumber = 0
    private var folderName = ""

    fun setISO(iso: Int) {
        if (!isRecording()) {
            this.isoVal.value = iso
        }
    }

    fun isoVal(): LiveData<Int> {
        return isoVal
    }

    fun setExposureVal(exposure: Int) {
        if (!isRecording) {
            this.exposureVal.value = exposure.toLong()
        }
    }

    fun setExposureVal(exposure: Long) {
        this.exposureVal.value = exposure
    }

    fun exposureVal(): LiveData<Long> {
        return this.exposureVal
    }

    fun changeRecordingState() {
        this.isRecording = this.isRecording.not()
        if (isRecording) {
            imageNumber = 0
            folderName = "scanner" + TimeService.getUniqueDateTimeString()
        }
    }

    fun folderName(): String {
        return folderName
    }

    fun isRecording(): Boolean {
        return this.isRecording
    }

    fun writeLatestImageFromImageReader(reader: ImageReader) {
        ImageService.writeLatestImageFromImageReader(reader, folderName, imageNumber)
        imageNumber++
    }

    fun exposureValInMillis(): Long? {
        return exposureVal.value?.div(1000)
    }

    fun dataText(): LiveData<String> {
        return dataText
    }

    fun readOutData(croppedDataString: String) {
        val context = getApplication<Application>().applicationContext
        val symbolString = DataExtractorService.extractSentSymbolStringFromBinaryCode(croppedDataString)
        dataText.postValue(symbolString)
        ScanObjectDatabase.getDatabase(context).scanObjectDao()
            .insert(ScanObject(0, symbolString, croppedDataString))

    }
}