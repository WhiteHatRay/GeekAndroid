package com.geek_era.android.GeekViews

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.geek_era.android.R
import android.graphics.*
import android.graphics.drawable.BitmapDrawable


/**
 * GeekImageView之CircleImageView
 * Created by Ray on 2017/8/29.
 * Website: http://www.geek-era.com
 * Email:nuoone@163.com
 */
class CircleImageView : ImageView {
    private var diameter = 0//圆形图片直径
    private var borderWidth = 0f//边框宽度，默认没有外边框
    private var borderColor = 0//边框颜色

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet){
        initViewAttr(attributeSet)
    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr:Int) : super(context, attributeSet, defStyleAttr){
        initViewAttr(attributeSet)
    }

    /**
     * 获取开发者设置的参数
     */
    private fun initViewAttr(attributeSet: AttributeSet){
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleImageView)
        borderWidth = typedArray.getDimension(R.styleable.CircleImageView_borderWidth, borderWidth)
        borderColor = typedArray.getColor(R.styleable.CircleImageView_borderColor, borderColor)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        if (drawable == null || width ==0  || height == 0){
            return
        }
        diameter = Math.min(width, height)
        val bitmap = (drawable as BitmapDrawable).bitmap

        val bitmapRes = drawCircleImageView(bitmap)
        canvas!!.drawBitmap(bitmapRes, 0f, 0f, null)
        //绘制border
        drawCircleBorder(canvas)
    }

    /**
     * 绘制ImageView
     */
    private fun drawCircleImageView(bitmap: Bitmap): Bitmap {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, diameter, diameter, true)
        val bitmapOut = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapOut)
        val paint = Paint()
        val rect = Rect(0, 0, diameter, diameter)
        val radius = diameter / 2f

        paint.setAntiAlias(true)//抗锯齿
        paint.setFilterBitmap(true)//设置滤波
        paint.setDither(true)//设置递色

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(radius, radius, radius - borderWidth, paint)

        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))//取两层相交部分的上层显示。
        canvas.drawBitmap(scaledBitmap, rect, rect, paint)
        return bitmapOut

    }

    /**
     * 绘制border
     */
    private fun drawCircleBorder(canvas: Canvas) {
        val radius = diameter / 2f
        if (borderWidth == 0f) {
            return
        } else {
            val paint = Paint()
            paint.setAntiAlias(true)
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeWidth(borderWidth)
            if (borderColor == 0) {
                //默认白色
                paint.setColor(Color.WHITE)
            } else {
                //使用设置的颜色
                paint.setColor(borderColor)
            }
            canvas.drawCircle(radius, radius, radius - borderWidth, paint)
        }
    }
}