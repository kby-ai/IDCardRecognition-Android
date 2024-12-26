package com.kbyai.idcardrecognition

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kbyai.idsdk.IDSDK
import org.json.JSONObject
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    companion object {
        private val SELECT_PHOTO_REQUEST_CODE = 1
        private val IDCARD_RECOGNITION_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ret = IDSDK.setActivation(
            "kUWgfFn5kv5ZeJ659tvyW3poZO2xb5llqL/se3g69rVmUJdOuUp8lyAwoeiZe+6e6PeER97sw4zL\n" +
                    "/rMR0qYVp0nupe6W7TzgQHtjq119BJKVDuxXjbQEyuM29nSEeRVYSwO2htOUEF/V1IH9BIfo33Vc\n" +
                    "sAg9ohvVuB5DT9BKHXQKevmEmE2AXba2t6ponKAUQ6VwWIX+w1NON8A+6hCoSIDlzVkacbsAp6Kg\n" +
                    "B0Abfqzbqhz8GG5WNPZlonx7XRNwKlYU+sPo/X6noy1gr7iAAhC+KpWetD2KOWsunyAswkZ5cg2Y\n" +
                    "WK5/HQXG+h2oGSkjrZj9zH7kGbzzDbn+2qT2jA=="
        )
        if(ret  == IDSDK.SDK_SUCCESS) {
            ret = IDSDK.init(this)
        }

        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            startActivityForResult(Intent(this, CameraActivityKt::class.java), IDCARD_RECOGNITION_REQUEST_CODE)
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_PICK)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO_REQUEST_CODE)
        }

        findViewById<Button>(R.id.buttonAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                var bitmap: Bitmap = Utils.getCorrectlyOrientedImage(this, data?.data!!)
                val result = IDSDK.idcardRecognition(bitmap)
                if(result != null) {
                    var jsonResult = JSONObject(result)
                    if(jsonResult.get(getString(R.string.documentName)) != "Unknown") {
                        ResultActivity.resultString = result

                        val intent = Intent(this, ResultActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Failed to recognition!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to recognition!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                //handle exception
                e.printStackTrace()
            }
        }
    }
}