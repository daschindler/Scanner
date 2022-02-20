package at.davidschindler.scanner.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The interface for accessing the database with the respective queries
 * */
@Dao
interface ScanObjectDao {

    @Query("SELECT * FROM scan_table ORDER BY uid ASC")
    fun getAllOrderedByID(): List<ScanObject>?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(scanObject: ScanObject)

    @Query("delete from scan_table where uid = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM scan_table")
    fun deleteAll()
}