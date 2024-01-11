package fr.isep.mediascanner.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.model.api.ProductItem
import fr.isep.mediascanner.model.api.ProductResponse
import fr.isep.mediascanner.service.ProductService
import fr.isep.mediascanner.R
import fr.isep.mediascanner.RequestCodes
import fr.isep.mediascanner.activity.productDetails.ScanProductProductDetailsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScanFragment : Fragment() {


    private lateinit var barcodeView : DecoratedBarcodeView
    private lateinit var scanButton : Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeView = requireView().findViewById(R.id.zxing_barcode_scanner)
        barcodeView.initializeFromIntent(Intent().apply {
            putExtra(Intents.Scan.CAMERA_ID, 0)
            putExtra(Intents.Scan.BEEP_ENABLED, false)
            putExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
            putExtra(Intents.Scan.PROMPT_MESSAGE, "")
        })
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                stopScan()
                Log.println(Log.INFO, "ScanResult", "Scanned: " + result.result.text)
                requestProductDetails(result.result.text)
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
            }
        })

        scanButton = view.findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            startScan()
        }

        val upc: EditText = view.findViewById(R.id.upc)
        val button: Button = view.findViewById(R.id.submit_button)
        button.setOnClickListener {
            requestProductDetails(upc.text.toString())
            upc.text.clear()
        }
    }

    fun requestProductDetails(scannedData: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.upcitemdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val productService = retrofit.create(ProductService::class.java)

        Log.println(Log.INFO, "ScanResult", "&scannedData")
        val call = productService.getProductInfo(scannedData)

        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (context is MainActivity) {
                    if (response.isSuccessful) {
                        Log.println(Log.INFO, "ScanResult", response.body().toString())
                        val productResponse: ProductResponse? = response.body()
                        if ((productResponse?.total != null) && (productResponse.total > 0)) {
                            val productItem: ProductItem? = productResponse.items?.get(0)
                            if (productItem != null) {
                                val intent = Intent(context as MainActivity, ScanProductProductDetailsActivity::class.java)
                                intent.putExtra("PRODUCT_ITEM", productItem)
                                startActivity(intent)
                            } else {
                                Log.println(Log.WARN, "ScanResult", "No product item found (2)")

                                val toast = Toast.makeText(
                                    (context as MainActivity).applicationContext,
                                    R.string.scan_toast_noProductFound,
                                    Toast.LENGTH_SHORT
                                )
                                toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                                toast.show()
                            }
                        } else {
                            Log.println(Log.WARN, "ScanResult", "No product item found")

                            val toast = Toast.makeText(
                                (context as MainActivity).applicationContext,
                                R.string.scan_toast_noProductFound,
                                Toast.LENGTH_SHORT
                            )
                            toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                            toast.show()
                        }
                    } else {
                        Log.println(Log.WARN, "ScanResult", String.format("Request has failed, error code", response.code()))

                        val toast = Toast.makeText(
                            (context as MainActivity).applicationContext,
                            R.string.scan_toast_notAValidBarcode,
                            Toast.LENGTH_SHORT
                        )
                        toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                        toast.show()
                    }
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                val sb: java.lang.StringBuilder = StringBuilder(String.format("Request has failed %s", t.message))
                for (stacktrace in t.stackTrace) {
                    sb.append(String.format("\n\t%s", stacktrace.toString()))
                }
                Log.println(Log.ERROR, "ScanResult", sb.toString())
            }
        })
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context as Activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), RequestCodes.CAMERA_PERMISSION_REQUEST_CODE)
    }

    fun startScan() {
        if (checkCameraPermission()) {
            barcodeView.visibility = View.VISIBLE
            barcodeView.resume()
            scanButton.setOnClickListener {
                stopScan()
            }
            scanButton.text = ContextCompat.getString(requireContext(), R.string.mainMenu_scan_buttonStopScan)
            scanButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorFalseButton))
        } else {
            requestCameraPermission()
        }
    }

    fun stopScan() {
        barcodeView.visibility = View.INVISIBLE
        barcodeView.pause()
        scanButton.setOnClickListener {
            startScan()
        }
        scanButton.text = ContextCompat.getString(requireContext(), R.string.mainMenu_scan_buttonScan)
        scanButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorButton))
    }
}