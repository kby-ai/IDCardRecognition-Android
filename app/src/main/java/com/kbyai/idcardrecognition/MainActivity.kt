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
            "ItbU8EMHg1dFLQgkk7h4IP+Zu/CkrfVWGEEmOY5IjiAmxvcEU1fqJ3C76+CoZ2zit816tchptZDn\n" +
                    "61TCAtHBKVV6Fub3tmoyHl8kJz4pOMX4OQ2qUEhyvI2WzRN8/FUZ6ZAdUGw/4I3SJfTxdxD55MJ3\n" +
                    "/rOt5C8OLJvR8sBTx+ltt9J2hGMO+T5Jf1ndrj00djN6/v5PtcQqFJiDQTbVdFyCm6E4w66Mgx63\n" +
                    "FWtYRgBd1qC3f9FFeFvICppd2BvEsuaw2n/8/6qDj6X1kTdYgYRtRD5VKPTnmAV04sV90EA/3Hm5\n" +
                    "xLeVxrn4C9SiQJ5t9T1g0EX0pOrY7SPlfLUocg=="
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