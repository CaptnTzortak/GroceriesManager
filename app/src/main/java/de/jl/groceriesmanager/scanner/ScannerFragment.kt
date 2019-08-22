package de.jl.groceriesmanager.scanner

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.BarcodeDetector
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.Product
import de.jl.groceriesmanager.databinding.FragmentScannerBinding
import de.jl.groceriesmanager.dialog.barcode.BarcodeDialogFragment
import de.jl.groceriesmanager.scanner.ScannerFragment.Companion.REQUEST_CAMERA_PERMISSION
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import com.google.android.gms.vision.barcode.Barcode as Barcodel


class ScannerFragment : Fragment(), View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var scannerViewModelFactory: GroceriesManagerViewModelFactory
    private lateinit var scannerBinding: FragmentScannerBinding
    lateinit var application: Application

    private var imageUri: Uri? = null
    private var currImagePath: String? = null
    private var imageFile: File? = null

    private var detector: BarcodeDetector? = null


    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    companion object {
        val ORIENTATIONS = SparseIntArray()
        const val FRAGMENT_DIALOG = "dialog"
        const val LOG_TAG = "ScannerFragment"
        const val REQUEST_CAMERA_PERMISSION = 1
        const val PHOTO_REQUEST = 10
        const val REQUEST_WRITE_PERMISSION = 20
    }

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    /**
     * Tag for the [Log].
     */
    private val TAG = "ScannerFragment"

    /**
     * Camera state: Showing camera preview.
     */
    private val STATE_PREVIEW = 0

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private val STATE_WAITING_LOCK = 1

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private val STATE_WAITING_PRECAPTURE = 2

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private val STATE_WAITING_NON_PRECAPTURE = 3

    /**
     * Camera state: Picture was taken.
     */
    private val STATE_PICTURE_TAKEN = 4

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_WIDTH = 1920

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_HEIGHT = 1080


    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}

    }

    /**
     * ID of the current [CameraDevice].
     */
    private var mCameraId: String? = null

    /**
     * A [CameraCaptureSession] for camera preview.
     */
    private var mCaptureSession: CameraCaptureSession? = null

    /**
     * A reference to the opened [CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * The [android.util.Size] of camera preview.
     */
    private var mPreviewSize: Size? = null

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
     */
    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            val activity = activity
            activity?.finish()
        }

    }


    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var mBackgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var mBackgroundHandler: Handler? = null

    /**
     * An [ImageReader] that handles still image capture.
     */
    private var mImageReader: ImageReader? = null

    /**
     * This is the output file for our picture.
     */
    private var mFile: File? = null

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        mBackgroundHandler!!.post(
            mFile?.let {
                ImageSaver(
                    reader.acquireNextImage(),
                    it
                )
            }
        )
    }

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private lateinit var mPreviewRequestBuilder: CaptureRequest.Builder

    /**
     * [CaptureRequest] generated by [.mPreviewRequestBuilder]
     */
    private var mPreviewRequest: CaptureRequest? = null

    /**
     * The current state of camera state for taking pictures.
     *
     * @see .mCaptureCallback
     */
    private var mState = STATE_PREVIEW

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val mCameraOpenCloseLock = Semaphore(1)

    /**
     * Whether the current camera device supports Flash or not.
     */
    private var mFlashSupported: Boolean = false

    /**
     * Orientation of the camera sensor
     */
    private var mSensorOrientation: Int = 0

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureProgressed(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest,
            @NonNull partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest,
            @NonNull result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    /**
     * Shows a [Toast] on the UI thread.
     *
     * @param text The message to show
     */
    private fun showToast(text: String) {
        val activity = activity
        activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
    }

    /**
     * Given `choices` of `Size`s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     * class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
        choices: Array<Size>, textureViewWidth: Int,
        textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * h / w
            ) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            return choices[0]
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.common_scanner)

            //Binding
            scannerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_scanner, container, false)

            //Application
            application = requireNotNull(this.activity).application

            //ViewModelFactory
            scannerViewModelFactory = GroceriesManagerViewModelFactory(application)
            //ViewModel
            scannerViewModel = ViewModelProviders.of(this, scannerViewModelFactory).get(ScannerViewModel::class.java)

            setObservers()

            scannerBinding.lifecycleOwner = this
            scannerBinding.viewModel = scannerViewModel
            scannerBinding.picture.setOnClickListener(this)

            //EAN-8 (64) + EAN-13 (32)
            detector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcodel.EAN_13 + Barcodel.EAN_8).build()

            return scannerBinding.root

        } catch (e: Exception) {
            Log.d("ScannerFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    private fun setObservers() {
        scannerViewModel.barcodeString.observe(this, Observer {
            if (it != null) {
                scannerViewModel.validateBarcode(it)
            }
        })

        scannerViewModel.valid.observe(this, Observer {
            it?.let {
                if (it) {
                    scannerViewModel.getData()
                }
            }
        })

        scannerViewModel.scannedBarcode.observe(this, Observer {
            if (it == null) {
                //TODO: Cleanup
            } else {
                scannerViewModel.validateBarcode(it)
            }
        })

        scannerViewModel.response.observe(this, Observer { product ->
            product?.let {
                openBarcodeDialog(it)
            }
        })
    }

    fun test() {
        val bitmap = context?.let { decodeBitmapUri(it, imageUri) }
        if (detector!!.isOperational && bitmap != null) {
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val barcodes = detector!!.detect(frame)
            if(barcodes == null || barcodes.size() == 0){
                showToast("No Barcode found.")
            } else {
                for (index in 0 until barcodes.size()) {
                    val code = barcodes.valueAt(index)
                    Log.i(TAG, "Barcode found: ${code.displayValue}")
                    scannerViewModel.setScannedBarcode(code.displayValue)
                }
            }
        }
    }

    private fun openBarcodeDialog(product: Product) {
        val dialog =
            BarcodeDialogFragment(product)
        fragmentManager?.let {
            dialog.setTargetFragment(this, 0)
            dialog.show(it, "Barcode Dialog")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            launchMediaScanIntent(mediaScanIntent)
            try {
                val bitmap = context?.let { decodeBitmapUri(it, imageUri) }
                if (detector!!.isOperational && bitmap != null) {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val barcodes = detector!!.detect(frame)
                    for (index in 0 until barcodes.size()) {
                        val code = barcodes.valueAt(index)
                        scannerViewModel.setScannedBarcode(code.displayValue)
                        val type = barcodes.valueAt(index).valueFormat
                        when (type) {
                            Barcodel.CONTACT_INFO -> Log.i(LOG_TAG, code.contactInfo.title)
                            Barcodel.EMAIL -> Log.i(LOG_TAG, code.email.address)
                            Barcodel.ISBN -> Log.i(LOG_TAG, code.rawValue)
                            Barcodel.PHONE -> Log.i(LOG_TAG, code.phone.number)
                            Barcodel.PRODUCT -> Log.i(LOG_TAG, code.rawValue)
                            Barcodel.SMS -> Log.i(LOG_TAG, code.sms.message)
                            Barcodel.TEXT -> Log.i(LOG_TAG, code.rawValue)
                            Barcodel.URL -> Log.i(LOG_TAG, "url: " + code.url.url)
                            Barcodel.WIFI -> Log.i(LOG_TAG, code.wifi.ssid)
                            Barcodel.GEO -> Log.i(LOG_TAG, code.geoPoint.lat.toString() + ":" + code.geoPoint.lng)
                            Barcodel.CALENDAR_EVENT -> Log.i(LOG_TAG, code.calendarEvent.description)
                            Barcodel.DRIVER_LICENSE -> Log.i(LOG_TAG, code.driverLicense.licenseNumber)
                            else -> Log.i(LOG_TAG, code.rawValue)
                        }
                    }
                    if (barcodes.size() == 0) {
                        scannerViewModel.scanFailed()
                    }
                } else {
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load Image", Toast.LENGTH_SHORT)
                    .show()
                Log.e(LOG_TAG, e.toString())
            }

        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = File(Environment.getExternalStorageDirectory(), "picture.jpg")
        if (!storageDir.exists()) {
            storageDir.parentFile.mkdirs()
            storageDir.createNewFile()
        }
        currImagePath = storageDir.absolutePath
        return storageDir
    }


    private fun launchMediaScanIntent(mediaScanIntent: Intent) {
        activity?.sendBroadcast(mediaScanIntent)
    }

    @Throws(FileNotFoundException::class)
    private fun decodeBitmapUri(ctx: Context, uri: Uri?): Bitmap? {

        val targetW = 600
        val targetH = 600
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true

        BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri), null, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeStream(
            ctx.contentResolver
                .openInputStream(uri), null, bmOptions
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mFile = File(activity!!.getExternalFilesDir(null), "pic.jpg")
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (scannerBinding.texture.isAvailable) {
            openCamera(scannerBinding.texture!!.width, scannerBinding.texture.height)
        } else {
            scannerBinding.texture.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance("Request Permission")
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        //OLD
        //when (requestCode) {
        //    REQUEST_WRITE_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //        takePicture()
        //    } else {
        //        Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        //    }
        //}
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val activity = activity
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // For still image captures, we use the largest available size.
                val largest = Collections.max(
                    Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                mImageReader = ImageReader.newInstance(
                    largest.width, largest.height,
                    ImageFormat.JPEG, /*maxImages*/2
                )
                mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler)

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = activity.windowManager.defaultDisplay.rotation

                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true
                    }
                    else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
                }

                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y

                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest
                )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    scannerBinding.texture.setAspectRatio(
                        mPreviewSize!!.width, mPreviewSize!!.height
                    )
                } else {
                    scannerBinding.texture.setAspectRatio(
                        mPreviewSize!!.height, mPreviewSize!!.width
                    )
                }

                // Check if the flash is supported.
                val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mFlashSupported = available ?: false

                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance("Camera Error")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }

    }

    /**
     * Opens the camera specified by [Camera2BasicFragment.mCameraId].
     */
    private fun openCamera(width: Int, height: Int) {
        if (activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } !== PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val activity = activity
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }

    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession!!.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader!!.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = scannerBinding.texture.surfaceTexture!!

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice?.createCaptureSession(
                Arrays.asList(surface, mImageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == mCameraDevice) {
                            return
                        }

                        // When the session is ready, we start displaying the preview.
                        mCaptureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            mPreviewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Flash is automatically enabled when necessary.
                            setAutoFlash(mPreviewRequestBuilder)

                            // Finally, we start displaying the camera preview.
                            mPreviewRequest = mPreviewRequestBuilder.build()
                            mCaptureSession!!.setRepeatingRequest(
                                mPreviewRequest,
                                mCaptureCallback, mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onConfigureFailed(
                        @NonNull cameraCaptureSession: CameraCaptureSession
                    ) {
                        showToast("Failed")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity = activity
        if (null == mPreviewSize || null == activity) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize!!.getHeight(),
                viewWidth.toFloat() / mPreviewSize!!.getWidth()
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
       } else if (Surface.ROTATION_180 == rotation) {
           matrix.postRotate(180f, centerX, centerY)
       } else {
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize!!.getHeight(),
                viewWidth.toFloat() / mPreviewSize!!.getWidth()
            )
            matrix.postScale(scale, scale, centerX, centerY)
        }
        scannerBinding.texture.setTransform(matrix)
    }

    /**
     * Initiate a still image capture.
     */
    private fun takePicture() {
        lockFocus()
        //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //try {
        //    imageFile = createImageFile()
        //} catch (e: IOException) {
        //    e.printStackTrace()
        //}

        //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        //if (intent.resolveActivity(activity!!.packageManager) != null) {
        //    startActivityForResult(intent, PHOTO_REQUEST)
        //}

    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            mCaptureSession!!.capture(
                mPreviewRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.mCaptureCallback] from [.lockFocus].
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE
            mCaptureSession!!.capture(
                mPreviewRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.mCaptureCallback] from both [.lockFocus].
     */
    private fun captureStillPicture() {
        try {
            val activity = activity
            if (null == activity || null == mCameraDevice) {
                return
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader?.surface)

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            setAutoFlash(captureBuilder)

            // Orientation
            val rotation = activity.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(
                    @NonNull session: CameraCaptureSession,
                    @NonNull request: CaptureRequest,
                    @NonNull result: TotalCaptureResult
                ) {
                    var authorities: String = context!!.packageName + ".fileprovider"
                    imageUri =
                        context?.let { mFile?.let { it1 -> FileProvider.getUriForFile(it, authorities, it1) } }
                    test()

                    Log.i(TAG, "Saved: $mFile")
                    Log.d(TAG, mFile.toString())
                    unlockFocus()
                }
            }

            mCaptureSession?.stopRepeating()
            mCaptureSession?.abortCaptures()
            mCaptureSession?.capture(captureBuilder.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private fun getOrientation(rotation: Int): Int {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(mPreviewRequestBuilder)
            mCaptureSession!!.capture(
                mPreviewRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler
            )
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW
            mCaptureSession!!.setRepeatingRequest(
                mPreviewRequest, mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    override fun onClick(view: View) {
        takePicture()
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (mFlashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }
}

/**
 * Saves a JPEG [Image] into the specified [File].
 */
private class ImageSaver internal constructor(
    /**
     * The JPEG image
     */
    private val mImage: Image,
    /**
     * The file we save the image into.
     */
    private val mFile: File
) : Runnable {

    override fun run() {
        val buffer = mImage.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(mFile)
            output.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mImage.close()
            if (null != output) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

}


/**
 * Compares two `Size`s based on their areas.
 */
internal class CompareSizesByArea : Comparator<Size> {

    override fun compare(lhs: Size, rhs: Size): Int {
        // We cast here to ensure the multiplications won't overflow
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }

}


/**
 * Shows an error message dialog.
 */
class ErrorDialog : DialogFragment() {

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        return AlertDialog.Builder(activity)
            .setMessage(arguments!!.getString(ARG_MESSAGE))
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ -> activity!!.finish() }
            .create()
    }

    companion object {

        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String): ErrorDialog {
            val dialog = ErrorDialog()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            dialog.arguments = args
            return dialog
        }
    }

}


/**
 * Shows OK/Cancel confirmation dialog about camera permission.
 */
class ConfirmationDialog : DialogFragment() {

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent = parentFragment
        return AlertDialog.Builder(activity)
            .setMessage("Request Permission")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                parent!!.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { _, _ ->
                val activity = parent!!.activity
                activity?.finish()
            }
            .create()
    }
}