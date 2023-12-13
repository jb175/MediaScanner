package fr.isep.mediascanner.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.google.zxing.integration.android.IntentIntegrator
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.model.api.ProductItem
import fr.isep.mediascanner.model.api.ProductResponse
import fr.isep.mediascanner.service.ProductService
import fr.isep.mediascanner.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference

class ScanFragment : Fragment() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private val SCAN_REQUEST_CODE = 203

    private var activityRef: WeakReference<Activity>? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activityRef = WeakReference(context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scanButton: Button = view.findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            if (checkCameraPermission()) {
                startBarcodeScanner()
            } else {
                requestCameraPermission()
            }
        }

        val upc: EditText = view.findViewById(R.id.upc)
        val button: Button = view.findViewById(R.id.submit_button)
        button.setOnClickListener {
            requestProductDetails(upc.text.toString())
            upc.text.clear()
        }
    }

    fun startBarcodeScanner() {

        val activity = activityRef?.get()
        if (activity != null) {
            val integrator = IntentIntegrator(activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Scan a barcode")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(false)
            val intent = integrator.createScanIntent()
            activity.startActivityForResult(intent, SCAN_REQUEST_CODE)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val activity = activityRef?.get()
        if (activity != null) {
            return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun requestCameraPermission() {
        val activity = activityRef?.get()
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun requestProductDetails (scannedData: String) {
        //debug
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        //end debug

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.upcitemdb.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val productService = retrofit.create(ProductService::class.java)

        val call = productService.getProductInfo(scannedData)

        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                val activity = activityRef?.get()
                if (response.isSuccessful && activity != null) {
                    Log.println(Log.INFO, "ScanResult", response.body().toString())
                    val productResponse: ProductResponse? = response.body()
                    if ((productResponse?.total != null) && (productResponse.total > 0)) {
                        val productItem: ProductItem? = productResponse.items?.get(0)
                        if (productItem != null) {
                            //start new activity
                            val intent = Intent(activity, ProductDetailsActivity::class.java)
                            intent.putExtra("PRODUCT_ITEM", productItem)
                            startActivity(intent)
                        } else {
                            Log.println(Log.WARN, "ScanResult", "No product item found (2)")

                            val toast = Toast.makeText(activity.applicationContext, "No product item found (2)", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                            toast.show()
                        }
                    } else {
                        Log.println(Log.WARN, "ScanResult", "No product item found")

                        val toast = Toast.makeText(activity.applicationContext, "No product item found", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                        toast.show()
                    }
                } else {
                    Log.println(Log.WARN, "ScanResult", String.format("Request has failed, error code", response.code()))
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
}