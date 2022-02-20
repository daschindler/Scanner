package at.davidschindler.scanner.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This is the object stored in the database
 *
 * @param uid The id of the db element
 * @param convertedData The converted data: for example "david"...
 * @param binaryData The raw binary data: for example "0110010010101x1001100001101010100000011011011110110x1100....."
 * */
@Entity(tableName = "scan_table")
data class ScanObject(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "converted_data") val convertedData: String,
    @ColumnInfo(name = "binary_data") val binaryData: String
)