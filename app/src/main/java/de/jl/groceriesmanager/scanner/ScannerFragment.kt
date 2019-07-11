package de.jl.groceriesmanager.scanner


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.jl.groceriesmanager.R
import de.jl.groceriesmanager.databinding.ScannerFragmentBinding

class ScannerFragment : Fragment() {

    private lateinit var scannerViewModel: ScannerViewModel
    private lateinit var scannerViewModelFactory: ScannerViewModelFactory
    private lateinit var scannerBinding: ScannerFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        try{
            //Binding
            scannerBinding = DataBindingUtil.inflate(inflater, R.layout.scanner_fragment, container, false)

            //ViewModelFactory
            //mit Parameter:
            //scannerViewModelFactory = ScannerViewModelFactory(ScannerFragmentArgs.fromBundle(arguments!!).score)
            //Ohne Parameter:
            scannerViewModelFactory = ScannerViewModelFactory()
            //ViewModel
            scannerViewModel = ViewModelProviders.of(this, scannerViewModelFactory).get(ScannerViewModel::class.java)

            return scannerBinding.root

        } catch(e: Exception){
            Log.d("ScannerFragment", "Error ${e.localizedMessage}")
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.scanner_fragment, container, false)
    }
}
