package at.davidschindler.scanner

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.davidschindler.scanner.adapter.ScanCardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * This is the entrance view which as well includes the history list
 * */
class MainActivity : AppCompatActivity() {
    companion object {
        val PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    private lateinit var btnScan: FloatingActionButton
    private lateinit var tvNoMessagesReceived: TextView
    private lateinit var rvMessages: RecyclerView

    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main)

        rvMessages = findViewById(R.id.rv_messages)
        tvNoMessagesReceived = findViewById(R.id.tv_no_messages_received)
        btnScan = findViewById(R.id.btn_scan)

        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        btnScan.setOnClickListener {
            if (!hasPermissions()) {
                requestPermission()
            } else {
                openCameraActivity()
            }
        }

        if (mainActivityViewModel.scansList().isNullOrEmpty()) {
            rvMessages.visibility = View.GONE
            tvNoMessagesReceived.visibility = View.VISIBLE
        } else {
            rvMessages.layoutManager = GridLayoutManager(applicationContext, 2)
            val adapter = ScanCardAdapter(mainActivityViewModel.scansList())
            rvMessages.adapter = adapter
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun hasPermissions(): Boolean{
        return (ContextCompat.checkSelfPermission(this, PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, PERMISSIONS, 0)
    }

}
