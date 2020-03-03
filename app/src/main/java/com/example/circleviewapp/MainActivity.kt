package com.example.circleviewapp

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.view.View.ROTATION
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.run {
            if(requestCode == REQUEST_PICK && resultCode == Activity.RESULT_OK) {
                circleView.setImageBitmap(this.data)
            }
        }
    }
}
