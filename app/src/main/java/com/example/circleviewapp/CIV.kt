package com.example.circleviewapp

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.*

class CIV(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, 0)
    constructor(context: Context) :
            this(context, null)

    private val ringsList = mutableListOf<Ring>()
    private var bitmap: Bitmap

    //from attrs
    private var drawableResource: Drawable? = null
    private var drawableError: Drawable? = null
    private var numOfCircles: Int = 1

    private var snap: Boolean = false
    private var canRotate = true
    private var mStickAngle: Int = 0
    var endAnimation : ObjectAnimator? = null

    private var offsetRaw: Double = 0.0
    private var offsetAngle: Double = 0.0
    private var pointedAngle = 0.0
    private var startAngle: Double = 0.0
    private var moveToAngle: Double = 0.0
    private var index: Int = 0
    private val errorText = "Error during processing drawable"

    init {
        if(attrs != null) {
            val attributesArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CIV,
                defStyleAttr,
                defStyleRes
            )

            val drawableId = attributesArray.getResourceId(R.styleable.CIV_src, 0)
            if(drawableId != 0)
                drawableResource = resources.getDrawable(drawableId, null)
            val drawableErrorId = attributesArray.getResourceId(R.styleable.CIV_drawableError, 0)
            if(drawableErrorId != 0)
                drawableError = resources.getDrawable(drawableErrorId, null)
            numOfCircles = attributesArray.getInt(R.styleable.CIV_numOfCircles, 1)
            snap = attributesArray.getBoolean(R.styleable.CIV_snap, false)
            mStickAngle = attributesArray.getInt(R.styleable.CIV_snapAngleRange, 0)

            attributesArray.recycle()
        }

        bitmap = getBitmapFromDrawable()

        repeat(numOfCircles) {
            ringsList.add(Ring(bitmap))
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
        if(canRotate) {
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
                    rotateRingByIndex((moveToAngle-startAngle).toFloat(), index)
                    startAngle = moveToAngle
                }
                MotionEvent.ACTION_UP -> {
                    if(snap)
                        tryToSnap()
                    endAnimation?.let {
                        var inRow = true
                        ringsList.forEach { ring -> if(!ring.isInRightPosition()) inRow = false }
                        if(inRow) it.start()
                    }
                }
            }
        }
        return true
    }

    fun rotateRingByIndex(angle: Float, index: Int) {
        if(canRotate) {
            ringsList[index].rotateRing(angle)
            invalidate()
        }
    }

    fun getCircleCount(): Int {
        return numOfCircles
    }

    fun setImageBitmap(uri: Uri) {
        post {
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            canRotate = true
            processBitmap()
        }
    }

    private fun tryToSnap() {
        if(ringsList[index].tryToSnap(mStickAngle))
            invalidate()
    }

    private fun getPointedCircleIndex(x: Float, y: Float) : Int {
        val distance = sqrt(
            ((width / 2.0) - x).pow(2.0) + ((height / 2.0) - y).pow(2.0)
        )
        val baseRadius = ringsList[0].bounds.width()/2
        val circleWidth = baseRadius / numOfCircles

        return ((-distance+baseRadius)/circleWidth).toInt()
    }

    private fun rescaleBitmap() {
        val scale : Float = if(bitmap.width < bitmap.height) {
            ringsList[0].bounds.width() / bitmap.width
        } else {
            ringsList[0].bounds.height() / bitmap.height
        }
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width*scale).toInt(),
            (bitmap.height*scale).toInt(),
            false)
        for (ring in ringsList) {
            ring.bitmap = bitmap
        }
    }

    private fun touchedInCircle(x: Float, y: Float): Boolean {
        val distance = sqrt(
            ((width / 2.0) - x).pow(2.0) + ((height / 2.0) - y).pow(2.0)
        )
        return distance <= (ringsList[0].bounds.width() / 2)
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

        if(bitmap.width < bitmap.height) {
            scale = ringsList[0].bounds.width() / bitmap.width
            dx = ringsList[0].bounds.left
            dy = ringsList[0].bounds.top - (bitmap.height * scale / 2f) + (ringsList[0].bounds.width() / 2f)
        } else {
            scale = ringsList[0].bounds.height() / bitmap.height
            dx = ringsList[0].bounds.left - (bitmap.width * scale / 2f) + (ringsList[0].bounds.width() / 2f)
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

        val diameter = contentHeight.coerceAtMost(contentWidth)
        var circleDiameter = diameter

        ringsList.forEach {ring ->
            ring.setBounds(left, top, left + circleDiameter, top + circleDiameter)
            left += contentHeight/numOfCircles/2
            top += contentHeight/numOfCircles/2
            circleDiameter -= diameter/numOfCircles
        }

    }

    private fun getBitmapFromDrawable() : Bitmap {
        drawableResource?.let {
            if(it is BitmapDrawable)
                return it.bitmap
        }
        return errorBitmap()
    }

    private fun errorBitmap() : Bitmap {
        canRotate = false
        drawableError?.let {
            return it.toBitmap()
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 32f
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }

        val size = min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 2
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(b)
        val offset = size/2f
        canvas.drawText(errorText, offset, offset, paint)
        return  b
    }
}