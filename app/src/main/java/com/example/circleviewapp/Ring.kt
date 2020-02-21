package com.example.circleviewapp

import android.graphics.*
import java.util.zip.DeflaterOutputStream

class Ring(bitmap: Bitmap) {

    private var ringAngle = 0f
    var matrix: Matrix
    var shader: Shader
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

    fun rotateRing(angle: Float) {
        matrix.postRotate(angle, bounds.centerY(), bounds.centerY())
        shader.setLocalMatrix(matrix)
        ringAngle += angle
    }

    fun updateShader() {
        shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
    }

    fun updateBitmapMatrix(scale: Float, dx: Float, dy: Float) {
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
    }

    override fun toString(): String {
        return super.toString()
    }

}