package hcmute.edu.vn.finalprojectdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import hcmute.edu.vn.finalprojectdemo.databinding.FragmentCameraBinding
import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID
import hcmute.edu.vn.finalprojectdemo.model.Item
import hcmute.edu.vn.finalprojectdemo.singleton.ResultHolder
import hcmute.edu.vn.finalprojectdemo.singleton.ResultUpdateListener
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener, ResultUpdateListener {

    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var imgFind: ImageView

    private val REQUEST_IMAGE_CAPTURE = 1

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ResultHolder.setListener(this)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        imgFind = view.findViewById(R.id.imgFind)

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        imgFind.setOnClickListener {
            val results: String = ResultHolder.results
            var query = results.trim()
            if (!query.isEmpty()) {
                val url = "https://www.google.com/search?q=$query&tbm=isch"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            // Save history
            val database = FirebaseDatabase.getInstance("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/")
            val historyRef = database.getReference("Users/"+GlobalUserID.GlobalUserID.toString()+"/history")

            var item = Item()
            item.setResourceImage("https://firebasestorage.googleapis.com/v0/b/finalproject-mopr-demo.appspot.com/o/images%2Fic_camera.png?alt=media&token=4dc94859-9543-4ba0-ade3-f49007acd125")
            item.setTxtImage(query)


            historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var nextKey: Int = 0

                    // Get the last key in the "history" node
                    for (childSnapshot in dataSnapshot.children) {
                        val key = childSnapshot.key?.toIntOrNull()
                        if (key != null && key > nextKey) {
                            nextKey = key
                        }
                    }

                    // Increment the last key to get the next key
                    nextKey++

                    val historyItemRef = historyRef.child(nextKey.toString())
                    historyItemRef.setValue(item)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imgView = requireView().findViewById<ImageView>(R.id.imageView)
            imgView.setImageBitmap(imageBitmap)
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        detectObjects(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        activity?.runOnUiThread {
//            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
//                String.format("%d ms", inferenceTime)

            // Pass necessary information to OverlayView for drawing on the canvas
            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )
            // Update the results in ResultHolder
            val drawableText = results?.getOrNull(0)?.categories?.getOrNull(0)?.label ?: ""
            ResultHolder.updateResults(drawableText)

            // Force a redraw
            fragmentCameraBinding.overlay.invalidate()

        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResultUpdated(results: String) {
        // Handle the updated results here
//        val trimmedResults = results.trim()
//        if (!trimmedResults.isEmpty()) {
//            val query = ResultHolder.results
//            val url = "https://www.google.com/search?q=$query&tbm=isch"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
//        }
    }
}