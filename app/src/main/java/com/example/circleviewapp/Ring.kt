package com.example.circleviewapp

import android.graphics.*

class Ring(bitmap: Bitmap) {

    private var ringAngle = 0f
    private var matrix: Matrix = Matrix()
    private var shader: Shader
    var bounds: RectF = RectF()
    var paint: Paint

    var bitmap: Bitmap = bitmap
        set(value) {
            shader = BitmapShader(value, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
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

    fun tryToSnap(stickAngle: Int): Boolean {
        when {
            (ringAngle < stickAngle && ringAngle > 0) -> rotateRing(-ringAngle)
            (ringAngle > -stickAngle && ringAngle < 0) -> rotateRing(-ringAngle)
            else -> return false
        }
        return true
    }

    fun isInRightPosition(): Boolean {
        return ringAngle == 0f
    }

    fun setBounds(left: Float, top: Float, right: Float, bottom: Float) {
        bounds.set(left, top, right, bottom)
    }
}