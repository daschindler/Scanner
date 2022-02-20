package at.davidschindler.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.StateCallback
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.MenuItem
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.davidschindler.scanner.service.CaptureRequestHelper
import at.davidschindler.scanner.service.DataExtractorService
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.coroutines.*


/**
 * This is the activity which shows the camera view and enables scanning the LED for data
 * */
class CameraActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_WIDTH = 1024
        const val DEFAULT_HEIGHT = 768

        private const val TAG = "CameraActivity"
    }

    //Views
    private lateinit var recordButton: ImageView
    private lateinit var textureView: TextureView
    private lateinit var crollerIso: Croller
    private lateinit var crollerExposure: Croller
    private lateinit var tvDisplayCurrentIso: TextView
    private lateinit var tvDisplayCurrentExposure: TextView
    private lateinit var tvDisplayData: TextView


    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var captureRequestHelper: CaptureRequestHelper? = null
    private var imageReader: ImageReader? = null
    private var scope: CoroutineScope? = null

    private lateinit var cameraActivityViewModel: CameraActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textureView = findViewById(R.id.texture)
        textureView.surfaceTextureListener = textureListener

        recordButton = findViewById(R.id.btn_takepicture)
        crollerIso = findViewById(R.id.crollerIso)
        crollerExposure = findViewById(R.id.crollerExposure)
        tvDisplayCurrentExposure = findViewById(R.id.tv_display_current_exposure)
        tvDisplayCurrentIso = findViewById(R.id.tv_display_current_iso)
        tvDisplayData = findViewById(R.id.tv_display_data)

        cameraActivityViewModel = ViewModelProvider(this).get(CameraActivityViewModel::class.java)

        cameraActivityViewModel.isoVal().observe(this, Observer { iso ->
            this.tvDisplayCurrentIso.text = iso.toString()
            captureRequestHelper?.setCaptureRequestISOVal(iso)
            updatePreview()
        })

        cameraActivityViewModel.exposureVal().observe(this, Observer { exposure ->
            this.tvDisplayCurrentExposure.text = String.format(resources.getString(R.string.exposure_time_description),  cameraActivityViewModel.exposureValInMillis())
            captureRequestHelper?.setCaptureRequestExposureVal(exposure)
            updatePreview()
        })

        cameraActivityViewModel.dataText().observe(this, Observer { dataText ->
            tvDisplayData.text = dataText
        })

        recordButton.setOnClickListener {
            cameraActivityViewModel.changeRecordingState()

            if (!cameraActivityViewModel.isRecording()) {
                imageReader?.close()
                recordButton.setImageResource(R.drawable.shutter)
                createCameraPreview()
            } else {
                startRecording()
                recordButton.setImageResource(R.drawable.reload_shutter)
                startExtractionProcess()
            }

        }

        crollerIso.setOnProgressChangedListener { iso ->
            cameraActivityViewModel.setISO(iso)
        }

        crollerExposure.setOnProgressChangedListener { exposure ->
            cameraActivityViewModel.setExposureVal(exposure)
        }
    }

    private fun startExtractionProcess() {
        scope?.cancel()
        scope = scope ?: MainScope()
        scope?.launch(Dispatchers.IO) {
            cameraActivityViewModel.readOutData(DataExtractorService.getBinaryStringBetweenHeaders(cameraActivityViewModel.folderName()))
        }
    }

    private var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e(TAG, "Error when trying to connect camera $errorMsg")
            cameraDevice?.close()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background")
        backgroundThread?.start()
        val backgroundThread = backgroundThread ?: return
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView.surfaceTexture
            texture?.setDefaultBufferSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)
            val surface = Surface(texture)
            val outputConfiguration = OutputConfiguration(surface)

            val previewSessionConfiguration = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                listOf(outputConfiguration),
                this.mainExecutor,
                object : StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // when session ready, start displaying the preview
                        this@CameraActivity.cameraCaptureSession = cameraCaptureSession
                        val cameraDevice = cameraDevice ?: return
                        captureRequestHelper = CaptureRequestHelper(cameraDevice, crollerExposure.progress.toLong(), crollerIso.progress)
                        captureRequestHelper?.addCaptureRequestTarget(surface)
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(
                            this@CameraActivity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            cameraDevice?.createCaptureSession(previewSessionConfiguration)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updatePreview() {
        val captureRequest = captureRequestHelper?.getCurrentCaptureRequest() ?: return
        cameraCaptureSession?.setRepeatingRequest(
            captureRequest,
            null,
            backgroundHandler
        )
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraId = manager.cameraIdList.first()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, MainActivity.PERMISSIONS, 0)
                return
            }
            val cameraId = cameraId ?: return
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startRecording() {
        imageReader = ImageReader.newInstance(DEFAULT_WIDTH, DEFAULT_HEIGHT, ImageFormat.YUV_420_888, 20)
        val readerListener: OnImageAvailableListener =
            OnImageAvailableListener { reader ->
                cameraActivityViewModel.writeLatestImageFromImageReader(reader)
            }

        val imageReader = imageReader ?: return
        imageReader.setOnImageAvailableListener(readerListener, backgroundHandler)

        val surfaceTexture = textureView.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        val previewSurface = Surface(surfaceTexture)
        val readerSurface = imageReader.surface
        captureRequestHelper?.addCaptureRequestTarget(readerSurface)

        val outputConfigurationPreview = OutputConfiguration(previewSurface)
        val outputConfigurationReader = OutputConfiguration(readerSurface)

        val captureStateVideoCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
            }

            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                updatePreview()
            }
        }

        val sessionConfiguration = SessionConfiguration(
            SESSION_REGULAR,
            listOf(outputConfigurationPreview, outputConfigurationReader),
            this@CameraActivity.mainExecutor,
            captureStateVideoCallback
        )

        cameraDevice?.createCaptureSession(sessionConfiguration)
    }

    private fun closeCamera() {
        cameraDevice?.close()
        imageReader?.close()
    }

    private fun prepareForActivityStop() {
        closeCamera()
        stopBackgroundThread()
        scope?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        super.onPause()
        prepareForActivityStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prepareForActivityStop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        prepareForActivityStop()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}