package fr.isep.mediascanner.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.activity.MainActivity
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

class ScanFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scanButton: Button = view.findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            if (context is MainActivity) {
                (context as MainActivity).startScan()
            }
        }

        val upc: EditText = view.findViewById(R.id.upc)
        val button: Button = view.findViewById(R.id.submit_button)
        button.setOnClickListener {
            requestProductDetails(upc.text.toString())
            upc.text.clear()
        }
    }

    fun requestProductDetails(scannedData: String) {
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
                                //start new activity
                                val intent = Intent(context as MainActivity, ProductDetailsActivity::class.java)
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
                        Log.println(
                            Log.WARN,
                            "ScanResult",
                            String.format("Request has failed, error code", response.code())
                        )

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
}