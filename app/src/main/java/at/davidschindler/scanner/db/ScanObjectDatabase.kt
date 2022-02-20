package at.davidschindler.scanner.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/**
 * The database implementation which only provides one db element
 * */
@Database(entities = arrayOf(ScanObject::class), version = 1, exportSchema = false)
public abstract class ScanObjectDatabase : RoomDatabase() {
    abstract fun scanObjectDao(): ScanObjectDao

    companion object {
        @Volatile
        private var INSTANCE: ScanObjectDatabase? = null

        fun getDatabase(context: Context): ScanObjectDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ScanObjectDatabase::class.java,
                        "scan_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}