package com.example.circleviewapp.shapes.elements

import android.graphics.Bitmap

class RingElement(bitmap: Bitmap): Element(bitmap) {
    override fun move(shiftValue: Float) {
        angle += shiftValue
        if(angle >= 180) angle -= 360
        if(angle <= -180) angle += 360
        matrix.postRotate(angle, bounds.centerY(), bounds.centerY())
        shader.setLocalMatrix(matrix)
    }

}