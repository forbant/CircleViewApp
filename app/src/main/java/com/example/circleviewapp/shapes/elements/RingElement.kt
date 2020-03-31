package com.example.circleviewapp.shapes.elements

import android.graphics.Bitmap

class RingElement(bitmap: Bitmap): Element(bitmap) {
    override fun move(shiftValue: Float) {
        angle += shiftValue
        if(angle >= 180) angle -= 360
        if(angle <= -180) angle += 360
        matrix.postRotate(shiftValue, bounds.centerY(), bounds.centerY())
        shader.setLocalMatrix(matrix)
    }

    override fun snap(): Boolean {
        when {
            (angle < 15 && angle > 0) -> move(-angle)
            (angle > -15 && angle < 0) -> move(-angle)
            else -> return false
        }
        return true
    }

}