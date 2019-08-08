package de.jl.groceriesmanager.scanner


import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.GroceriesManagerViewModelFactory
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.FragmentScannerBinding

class ScannerFragment : Fragment() {

    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var scannerViewModelFactory: GroceriesManagerViewModelFactory
    private lateinit var scannerBinding: FragmentScannerBinding
    lateinit var application: Application

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

            return scannerBinding.root

        } catch (e: Exception) {
            Log.d("ScannerFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false)
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
    }
}
