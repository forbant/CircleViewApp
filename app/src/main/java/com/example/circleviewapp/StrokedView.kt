package com.example.circleviewapp

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

class StrokedView(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?) :
            this(context, attrs, 0)
    constructor(context: Context?) :
            this(context, null)

    var strokeWidth = 0f
    lateinit var strokePaint : Paint
    var strokeBitmap : Bitmap
    lateinit var mDrawable : Drawable
    lateinit var strokeShader: Shader
    lateinit var strokeColor: Color
    lateinit var strokedMatrix: Matrix
    lateinit var mStrokeBounds : RectF

    init {
        if(attrs != null) {
            val attributesArray = context!!.obtainStyledAttributes(attrs, R.styleable.StrokedView, defStyleAttr, defStyleRes)
            val id = attributesArray.getResourceId(R.styleable.StrokedView_srcImage, 0)
            mDrawable = resources.getDrawable(id, null)
            attributesArray.recycle()
        }

        strokeBitmap = getBitmapFromDrawable()!!
        strokeShader = BitmapShader(strokeBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        strokedMatrix = Matrix()
        strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            strokePaint.shader = strokeShader
            strokePaint.style = Paint.Style.STROKE
        }
    }

    private fun getBitmapFromDrawable(): Bitmap? {
        val d = mDrawable
        if(d is BitmapDrawable) {
            return d.bitmap
        }
        return null
    }
}