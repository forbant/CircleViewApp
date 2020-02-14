package com.example.circleviewapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.RotateAnimation
import android.widget.ImageView
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt

class CIV(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ImageView(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?) :
            this(context, attrs, 0)
    constructor(context: Context?) :
            this(context, null)

    lateinit var mBitmapShader : Shader
    var mShaderMatrix : Matrix

    var mBitmapDrawBounds : RectF

    var mBitmap: Bitmap

    var mBitmapPaint : Paint

    var centerX : Double = 0.0
    var centerY : Double = 0.0
    var offsetRaw : Double = 0.0
    var offsetAngle : Double = 0.0
    var pointedAngle = 0.0
    var startAngle : Double = 0.0
    var moveToAngle : Double = 0.0

    init {
        mBitmap = getBitmapFromDrawable()?.also {
            mBitmapShader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }!!
        mShaderMatrix = Matrix()
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.shader = mBitmapShader }
        mBitmapDrawBounds = RectF()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                offsetAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
            }
            MotionEvent.ACTION_MOVE -> {
                pointedAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
                offsetRaw = pointedAngle - offsetAngle
                offsetAngle = pointedAngle
                moveToAngle = startAngle.toFloat() + offsetRaw

                animateView(startAngle.toFloat(), moveToAngle.toFloat(), 0)

                startAngle = moveToAngle
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }

    private fun animateView(_prevAngle: Float, _newAngle: Float, i: Int) {

        val rotationAnimation = RotateAnimation(
            _prevAngle,
            _newAngle,
            centerX.toFloat(),
            centerY.toFloat()
        )
        rotationAnimation.duration = i.toLong()
        rotationAnimation.fillBefore = true
        rotationAnimation.fillAfter = true
        rotationAnimation.isFillEnabled = true
        this.startAnimation(rotationAnimation)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            canvas.drawOval(mBitmapDrawBounds, mBitmapPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(w,h)
        setMeasuredDimension(size,size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCircleDrawBounds()
        updateBitmapSize()
    }


    override fun setImageBitmap(bm: Bitmap?) {
        //super.setImageBitmap(bm)
        bm?.let {
            mBitmapShader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.shader = mBitmapShader }
    }

    private fun updateBitmapSize() {
        val scale : Float = if(mBitmap.height > mBitmap.width) {
            mBitmapDrawBounds.height() / mBitmap.height
        } else {
            mBitmapDrawBounds.height() / mBitmap.height
        }

        mShaderMatrix.apply {
            reset()
            setScale(scale, scale)
            postTranslate(mBitmapDrawBounds.left, mBitmapDrawBounds.top)
        }
        mBitmapShader.setLocalMatrix(mShaderMatrix)
    }

    private fun updateCircleDrawBounds() {
        val contentHeight = height - paddingBottom - paddingTop
        val contentWidth = width - paddingLeft - paddingRight

        var left = paddingLeft.toFloat()
        var top = paddingTop.toFloat()

        if(contentWidth > contentHeight) {
            left += (contentWidth - contentHeight) / 2f
        } else {
            top += (contentHeight - contentWidth) / 2f
        }

        val diameter = contentHeight.coerceAtMost(contentWidth)
        mBitmapDrawBounds.set(left, top, left + diameter, top + diameter)

        centerX = height / 2.0
        centerY = width / 2.0
    }

    private fun getBitmapFromDrawable() : Bitmap? {
        val d = drawable
        if(d is BitmapDrawable) {
            return d.bitmap
        }
        return null
    }
}