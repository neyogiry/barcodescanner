package com.neyogiry.android.barcodescanner

import android.hardware.Camera
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.common.base.Objects
import com.neyogiry.android.barcodescanner.barcodedetection.BarcodeProcessor
import com.neyogiry.android.barcodescanner.camera.CameraSource
import com.neyogiry.android.barcodescanner.camera.CameraSourcePreview
import com.neyogiry.android.barcodescanner.camera.GraphicOverlay
import com.neyogiry.android.barcodescanner.camera.WorkflowModel
import java.io.IOException

open class BarcodeScannerView(private val activity: FragmentActivity) {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var workflowModel: WorkflowModel? = null
    private var currentWorkflowState: WorkflowModel.WorkflowState? = null
    private var barcodeResult: BarcodeResult? = null

    fun onCreate(preview: CameraSourcePreview, graphicOverlay: GraphicOverlay) {
        this.preview = preview
        this.graphicOverlay = graphicOverlay.apply {
            cameraSource = CameraSource(this)
        }

        setUpWorkflowModel()
    }

    fun onResume() {
        workflowModel?.markCameraFrozen()
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
        workflowModel?.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
    }

    fun onPause() {
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    fun onDestroy() {
        cameraSource?.release()
        cameraSource = null
    }

    fun setFlash(flag: Boolean) {
        cameraSource?.updateFlashMode(if (flag) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF)
    }

    fun getFlash(): Boolean {
        return cameraSource?.getFlash() ?: false
    }

    fun toggleFlash() {
        cameraSource?.toggleFlash()
    }

    fun setBarcodeResult(result: BarcodeResult) {
        barcodeResult = result
    }

    private fun startCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start camera preview!", e)
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            preview?.stop()
        }
    }

    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProviders.of(activity).get(WorkflowModel::class.java)

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(activity, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")

            when (workflowState) {
                WorkflowModel.WorkflowState.DETECTING -> {
                    startCameraPreview()
                }
                WorkflowModel.WorkflowState.CONFIRMING -> {
                    startCameraPreview()
                }
                WorkflowModel.WorkflowState.SEARCHING -> {
                    stopCameraPreview()
                }
                WorkflowModel.WorkflowState.DETECTED, WorkflowModel.WorkflowState.SEARCHED -> {
                    stopCameraPreview()
                }
            }

        })

        workflowModel?.detectedBarcode?.observe(activity!!, Observer { barcode ->
            barcode?.let {
                it.rawValue?.let { it ->
                    Log.i(TAG, "Value $it")
                    barcodeResult?.onBarcodeResult(it)
                }
            }
        })
    }

    interface BarcodeResult {
        fun onBarcodeResult(value: String)
    }

    companion object {
        private const val TAG = "BarcodeScannerView"
    }

}