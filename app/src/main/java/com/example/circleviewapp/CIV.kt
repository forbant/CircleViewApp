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
import kotlin.math.*

class CIV(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?) :
            this(context, attrs, 0)
    constructor(context: Context?) :
            this(context, null)

    private val ringsList: List<Ring>
    lateinit var mBitmapShader : Shader
    var mShaderMatrix : Matrix
    var mBitmapDrawBounds : RectF
    var mBitmap: Bitmap

    var mBitmapPaint : Paint
    //from attrs
    lateinit var mDrawable : Drawable
    private var mNumOfCircles: Int = 1


    var mStickToGrid: Boolean = false
    ////For test/////
    var centerX: Double = 0.0
    var centerY: Double = 0.0
    var offsetRaw: Double = 0.0
    var offsetAngle: Double = 0.0
    var pointedAngle = 0.0
    var startAngle: Double = 0.0
    var moveToAngle: Double = 0.0
    var index : Int = 0

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
            mNumOfCircles = attributesArray.getInt(R.styleable.CIV_numOfCircles, 1)
            mStickToGrid = attributesArray.getBoolean(R.styleable.CIV_gridStick, false)

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

        val rings = ArrayList<Ring>()
        repeat(mNumOfCircles) {
            rings.add(Ring(mBitmap))
        }
        ringsList = rings
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
        processBitmap()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            for(item in ringsList) {
                canvas.drawOval(item.bounds, item.paint)
            }

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(!touchedInCircle(event.x, event.y)) return false

                offsetAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
                index = getPointedCircleIndex(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                pointedAngle = Math.toDegrees(atan2(event.x - centerX, centerY - event.y)).roundToInt().toDouble()
                offsetRaw = pointedAngle - offsetAngle
                offsetAngle = pointedAngle
                moveToAngle = startAngle + offsetRaw
                rotateWithMatrix((moveToAngle-startAngle).toFloat(), index)
                startAngle = moveToAngle
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }

    private fun rotateWithMatrix(angle: Float, index: Int) {
        ringsList[index].rotateRing(angle)
        invalidate()
    }

    private fun touchedInCircle(x: Float, y: Float): Boolean {
        val distance = sqrt(
            (mBitmapDrawBounds.centerX().toDouble() - x).pow(2.0) +
                    (mBitmapDrawBounds.centerY().toDouble() - y).pow(2.0)
        )
        return distance <= (mBitmapDrawBounds.width() / 2)
    }

    private fun getPointedCircleIndex(x: Float, y: Float) : Int {
        val distance = sqrt(
            (mBitmapDrawBounds.centerX().toDouble() - x).pow(2.0) +
                    (mBitmapDrawBounds.centerY().toDouble() - y).pow(2.0)
        )

        val baseRadius = mBitmapDrawBounds.width()/2
        val circleWidth = baseRadius / mNumOfCircles

        return ((-distance+baseRadius)/circleWidth).toInt()
    }

    private fun rescaleBitmap() {
        val scale : Float = if(mBitmap.width < mBitmap.height) {
            mBitmapDrawBounds.width() / mBitmap.width
        } else {
            mBitmapDrawBounds.height() / mBitmap.height
        }
        mBitmap = Bitmap.createScaledBitmap(
            mBitmap,
            (mBitmap.width*scale).toInt(),
            (mBitmap.height*scale).toInt(),
            false)
        for (ring in ringsList) {
            ring.bitmap = mBitmap
        }
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
        rescaleBitmap()
        updateBitmapMatrix()
        updateShader()
    }

    private fun updateShader() {
        ringsList.forEach { ring -> ring.updateShader() }
    }

    private fun updateBitmapMatrix() {
        val dx: Float
        val dy: Float
        val scale : Float

        if(mBitmap.width < mBitmap.height) {
            scale = mBitmapDrawBounds.width() / mBitmap.width
            dx = mBitmapDrawBounds.left
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
        ringsList.forEach { ring -> ring.updateBitmapMatrix(scale, dx, dy) }
//        repeat(mNumOfCircles) {
//            ringsList[it].matrix.setScale(scale, scale)
//            ringsList[it].matrix.postTranslate(dx, dy)
//            ringsList[it].shader.setLocalMatrix(ringsList[it].matrix)
//        }
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
        centerX = height / 2.0
        centerY = width / 2.0

        val diameter = contentHeight.coerceAtMost(contentWidth)
        var circleDiameter = diameter
        mBitmapDrawBounds.set(left, top, left + circleDiameter, top + circleDiameter)
        repeat(mNumOfCircles) {
            ringsList[it].bounds.set(left, top, left + circleDiameter, top + circleDiameter)
            left += contentHeight/mNumOfCircles/2
            top += contentHeight/mNumOfCircles/2
            circleDiameter -= diameter/mNumOfCircles
        }

    }

    private fun getBitmapFromDrawable() : Bitmap? {
        val d = mDrawable
        if(d is BitmapDrawable) {
            return d.bitmap
        }
        return null
    }
}