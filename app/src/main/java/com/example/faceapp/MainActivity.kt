package com.example.faceapp


import android.graphics.Bitmap
import android.graphics.Camera
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.faceapp.Helper.GraphicOverlay
import com.example.faceapp.Helper.RectOverlay
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.wonderkiln.camerakit.*
import dmax.dialog.SpotsDialog


class MainActivity : AppCompatActivity() {

    lateinit var waitingDialog: android.app.AlertDialog
    lateinit var cameraView: CameraView
    lateinit var graphicOverlay: GraphicOverlay

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById<CameraView>(R.id.camera_view)
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("Please wait...")
            .setCancelable(false)
            .build()

        var detect = findViewById<Button>(R.id.detect_btn)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.graphic_overlay)
        detect.setOnClickListener {
            cameraView.start()
            cameraView.captureImage()
            graphicOverlay.clear()
        }

        cameraView.addCameraKitListener(object:CameraKitEventListener{
            override fun onEvent(p0: CameraKitEvent?) {

            }

            override fun onError(p0: CameraKitError?) {

            }

            override fun onImage(p0: CameraKitImage?) {
                waitingDialog.show()

                var bitmap = p0?.bitmap
                bitmap = bitmap?.let { Bitmap.createScaledBitmap(it,cameraView.width,cameraView.height,false) }
                cameraView.stop()
                
                runFaceDetector(bitmap)

            }

            override fun onVideo(p0: CameraKitVideo?) {

            }

        })

    }

    private fun runFaceDetector(bitmap: Bitmap?) {
        val image = bitmap?.let { FirebaseVisionImage.fromBitmap(it) }
        val options = FirebaseVisionFaceDetectorOptions.Builder().build()
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        if (image != null) {
            detector.detectInImage(image)
                .addOnSuccessListener { result -> processFaceResult(result) }
                .addOnFailureListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
        }

    }

    private fun processFaceResult(result: List<FirebaseVisionFace>) {

        var count = 0
        for (face in result) {
            val bounds = face.boundingBox
            val rectOverlay = RectOverlay(graphicOverlay,bounds)
            graphicOverlay.add(rectOverlay)

            count++
        }

        waitingDialog.dismiss()
        Toast.makeText(this, String.format("Detected %d faces in picture", count), Toast.LENGTH_SHORT).show()

    }


}