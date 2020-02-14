package com.example.circleviewapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.os.bundleOf

class CustomCircleView(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
): ImageView(
    context,
    attrs,
    defStyleAttr,
    defStyleRes)
{

    private val DEF_PRESS_HIGHLIGHT_COLOR = 0x32000000

    lateinit var mBitmapShader : Shader
    var mShaderMatrix : Matrix

    var mBitmapDrawBounds : RectF
    var mStrokeBounds : RectF

    lateinit var mBitmap: Bitmap

    var mBitmapPaint : Paint
    var mStrokePaint : Paint
    var mPressedPaint : Paint

    var mInitialized = false
    var mPressed = false
    var mHighlightEnable = true


    constructor(context: Context?,
                attrs: AttributeSet?,
                defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?,
                attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    init {
        var strokeColor = Color.TRANSPARENT
        var strokeWidth = 0f
        var highlightEnable = true
        var highlightColor = DEF_PRESS_HIGHLIGHT_COLOR

        if(attrs != null) {
            val attributesArray = context!!.obtainStyledAttributes(attrs, R.styleable.CustomCircleView, defStyleAttr, defStyleRes)

            strokeColor = attributesArray.getColor(R.styleable.CustomCircleView_strokeColor, Color.TRANSPARENT)
            strokeWidth = attributesArray.getDimension(R.styleable.CustomCircleView_strokeWidth, 0f)
            highlightEnable = attributesArray.getBoolean(R.styleable.CustomCircleView_highlightEnable, true)
            highlightColor = attributesArray.getColor(R.styleable.CustomCircleView_highlightColor, DEF_PRESS_HIGHLIGHT_COLOR)

            attributesArray.recycle()
        }


        mShaderMatrix = Matrix()
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokeBounds = RectF()
        mBitmapDrawBounds = RectF()
        mStrokePaint.apply {
            color = strokeColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }

        mPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPressedPaint.apply {
            color = highlightColor
            style = Paint.Style.FILL
        }

        mHighlightEnable = highlightEnable
        mInitialized = true

        //setupBitmap()
    }

    private fun setupBitmap() {
        if(!mInitialized) {
            return
        }
        mBitmap = getBitmapFromDrawable(drawable)!!

        mBitmapShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapPaint.shader = mBitmapShader

        updateBitmapSize()
    }

    private fun updateBitmapSize() {
        val dy : Float
        val dx : Float

        val scale : Float

        if(mBitmap.width >= mBitmap.height) {
            //scale = mBitmapDrawBounds.width() / mBitmapDrawBounds.height()
            scale = mBitmapDrawBounds.width() / mBitmap.width
            dx = mBitmapDrawBounds.left
            dy = mBitmapDrawBounds.top - (mBitmap.height * scale / 2f) + (mBitmapDrawBounds.width() / 2f)
        } else {
            scale = mBitmapDrawBounds.height() / mBitmap.height
            dx = mBitmapDrawBounds.left - (mBitmap.width * scale / 2f) + (mBitmapDrawBounds.width() / 2f)
            dy = mBitmapDrawBounds.top
        }


        mShaderMatrix.apply {
            reset()
            setScale(scale, scale)
            postTranslate(dx, dy)
            setRotate(45f)
        }
        mBitmapShader.setLocalMatrix(mShaderMatrix)

    }

    private fun getBitmapFromDrawable(drawable: Drawable?) : Bitmap? {
        if(drawable == null) {
            return null
        }

        if(drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        drawable.setBounds(0,0,canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCircleDrawBounds(mBitmapDrawBounds)
        setupBitmap()
    }

    private fun updateCircleDrawBounds(bounds : RectF) {
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        var left = paddingLeft.toFloat()
        var top = paddingTop.toFloat()

        if(contentWidth > contentHeight) {
            left += (contentWidth - contentHeight) / 2f
        } else {
            top += (contentHeight - contentWidth) / 2f
        }


        val diameter = contentHeight.coerceAtMost(contentWidth)
        mBitmapDrawBounds.set(left, top, left + diameter, top + diameter)
    }


    override fun onDraw(canvas: Canvas?) {
        drawBitmap(canvas)
        //drawStroke(canvas)
        //drawHighlight(canvas)
    }

    private fun drawBitmap(canvas: Canvas?) {
        canvas?.run {
            drawOval(mBitmapDrawBounds, mBitmapPaint)
        }
    }

    private fun drawStroke(canvas: Canvas?) {
        if(mStrokePaint.strokeWidth > 0f) {
            canvas!!.drawOval(mStrokeBounds, mStrokePaint)
        }
    }

    private fun drawHighlight(canvas: Canvas?) {
//        if(mHighlightEnable && mPressed) {
//            canvas!!.drawOval(mBitmapDrawBounds, mPressedPaint)
//        }
    }


}