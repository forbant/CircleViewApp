package com.example.circleviewapp.shapes

import android.graphics.Bitmap
import com.example.circleviewapp.shapes.elements.RingElement
import java.lang.Float.max
import kotlin.math.pow
import kotlin.math.sqrt

class Circle(numOfElements: Int, bitmap: Bitmap): Shape(numOfElements, bitmap) {

    init {
        repeat(numOfElements) {
            elements.add(RingElement(bitmap))
        }
    }

    override fun setShapeBounds(l: Float, t: Float, r: Float, b: Float) {
        left = l
        top = t
        right = r
        bottom = b
        centerX = (right + left) / 2
        centerY = (bottom + top) / 2

        shapeWidth = right - left
        shapeHeight = bottom - top

        var diameter = max(shapeWidth, shapeHeight)
        val offsetDiameter = diameter/numOfElements
        var offsetLeft = left
        var offsetTop = top

        elements.forEach {
            it.setBounds(offsetLeft, offsetTop, offsetLeft + diameter, offsetTop + diameter)
            offsetLeft += shapeWidth/numOfElements/2
            offsetTop += shapeHeight/numOfElements/2
            diameter -= offsetDiameter
        }

    }

    override fun touched(x: Float, y: Float): Boolean {
        val distance =
            sqrt(
                (centerX - x).pow(2) +
                        (centerY - y).pow(2)
            )
        return distance <= elements[0].bounds.width()/2
    }
}