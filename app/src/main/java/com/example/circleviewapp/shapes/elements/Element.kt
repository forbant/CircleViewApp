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
        shader.setLocalMatrix(matrix)
        paint.shader = shader
        field = value
    }

    init {
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
    }

    fun drawOn(canvas: Canvas) {
        canvas.drawOval(bounds, paint)
    }

    fun setBounds(left: Float, top: Float, right: Float, bottom: Float) {
        bounds.set(left, top, right, bottom)
    }

    fun setMatrix() {
        matrix.setScale(1f,1f)
        matrix.postTranslate(1f,1f)
    }

    fun updateShader() {
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
    }
}