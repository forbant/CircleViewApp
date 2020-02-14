package com.example.circleviewapp

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI

class MainActivity : AppCompatActivity() {

    private val REQUEST_PICK = 1

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
            val a = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, selectedFile!!))
            } else {
                Media.getBitmap(contentResolver, selectedFile)
            }
            viewGlass.setImageBitmap(a)
        }
    }
}
