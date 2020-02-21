package com.example.circleviewapp

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
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
    var mBitmap: Bitmap

    //from attrs
    lateinit var mDrawable : Drawable
    private var mNumOfCircles: Int = 1


    var mStickToGrid: Boolean = false
    var mStickAngle: Int = 0
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
            mStickAngle = attributesArray.getInt(R.styleable.CIV_stickAngle, 0)

            attributesArray.recycle()
        }

        mBitmap = getBitmapFromDrawable()!!

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

                offsetAngle = Math.toDegrees(
                    atan2(event.x - (width / 2.0), (height / 2.0) - event.y))
                    .roundToInt()
                    .toDouble()
                index = getPointedCircleIndex(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                pointedAngle = Math.toDegrees(
                    atan2(event.x - (width / 2.0), (height / 2.0) - event.y))
                    .roundToInt()
                    .toDouble()
                offsetRaw = pointedAngle - offsetAngle
                offsetAngle = pointedAngle
                moveToAngle = startAngle + offsetRaw
                rotateWithMatrix((moveToAngle-startAngle).toFloat(), index)
                startAngle = moveToAngle
            }
            MotionEvent.ACTION_UP -> {
                if(mStickToGrid) tryToSnap()

                var inRow = true
                ringsList.forEach { ring -> if(!ring.isInRightPosition()) inRow = false }
                if(inRow) {
                    val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1f, 1.2f, 1f)
                    val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1f, 1.2f, 1f)
                    val alpha = PropertyValuesHolder.ofFloat(ALPHA, 1f, 0.7f, 1f)


                    ObjectAnimator.ofFloat(this, ROTATION, 360f, 0f).apply {
                        duration = 1000
                        start()
                    }

//                    ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY, alpha).apply {
//                        start()
//                    }
                }
            }
        }
        return true
    }

    private fun tryToSnap() {
        ringsList[index].tryToSnap(mStickAngle)
        invalidate()
    }

    private fun rotateWithMatrix(angle: Float, index: Int) {
        ringsList[index].rotateRing(angle)
        invalidate()
    }

    private fun touchedInCircle(x: Float, y: Float): Boolean {
        val distance = sqrt(
            ((width / 2.0) - x).pow(2.0) + ((height / 2.0) - y).pow(2.0)
        )
        return distance <= (ringsList[0].bounds.width() / 2)
    }

    private fun getPointedCircleIndex(x: Float, y: Float) : Int {
        val distance = sqrt(
            ((width / 2.0) - x).pow(2.0) + ((height / 2.0) - y).pow(2.0)
        )

        val baseRadius = ringsList[0].bounds.width()/2
        val circleWidth = baseRadius / mNumOfCircles

        return ((-distance+baseRadius)/circleWidth).toInt()
    }

    private fun rescaleBitmap() {
        val scale : Float = if(mBitmap.width < mBitmap.height) {
            ringsList[0].bounds.width() / mBitmap.width
        } else {
            ringsList[0].bounds.height() / mBitmap.height
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
            scale = ringsList[0].bounds.width() / mBitmap.width
            dx = ringsList[0].bounds.left
            dy = ringsList[0].bounds.top - (mBitmap.height * scale / 2f) + (ringsList[0].bounds.width() / 2f)
        } else {
            scale = ringsList[0].bounds.height() / mBitmap.height
            dx = ringsList[0].bounds.left - (mBitmap.width * scale / 2f) + (ringsList[0].bounds.width() / 2f)
            dy = ringsList[0].bounds.top
        }

        ringsList.forEach { ring -> ring.updateBitmapMatrix(scale, dx, dy) }
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
//        centerX = height / 2.0
//        centerY = width / 2.0

        val diameter = contentHeight.coerceAtMost(contentWidth)
        var circleDiameter = diameter

        ringsList.forEach {ring ->
            ring.bounds.set(left, top, left + circleDiameter, top + circleDiameter)
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