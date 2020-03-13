package com.example.circleviewapp.shapes.elements

import android.graphics.*

abstract class Element(bitmap: Bitmap) {

    private var angle = 0f
    private var matrix: Matrix = Matrix()
    private var shader: Shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var bounds = RectF()

    var bitmap: Bitmap = bitmap
    set(value) {
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        field = value
    }

    fun drawOn(canvas: Canvas) {
        canvas.drawOval(bounds, paint)
    }
}