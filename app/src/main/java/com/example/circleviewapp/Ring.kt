package com.example.circleviewapp

import android.graphics.*

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
        ringAngle += angle
        if(ringAngle >= 180) ringAngle -= 360
        if(ringAngle <= -180) ringAngle += 360
        matrix.postRotate(angle, bounds.centerY(), bounds.centerY())
        shader.setLocalMatrix(matrix)
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
        ringAngle = 0f
    }

    override fun toString(): String {
        return super.toString()
    }

    fun tryToSnap(stickAngle: Int) {
        when {
            (ringAngle < stickAngle && ringAngle > 0) -> rotateRing(-ringAngle)
            (ringAngle > -stickAngle && ringAngle < 0) -> rotateRing(-ringAngle)
        }
    }

    fun isInRightPosition(): Boolean {
        return ringAngle == 0f
    }

}