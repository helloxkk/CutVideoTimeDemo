package cc.hellokk.cutvideotime.costomview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import cc.hellokk.cutvideotime.R
import java.text.DecimalFormat

/**
 * 作者: Kun on 2018/6/6 11:35
 * 邮箱: vip@hellokk.cc
 * 描述: #
 */
class CutVideoTimeView(context: Context?, absoluteMinValuePrim: Long, absoluteMaxValuePrim: Long) : View(context) {
    private val TAG: String = CutVideoTimeView::class.java.simpleName
    private var absoluteMinValuePrim: Double = 0.toDouble()
    private var absoluteMaxValuePrim: Double = 0.toDouble()
    private var normalizedMinValue = 0.0// 点坐标占总长度的比例值，范围从0-1
    private var normalizedMaxValue = 1.0// 点坐标占总长度的比例值，范围从0-1
    private var min_cut_time: Long = 3000// 最短剪切时长
    private var normalizedMinValueTime = 0.0
    private var normalizedMaxValueTime = 1.0// normalized：规格化的--点坐标占总长度的比例值，范围从0-1
    private var mIsDragging: Boolean = false
    private var pressedThumb: Thumb? = null
    private var padding = 0f
    private var isMin: Boolean = false
    private var min_width = 1.0//最小裁剪距离
    private var notifyWhileDragging = false
    private val ACTION_POINTER_INDEX_MASK = 0x0000ff00
    private val ACTION_POINTER_INDEX_SHIFT = 8

    init {
        this.absoluteMinValuePrim = absoluteMinValuePrim.toDouble()
        this.absoluteMaxValuePrim = absoluteMaxValuePrim.toDouble()
        isFocusable = true
        isFocusableInTouchMode = true
        init()
    }

    enum class Thumb {
        MIN, MAX
    }

    private var mScaledTouchSlop: Int = 0

    private var mDragImageLeft: Bitmap? = null

    private var mDragImageRight: Bitmap? = null

    private var thumbWidth: Int = 0

    private var thumbHalfWidth: Float = 0f

    private var paint: Paint? = null

    private var ractPaint: Paint? = null

    private fun init() {
        // 获取能够识别的最小滑动距离
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        // 等比例缩放图片
        mDragImageLeft = BitmapFactory.decodeResource(resources, R.drawable.ic_drag_left)
        mDragImageRight = BitmapFactory.decodeResource(resources, R.drawable.ic_drag_right)

        val width = mDragImageLeft?.width
        val height = mDragImageLeft?.height

        val newWidth = dip2px(11f)
        val newHeight = dip2px(60f)

        val scaleWidth = newWidth * 1.0f / width!!
        val scaleHeight = newHeight * 1.0f / height!!

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        mDragImageLeft = Bitmap.createBitmap(mDragImageLeft, 0, 0, width, height, matrix, true)
        mDragImageRight = Bitmap.createBitmap(mDragImageRight, 0, 0, width, height, matrix, true)

        thumbWidth = newWidth.toInt()
        thumbHalfWidth = newWidth / 2f

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        ractPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        ractPaint?.style = Paint.Style.FILL
        ractPaint?.color = Color.parseColor("#ffffff")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 300
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec)
        }
        var height = 120
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = View.MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val rangeL = normalizedToScreen(normalizedMinValue)
        val rangeR = normalizedToScreen(normalizedMaxValue)
        // 画出上下矩形
        canvas?.drawRect(rangeL + dip2px(5f), 0f, rangeR - dip2px(5f), dip2px(4f), ractPaint)
        canvas?.drawRect(rangeL + dip2px(5f), height - dip2px(4f), rangeR - dip2px(5f), height.toFloat(), ractPaint)
        // 画左右拖动的thumb
        canvas?.drawBitmap(mDragImageLeft, 0f, 0f, paint)
        canvas?.drawBitmap(mDragImageRight, thumbWidth.toFloat(), 0f, paint)
    }

    private fun normalizedToScreen(normalizedCoord: Double): Float {
        return (paddingLeft + normalizedCoord * (width - paddingLeft - paddingRight)).toFloat()
    }

    private var isTouchDown: Boolean = false

    private var mActivePointerId: Int = 0

    private var mDownMotionX: Float = 0f

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isTouchDown)
            return super.onTouchEvent(event)
        if (event?.pointerCount!! > 1)
            return super.onTouchEvent(event)
        if (!isEnabled)
            return false
        if (absoluteMaxValuePrim <= min_cut_time)
            return super.onTouchEvent(event)
        val pointerIndex: Int// 记录点击点的index
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // 记住最后一个手指点击屏幕的坐标x
                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)
                // 判断 touch 到的是最大值 thumb 还是最小值 thumb
                pressedThumb = evalPressedThumb(mDownMotionX)
                if (pressedThumb == null)
                    return super.onTouchEvent(event)
                isPressed = true// 设置该控件被按下了
                onStartTrackingTouch()// 置mIsDragging为true，开始追踪touch事件
                trackTouchEvent(event)
                attemptClaimDrag()
                if (listener != null) {
                    listener?.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_DOWN, isMin, pressedThumb!!)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (pressedThumb != null) {
                    if (mIsDragging) {
                        trackTouchEvent(event)
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId)
                        val x = event.getX(pointerIndex)// 手指在控件上点的X坐标
                        // 手指没有点在最大最小值上，并且在控件上有滑动事件
                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            isPressed = true
                            Log.e(TAG, "没有拖住最大最小值")// 一直不会执行？
                            invalidate()
                            onStartTrackingTouch()
                            trackTouchEvent(event)
                            attemptClaimDrag()
                        }
                    }
                    if (notifyWhileDragging && listener != null) {
                        listener?.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_MOVE, isMin, pressedThumb!!)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }

                invalidate()
                if (listener != null) {
                    listener?.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue(), MotionEvent.ACTION_UP, isMin, pressedThumb!!)
                }
                pressedThumb = null// 手指抬起，则置被touch到的thumb为空
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
            else ->{}
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT

        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }


    /**
     * 请求父view不要拦截子控件的drag
     */
    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 1) return
        val pointerIndex = event.findPointerIndex(mActivePointerId)// 得到按下点的index
        var x = 0f
        try {
            x = event.getX(pointerIndex)
        } catch (e: Exception) {
            return
        }

        if (Thumb.MIN == pressedThumb) {
            // screenToNormalized(x)-->得到规格化的0-1的值
            setNormalizedMinValue(screenToNormalized(x, 0))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x, 1))
        }
    }

    fun getSelectedMinValue(): Long {
        return normalizedToValue(normalizedMinValueTime)
    }

    fun getSelectedMaxValue(): Long {
        return normalizedToValue(normalizedMaxValueTime)
    }

    private fun normalizedToValue(normalized: Double): Long {
        return (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)).toLong()
    }

    private fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = Math.max(0.0, Math.min(1.0, Math.min(value, normalizedMaxValue)))
        // 重新绘制此view
        invalidate()
    }

    private fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = Math.max(0.0, Math.min(1.0, Math.max(value, normalizedMinValue)))
        // 重新绘制此view
        invalidate()
    }

    private fun screenToNormalized(screenCoord: Float, position: Int): Double {
        val width = width
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0.0
        } else {
            isMin = false
            var current_width = screenCoord.toDouble()
            val rangeL = normalizedToScreen(normalizedMinValue)
            val rangeR = normalizedToScreen(normalizedMaxValue)
            val min = min_cut_time / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width - thumbWidth * 2)

            min_width = if (absoluteMaxValuePrim > 5 * 60 * 1000) {//大于5分钟的精确小数四位
                val df = DecimalFormat("0.0000")
                java.lang.Double.parseDouble(df.format(min))
            } else {
                Math.round(min + 0.5).toDouble()
            }
            if (position == 0) {
                if (isInThumbRangeLeft(screenCoord, normalizedMinValue, 0.5)) {
                    return normalizedMinValue
                }

                val rightPosition = if (getWidth() - rangeR >= 0) (getWidth() - rangeR).toInt() else 0
                val left_length = getValueLength() - (rightPosition.plus(min_width))


                if (current_width > rangeL) {
                    current_width = rangeL + (current_width - rangeL)
                } else if (current_width <= rangeL) {
                    current_width = rangeL - (rangeL - current_width)
                }

                if (current_width > left_length) {
                    isMin = true
                    current_width = left_length
                }

                if (current_width < thumbWidth * 2 / 3) {
                    current_width = 0.0
                }

                val resultTime = (current_width - padding) / (width - 2 * thumbWidth)
                normalizedMinValueTime = Math.min(1.0, Math.max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                return Math.min(1.0, Math.max(0.0, result))// 保证该该值为0-1之间，但是什么时候这个判断有用呢？
            } else {
                if (isInThumbRange(screenCoord, normalizedMaxValue, 0.5)) {
                    return normalizedMaxValue
                }

                val right_length = getValueLength() - (rangeL + min_width)
                if (current_width > rangeR) {
                    current_width = rangeR + (current_width - rangeR)
                } else if (current_width <= rangeR) {
                    current_width = rangeR - (rangeR - current_width)
                }

                var paddingRight = getWidth() - current_width

                if (paddingRight > right_length) {
                    isMin = true
                    current_width = getWidth() - right_length
                    paddingRight = right_length
                }

                if (paddingRight < thumbWidth * 2 / 3) {
                    current_width = getWidth().toDouble()
                    paddingRight = 0.0
                }

                var resultTime = (paddingRight - padding) / (width - 2 * thumbWidth)
                resultTime = 1 - resultTime
                normalizedMaxValueTime = Math.min(1.0, Math.max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                return Math.min(1.0, Math.max(0.0, result))// 保证该该值为0-1之间，但是什么时候这个判断有用呢？
            }

        }
    }

    private fun getValueLength(): Int {
        return width - 2 * thumbWidth
    }

    private fun onStartTrackingTouch() {
        mIsDragging = true
    }

    private fun onStopTrackingTouch() {
        mIsDragging = false
    }

    /**
     * 计算位于哪个Thumb内
     *
     * @param touchX touchX
     * @return 被touch的是空还是最大值或最小值
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed = isInThumbRange(touchX, normalizedMinValue, 2.0)// 触摸点是否在最小值图片范围内
        val maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue, 2.0)
        if (minThumbPressed && maxThumbPressed) {
            // 如果两个thumbs重叠在一起，无法判断拖动哪个，做以下处理
            // 触摸点在屏幕右侧，则判断为touch到了最小值thumb，反之判断为touch到了最大值thumb
            result = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result
    }

    private fun isInThumbRange(touchX: Float, normalizedThumbValue: Double, scale: Double): Boolean {
        // 当前触摸点X坐标-最小值图片中心点在屏幕的X坐标之差<=最小点图片的宽度的一般
        // 即判断触摸点是否在以最小值图片中心为原点，宽度一半为半径的圆内。
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth * scale
    }

    private fun isInThumbRangeLeft(touchX: Float, normalizedThumbValue: Double, scale: Double): Boolean {
        // 当前触摸点X坐标-最小值图片中心点在屏幕的X坐标之差<=最小点图片的宽度的一般
        // 即判断触摸点是否在以最小值图片中心为原点，宽度一半为半径的圆内。
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue) - thumbWidth) <= thumbHalfWidth * scale
    }

    fun setNotifyWhileDragging(flag: Boolean) {
        this.notifyWhileDragging = flag
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    private fun View.dip2px(dpValue: Float): Float {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        bundle.putDouble("MIN_TIME", normalizedMinValueTime)
        bundle.putDouble("MAX_TIME", normalizedMaxValueTime)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
        normalizedMinValueTime = bundle.getDouble("MIN_TIME")
        normalizedMaxValueTime = bundle.getDouble("MAX_TIME")
    }

    private var listener: OnRangeSeekBarChangeListener? = null

    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(view: CutVideoTimeView, minValue: Long, maxValue: Long, action: Int, isMin: Boolean, pressedThumb: Thumb)
    }

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener) {
        this.listener = listener
    }

}