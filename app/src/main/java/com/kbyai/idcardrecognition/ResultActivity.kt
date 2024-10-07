package com.kbyai.idcardrecognition

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import android.provider.Settings
import android.os.Build.VERSION.SDK_INT
import android.os.Environment

class ResultActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 1

    private lateinit var profileImg : ImageView
    private lateinit var nameTxt: TextView
    private lateinit var typeTxt: TextView
    private lateinit var shareBtn: Button

    private lateinit var pager: ViewPager // creating object of ViewPager
    private lateinit var tab: TabLayout  // creating object of TabLayout
    companion object {
        lateinit var overviewBmp : Bitmap
        lateinit var resultItems: ArrayList<ResultItem>
        lateinit var resultString: String
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        profileImg = findViewById(R.id.profileImg)
        nameTxt = findViewById(R.id.nameTxt)
        typeTxt = findViewById(R.id.typeTxt)
        pager = findViewById(R.id.viewPager)
        tab = findViewById(R.id.tabs)
        shareBtn = findViewById(R.id.shareBtn)
        shareBtn.setOnClickListener {
            if(!checkPermission()) {
                _requestPermission()
            } else {
                generatePdfFromViewPager(pager)
            }
        }
        val result:String = resultString
        resultItems = ArrayList<ResultItem>()

        try {
            var jsonResult = JSONObject(result)
            val keys: MutableIterator<String> = jsonResult.keys()

            while(keys.hasNext()) {
                var key = keys.next() as String
                if(key == getString(R.string.full_name)) {
                    val value = jsonResult.get(key).toString()
                    nameTxt.text = value
                }

                if(key == getString(R.string.images)) {
                    val imagesObj = jsonResult.get(key) as JSONObject
                    val imagesKeys: MutableIterator<String> = imagesObj.keys()

                    while (imagesKeys.hasNext()) {
                        val imageKey = imagesKeys.next() as String
                        val imageValue = imagesObj.get(imageKey).toString()
                        val imageBytes = Base64.getDecoder()!!.decode(imageValue)

                        try {
                            val bitmap =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            if (imageKey == getString(R.string.portrait)) {
                                profileImg.setImageBitmap(bitmap)
                            } else if (imageKey == getString(R.string.document)) {
                                overviewBmp = bitmap
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if(key == getString(R.string.position)) {

                } else if(key == getString(R.string.quality)) {

                } else if(key == getString(R.string.mrz)) {
                    val mrzObj = jsonResult.get(key) as JSONObject
                    val mrzKeys: MutableIterator<String> = mrzObj.keys()

                    while (mrzKeys.hasNext()) {
                        val mrzKey = mrzKeys.next() as String
                        val mrzValue = mrzObj.get(mrzKey).toString()

                        var exists:Boolean = false
                        var idx:Int = 0
                        for (resultItem in resultItems) {
                            if(resultItem.key == mrzKey) {
                                resultItem.value2 = mrzValue
                                resultItem.field2 = "MRZ"
                                resultItems[idx] = resultItem
                                exists = true
                                break
                            }
                            idx += 1
                        }
                        if(exists == false) {
                            val resultItem = ResultItem(mrzKey, "", "", "MRZ", mrzValue, "", "")
                            resultItems.add(resultItem)
                        }
                    }
                } else if(key == getString(R.string.documentName)) {
                    var value = jsonResult.get(key).toString()

                    if(jsonResult.has(getString(R.string.issuingStateCode))) {
                        val stateCode = jsonResult.get(getString(R.string.issuingStateCode)).toString()
                        value = value + " (" + stateCode + ")"
                    }
                    typeTxt.text = value
                } else {
                    var isNational = false
                    var value = jsonResult.get(key).toString()
                    var field1 = "OCR"
                    var field3 = ""
                    var value3 = ""
                    if(key.contains("-")) {
                        isNational = true

                        val subKeys = key.split("-")
                        key = subKeys[0]
                        field3 = subKeys[1]
                        value3 = value
                        value = ""
                        field1 = ""
                    }

                    var exists: Boolean = false
                    var idx:Int = 0
                    for (resultItem in resultItems) {
                        if(resultItem.key == key) {
                            if(isNational) {
                                resultItem.value3 = value3
                                resultItem.field3 = field3
                            } else {
                                resultItem.value1 = value
                                resultItem.field1 = field1
                            }
                            resultItems[idx] = resultItem
                            exists = true
                            break
                        }
                        idx += 1
                    }
                    if(exists == false) {
                        val resultItem = ResultItem(key, field1, value, "", "", field3, value3)
                        resultItems.add(resultItem)
                    }
                }
            }

            val pagerAdapter = ResultViewPagerAdapter(supportFragmentManager)

            // add fragment to the list
            pagerAdapter.addFragment(ResultOverviewFragment(), "Overview")
            pagerAdapter.addFragment(ResultDataFragment(), "Extracted Data")

            // Adding the Adapter to the ViewPager
            pager.adapter = pagerAdapter
            // bind the viewPager with the TabLayout.
            tab.setupWithViewPager(pager)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R ) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun _requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                2296
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2296) {
            if(!checkPermission()) {
                _requestPermission()
            } else {
                generatePdfFromViewPager(pager)
            }
        }
    }

    private fun generatePdfFromViewPager(viewPager: ViewPager) {
        val root = Environment.getExternalStorageDirectory()
        val dir = File(root.absolutePath + "/KbyAI")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, "idcard.pdf")
        viewPager.adapter?.instantiateItem(viewPager, 1)?.let { mFragment ->
            if (mFragment is ResultDataFragment) {
                createPdfFromListView(this, mFragment.listView, file.absolutePath)
            }
        }

        try {
            // PDF created successfully, do something with it (e.g., open or share)
            sharePdfFile(file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            // Error occurred while creating the PDF
        }
    }

    private fun createPdfFromListView(context: Context, listView: ListView, fileName: String) {
        val document = Document()
        try {
            PdfWriter.getInstance(document, FileOutputStream(fileName))
            document.open()

            // Create a single paragraph to hold all the list items
            val paragraph = Paragraph()
            val itemCount = listView.adapter.count
            for (i in 0 until itemCount) {
                val listItem = listView.adapter.getView(i, null, listView)
                listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                listItem.layout(0, 0, listItem.measuredWidth, listItem.measuredHeight)

                val bitmap = Bitmap.createBitmap(listItem.width, listItem.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                listItem.draw(canvas)

                val image = com.itextpdf.text.Image.getInstance(bitmap.toBytes())
                image.scaleToFit(document.pageSize.width - document.leftMargin() - document.rightMargin(), document.pageSize.height - document.topMargin() - document.bottomMargin())
                paragraph.add(image)
            }

            document.add(paragraph)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    private fun Bitmap.toBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun sharePdfFile(pdfFilePath: String) {
        val pdfFile = File(pdfFilePath)
        val pdfUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "application/pdf"
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri)

        startActivity(Intent.createChooser(shareIntent, "Share Card Information"))
    }
}