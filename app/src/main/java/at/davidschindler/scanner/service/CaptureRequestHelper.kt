package at.davidschindler.scanner.service

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.util.Range
import android.view.Surface

/**
 * This class helps managing the CaptureRequest
 */
class CaptureRequestHelper(cameraDevice: CameraDevice, exposureVal: Long = DEFAULT_EXPOSURE_VAL, isoValue: Int = DEFAULT_ISO) {
    companion object {
        private const val DEFAULT_EXPOSURE_VAL: Long = 125000
        private const val DEFAULT_ISO = 100
    }

    private var captureRequestBuilder: CaptureRequest.Builder? = null

    init {
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        captureRequestBuilder?.set(
            CaptureRequest.CONTROL_AWB_MODE,
            CaptureRequest.CONTROL_AWB_MODE_AUTO
        )

        captureRequestBuilder?.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(30, 30))
        setCaptureRequestExposureVal(exposureVal)
        setCaptureRequestISOVal(isoValue)
    }

    fun setCaptureRequestISOVal(isoValue: Int) {
        captureRequestBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue)
    }

    fun setCaptureRequestExposureVal(exposureVal: Long) {
        captureRequestBuilder?.set(
            CaptureRequest.SENSOR_EXPOSURE_TIME,
            exposureVal
        )
    }

    fun getCurrentCaptureRequest(): CaptureRequest? {
        return captureRequestBuilder?.build()
    }

    fun addCaptureRequestTarget(outputTarget: Surface) {
        captureRequestBuilder?.addTarget(outputTarget)
    }
}