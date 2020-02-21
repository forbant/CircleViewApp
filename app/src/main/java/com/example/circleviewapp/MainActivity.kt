package com.example.circleviewapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Integer.min

class MainActivity : AppCompatActivity() {

    private val REQUEST_PICK = 1
    val file : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabInsertImage.setOnClickListener {
            val intentPick = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPick, REQUEST_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            val selectedFile = data!!.data
            circleView.setImageBitmap(selectedFile)
        }
    }
}
