package com.example.circleviewapp.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.circleviewapp.shapes.elements.Element

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
        val dx: Float
        val dy: Float
        val scale : Float

        if(bitmap.width < bitmap.height) {
            scale = elements[0].bounds.width() / bitmap.width
            dx = elements[0].bounds.left
            dy = elements[0].bounds.top - (bitmap.height * scale / 2f) + (elements[0].bounds.width() / 2f)
        } else {
            scale = elements[0].bounds.height() / bitmap.height
            dx = elements[0].bounds.left - (bitmap.width * scale / 2f) + (elements[0].bounds.width() / 2f)
            dy = elements[0].bounds.top
        }
        elements.forEach {
            it.setMatrix(scale, dx, dy)
        }
    }

    fun updateShader() {
        elements.forEach {
            it.updateShader()
        }
    }
}