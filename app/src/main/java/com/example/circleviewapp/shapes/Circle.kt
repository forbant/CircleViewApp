package com.example.circleviewapp.shapes

import android.graphics.Bitmap
import com.example.circleviewapp.shapes.elements.RingElement
import java.lang.Float.max

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
}