package com.example.circleviewapp

import android.graphics.*

class Ring(bitmap: Bitmap) {

    var diameter: Int = 0
    var matrix: Matrix
    var shader: Shader
    var centerX: Float = 0f
    var centerY: Float = 0f
    var bounds: RectF
    var paint: Paint

    var bitmap: Bitmap = bitmap
        set(value) {
            shader = BitmapShader(value, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            shader.setLocalMatrix(matrix)
            paint.shader = shader
            field = value
        }

    init {
        bounds = RectF()
        matrix = Matrix()
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
    }



}