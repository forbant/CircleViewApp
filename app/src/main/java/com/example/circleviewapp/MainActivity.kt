package com.example.circleviewapp

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.renderscript.*
import android.view.View.ROTATION
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : Activity() {

    private val REQUEST_PICK = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val animator = ObjectAnimator.ofFloat(circleView, ROTATION, 360f, 0f).apply {
            duration = 1000
        }
        circleView.endAnimation = animator

        fabInsertImage.setOnClickListener {
            val intentPick = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPick, REQUEST_PICK)
        }

        fabRandomRotate.setOnClickListener {
            repeat(circleView.getCircleCount()) {
                val randomAngle = Random.nextInt(-180, 180)
                circleView.rotateRingByIndex(randomAngle.toFloat(), it)
            }
        }

    }

    fun blur(image: Bitmap): Bitmap {
        var outputBitmap = Bitmap.createBitmap(image)
        val renderScript = RenderScript.create(this)
        val tmpIn = Allocation.createFromBitmap(renderScript, image)
        val tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)

        val theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        theIntrinsic.setRadius(25f)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.data?.run{
                    circleView.setImageBitmap(this)
                    var bitmap: Bitmap = getBitmapFromURI(this)
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 32, bitmap.height / 32, false )
                    bitmap = blur(bitmap)
                    backgroundView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun getBitmapFromURI(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }
}
