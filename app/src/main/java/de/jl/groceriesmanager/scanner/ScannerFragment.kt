package de.jl.groceriesmanager.scanner


import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.BarcodeDetector
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.database.products.Barcode
import de.jl.groceriesmanager.databinding.FragmentScannerBinding
import de.jl.groceriesmanager.dialog.barcode.BarcodeDialogFragment
import de.jl.groceriesmanager.dialog.product.ProductDialogFragment
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import com.google.android.gms.vision.barcode.Barcode as Barcode1

class ScannerFragment : Fragment() {

    private val LOG_TAG = "ScannerFragment"
    private var detector: BarcodeDetector? = null
    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var scannerViewModelFactory: GroceriesManagerViewModelFactory
    private lateinit var scannerBinding: FragmentScannerBinding
    lateinit var application: Application

    private val PHOTO_REQUEST = 10
    private val REQUEST_WRITE_PERMISSION = 20
    private var imageUri: Uri? = null
    private var currImagePath: String? = null
    private var imageFile: File? = null


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
            scannerBinding.scanBarcodeBtn.setOnClickListener {
                openBarcodeScanner()
            }
            detector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode1.EAN_13)
                .build()

            return scannerBinding.root

        } catch (e: Exception) {
            Log.d("ScannerFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    private fun openBarcodeScanner() {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission_group.CAMERA,
                Manifest.permission.CAMERA
            ),
            REQUEST_WRITE_PERMISSION
        )
    }

    private fun setObservers() {
        scannerViewModel.barcode.observe(this, Observer {
            if (it != null) {
                scannerViewModel.validateBarcode()
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
                scannerViewModel.setBarcode(it)
            }
        })

        scannerViewModel.response.observe(this, Observer{ barcode ->
            barcode?.let {
                openBarcodeDialog(it)
            }
        })
    }

    private fun openBarcodeDialog(barcode: Barcode) {
        val dialog =
            BarcodeDialogFragment(barcode)
        fragmentManager?.let {
            dialog.setTargetFragment(this, 0)
            dialog.show(it, "Barcode Dialog")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
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
                            Barcode1.CONTACT_INFO -> Log.i(LOG_TAG, code.contactInfo.title)
                            Barcode1.EMAIL -> Log.i(LOG_TAG, code.email.address)
                            Barcode1.ISBN -> Log.i(LOG_TAG, code.rawValue)
                            Barcode1.PHONE -> Log.i(LOG_TAG, code.phone.number)
                            Barcode1.PRODUCT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode1.SMS -> Log.i(LOG_TAG, code.sms.message)
                            Barcode1.TEXT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode1.URL -> Log.i(LOG_TAG, "url: " + code.url.url)
                            Barcode1.WIFI -> Log.i(LOG_TAG, code.wifi.ssid)
                            Barcode1.GEO -> Log.i(LOG_TAG, code.geoPoint.lat.toString() + ":" + code.geoPoint.lng)
                            Barcode1.CALENDAR_EVENT -> Log.i(LOG_TAG, code.calendarEvent.description)
                            Barcode1.DRIVER_LICENSE -> Log.i(LOG_TAG, code.driverLicense.licenseNumber)
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


    private fun takePicture() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            imageFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var authorities: String = context!!.packageName + ".fileprovider"

        imageUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Uri.fromFile(imageFile)
        } else {
            context?.let { imageFile?.let { it1 -> FileProvider.getUriForFile(it, authorities, it1) } }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivityForResult(intent, PHOTO_REQUEST)
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
}
