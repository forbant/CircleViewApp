package com.example.circleviewapp.shapes

import android.graphics.Canvas
import com.example.circleviewapp.shapes.elements.Element

abstract class Shape {

    var elements = mutableListOf<Element>()

    fun drawOn(canvas: Canvas) {
        elements.forEach { element ->
            element.drawOn(canvas)
        }
    }
}