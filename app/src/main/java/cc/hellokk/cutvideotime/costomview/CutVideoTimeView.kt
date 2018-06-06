package cc.hellokk.cutvideotime.costomview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewConfiguration
import cc.hellokk.cutvideotime.R

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

    private var mScaledTouchSlop: Int? = null

    private var mDragImageLeft: Bitmap? = null

    private var mDragImageRight: Bitmap? = null

    private var thumbWidth: Float = 0f

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

        thumbWidth = newWidth
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
        canvas?.drawBitmap(mDragImageLeft,0f,0f,paint)
        canvas?.drawBitmap(mDragImageRight,thumbWidth,0f,paint)
    }

    private fun normalizedToScreen(normalizedCoord: Double): Float {
        return (paddingLeft + normalizedCoord * (width - paddingLeft - paddingRight)).toFloat()
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
}