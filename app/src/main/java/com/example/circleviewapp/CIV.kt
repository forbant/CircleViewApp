package com.example.circleviewapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.RotateAnimation
import kotlin.math.*

class CIV(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?) :
            this(context, attrs, 0)
    constructor(context: Context?) :
            this(context, null)

    lateinit var mBitmapShader : Shader
    var mShaderMatrix : Matrix
    var mBitmapDrawBounds : RectF
    var mBitmapDrawBounds_SECOND : RectF
    var mBitmap: Bitmap
    var mBitmapPaint : Paint
    lateinit var mDrawable : Drawable

    var centerX : Double = 0.0
    var centerY : Double = 0.0
    var offsetRaw : Double = 0.0
    var offsetAngle : Double = 0.0
    var pointedAngle = 0.0
    var startAngle : Double = 0.0
    var moveToAngle : Double = 0.0

    init {
        if(attrs != null) {
            val attributesArray = context!!.obtainStyledAttributes(
                attrs,
                R.styleable.CIV,
                defStyleAttr,
                defStyleRes
            )

            val id = attributesArray.getResourceId(R.styleable.CIV_src, 0)

            mDrawable = resources.getDrawable(id, null)

            attributesArray.recycle()
        }

        mBitmap = getBitmapFromDrawable()?.also {
            mBitmapShader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }!!
        mShaderMatrix = Matrix()
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.shader = mBitmapShader
        }
        mBitmapDrawBounds = RectF()
        mBitmapDrawBounds_SECOND = RectF()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var processed = false
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(!touchedInCircle(event.x, event.y)) return false
                offsetAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
            }
            MotionEvent.ACTION_MOVE -> {
                pointedAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
                offsetRaw = pointedAngle - offsetAngle
                offsetAngle = pointedAngle
                moveToAngle = startAngle.toFloat() + offsetRaw
                rotateWithMatrix(startAngle, moveToAngle)
                startAngle = moveToAngle
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }

    private fun rotateWithMatrix(startAngle: Double, moveToAngle: Double) {
        val angle = moveToAngle.toFloat() - startAngle.toFloat()
        mShaderMatrix.postRotate(angle, centerX.toFloat(), centerY.toFloat())
        mBitmapShader.setLocalMatrix(mShaderMatrix)
        invalidate()
    }

    private fun touchedInCircle(x: Float, y: Float): Boolean {
        val distance = sqrt(
            (mBitmapDrawBounds.centerX().toDouble() - x).pow(2.0) +
                    (mBitmapDrawBounds.centerY().toDouble() - y).pow(2.0)
        )

        return distance <= (mBitmapDrawBounds.width() / 2)
    }

//    private fun animateView(_prevAngle: Float, _newAngle: Float, i: Int) {
//
//        val rotationAnimation = RotateAnimation(
//            _prevAngle,
//            _newAngle,
//            centerX.toFloat(),
//            centerY.toFloat()
//        )
//        rotationAnimation.duration = i.toLong()
//        rotationAnimation.fillBefore = true
//        rotationAnimation.fillAfter = true
//        rotationAnimation.isFillEnabled = true
//        this.startAnimation(rotationAnimation)
//    }

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

    fun setImageBitmap(uri: Uri?) {
        post {
            mBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri!!))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            processBitmap()
        }
    }

    private fun processBitmap() {
        updateBitmapSize()

        mBitmapShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapShader.setLocalMatrix(mShaderMatrix)
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.shader = mBitmapShader }
    }

    private fun updateBitmapSize() {
        val dx: Float
        val dy: Float
        val scale : Float

        if(mBitmap.width < mBitmap.height) {
            scale = mBitmapDrawBounds.width() / mBitmap.width
            dx = mBitmapDrawBounds.left;
            dy = mBitmapDrawBounds.top - (mBitmap.height * scale / 2f) + (mBitmapDrawBounds.width() / 2f)
        } else {
            scale = mBitmapDrawBounds.height() / mBitmap.height
            dx = mBitmapDrawBounds.left - (mBitmap.width * scale / 2f) + (mBitmapDrawBounds.width() / 2f)
            dy = mBitmapDrawBounds.top

        }

        mShaderMatrix.apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
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
        val d = mDrawable
        if(d is BitmapDrawable) {
            return d.bitmap
        }
        return null
    }
}