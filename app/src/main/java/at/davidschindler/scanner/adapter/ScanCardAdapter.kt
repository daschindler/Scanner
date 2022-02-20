package at.davidschindler.scanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.davidschindler.scanner.R
import at.davidschindler.scanner.db.ScanObject
import at.davidschindler.scanner.db.ScanObjectDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * This is the adapter for the recyclerview used in MainActivity
 *
 * @param scanList A list of the saved scanobjects
 * */
class ScanCardAdapter(private val scanList: ArrayList<ScanObject>) : RecyclerView.Adapter<ScanCardAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_cell, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanObject = scanList[position]

        holder.tvConvertedString.text = scanObject.convertedData
        holder.tvBinaryString.text = scanObject.binaryData

        holder.tvBinaryString.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context).apply {
                setTitle(scanObject.convertedData)
                setMessage(scanObject.binaryData)
                setPositiveButton("Delete"){_,_->
                    removeElement(position, scanObject, holder.itemView.context)
                }
                setNeutralButton("Cancel"){_,_->}
            }.create().show()
        }

        holder.ivDeleteScan.setOnClickListener {
            removeElement(position, scanObject, holder.itemView.context)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return scanList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tvConvertedString: TextView = itemView.findViewById(R.id.tv_cell_converted_data)
        val tvBinaryString: TextView = itemView.findViewById(R.id.tv_cell_binary_data)
        val ivDeleteScan: ImageView = itemView.findViewById(R.id.iv_cell_delete)
    }

    private fun removeElement(position: Int, scanObject: ScanObject, context: Context) {
        scanList.removeAt(position)
        this.notifyItemRemoved(position)
        ScanObjectDatabase.getDatabase(context).scanObjectDao().deleteById(scanObject.uid)
    }

}
