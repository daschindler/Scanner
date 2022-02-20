package at.davidschindler.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import at.davidschindler.scanner.db.ScanObject
import at.davidschindler.scanner.db.ScanObjectDatabase

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    fun scansList(): ArrayList<ScanObject> {
        val scansList = ArrayList<ScanObject>()
        ScanObjectDatabase.getDatabase(getApplication<Application>().applicationContext).scanObjectDao().getAllOrderedByID()?.forEach { scanObject ->
            scansList.add(ScanObject(scanObject.uid, scanObject.convertedData, scanObject.binaryData))
        }
        return scansList
    }
}