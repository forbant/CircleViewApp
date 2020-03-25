package com.example.circleviewapp.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.circleviewapp.shapes.elements.Element
import com.example.circleviewapp.shapes.elements.RingElement

abstract class Shape(var numOfElements: Int, var bitmap: Bitmap) {

    var elements = mutableListOf<Element>()
//    var bitmap: Bitmap? = null
    var shapeWidth = 0f
    var shapeHeight = 0f
    var left = 0f
    var top = 0f
    var right = 0f
    var bottom = 0f

    fun drawOn(canvas: Canvas) {
        elements.forEach { element ->
            element.drawOn(canvas)
        }
    }

    abstract fun setShapeBounds(l: Float, t: Float, r: Float, b: Float)

    fun setRescaledBitmap(b: Bitmap) {
        val scale : Float = if(b.width < b.height) {
            shapeWidth / b.width
        } else {
            shapeHeight / b.height
        }

        bitmap = Bitmap.createScaledBitmap(b, (b.width * scale).toInt(), (b.height * scale).toInt(), false)

        elements.forEach {
            it.bitmap = bitmap
        }
    }

    fun updateBitmapMatrix() {
        elements.forEach {
            it.setMatrix()
        }
    }

    fun updateShader() {
        elements.forEach {
            it.updateShader()
        }
    }
}