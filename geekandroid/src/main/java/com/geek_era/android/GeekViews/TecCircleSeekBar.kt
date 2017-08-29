package com.geek_era.android.GeekViews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.geek_era.android.Interface.ViewInterface.GeekSeekBarOnChangeListener
import com.geek_era.android.R

/**
 * 科技主题圆形SeekBar之TecCircleSeekBar
 * Created by Ray on 2017/8/29.
 * Website: http://www.geek-era.com
 * Email:nuoone@163.com
 */
class TecCircleSeekBar : View {
    private var progressWidth = 2f //progress width, default 2
    private var innerProgressWidth = 20f //内圆宽度
    private var innerPadding = 2f //外部进度条与内部进度条的边距
    private var thumb: Drawable? = null
    private var clockWise = true //true顺时针，false逆时针

    private var rotation = 0 //旋转了多少的值，不是度数
    private var sumRotation = 360 //该控件所能表示的最大值

    private var sweepAngle = 360 //画一个多少度的控件，默认是个360度的圆
    private var progressSweepAngle = 0 //progress滑动角度，需要根据rotation和sumRotationo进行计算
    private var startAngle = 0 //起始角度
    private var currentAngle = 0.0 //在9点钟方向是270,12点钟方向是360.一圈从0-360的圆中当前的角度

    private var correctAngle = 0 //纠正角度，默认从9点钟方向开始的，顺时针调整correctAngle度

    private var outerPaint: Paint? = null
    private var innerBgPaint: Paint? = null
    private var innerPaint: Paint? = null //分别：外圆画笔，内圆背景画笔，内圆画笔
    private val outerRectF = RectF()
    private val innerRectF = RectF()

    private var translateX = 0
    private var translateY = 0
    private var thumbX = 0
    private var thumbY = 0
    private var circleRadius: Float = 0.toFloat() //外圆半径
    private var forbiddenRadius = 0f //禁止点击的范围

    private var changeListener: GeekSeekBarOnChangeListener? = null

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet){
        initViewAttr(context, attributeSet, R.attr.tecCircleSeekBarStyle)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr){
        initViewAttr(context, attributeSet, defStyleAttr)
    }

    /**
     * 初始化参数
     * @param context Context
     * *
     * @param attrs AttributeSet
     * *
     * @param defStyleAttr int
     */
    private fun initViewAttr(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val density = context.resources.displayMetrics.density //转为像素
        //默认的外圆宽度和颜色
        progressWidth *= density
        var progressColor = ContextCompat.getColor(context, R.color.colorTecCircleSeekBarProgress) //进度条的默认颜色

        //默认的内圆宽度和颜色
        innerProgressWidth *= density
        var innerProgressBgColor = ContextCompat.getColor(context, R.color.colorTecCircleSeekBarProgressInnerBg) //进度条内圆的默认背景颜色
        var innerProgressColor = ContextCompat.getColor(context, R.color.colorTecCircleSeekBarProgressInner) //进度条内圆的默认颜色

        //内边距，避免thumb遮挡内圆
        innerPadding *= density

        thumb = ContextCompat.getDrawable(context, R.drawable.teccircleseekbar_thumb) //默认的thumb资源

        //在开发者设置了自己的属性的时候，使用开发者定义的属性
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TecCircleSeekBarStyle, defStyleAttr, 0)
            //开发者设置的外圆宽度和颜色
            progressWidth = typedArray.getDimension(R.styleable.TecCircleSeekBarStyle_outerWidth, progressWidth)
            progressColor = typedArray.getColor(R.styleable.TecCircleSeekBarStyle_outerColor, progressColor)

            //开发者设置的内圆宽度和颜色
            innerProgressWidth = typedArray.getDimension(R.styleable.TecCircleSeekBarStyle_innerWidth, innerProgressWidth)
            innerProgressBgColor = typedArray.getColor(R.styleable.TecCircleSeekBarStyle_innerBgColor, innerProgressBgColor)
            innerProgressColor = typedArray.getColor(R.styleable.TecCircleSeekBarStyle_innerColor, innerProgressColor)

            //内边距
            innerPadding = typedArray.getDimension(R.styleable.TecCircleSeekBarStyle_innerPadding, innerPadding)
            //旋转的值
            rotation = typedArray.getInt(R.styleable.TecCircleSeekBarStyle_rotation, rotation)
            //总值
            sumRotation = typedArray.getInt(R.styleable.TecCircleSeekBarStyle_sumRotation, sumRotation)

            //顺时针还是逆时针
            clockWise = typedArray.getBoolean(R.styleable.TecCircleSeekBarStyle_clockWise, clockWise)

            //控件度数
            sweepAngle = typedArray.getInt(R.styleable.TecCircleSeekBarStyle_sweepAngle, sweepAngle)

            //只能放在rotation、sumRotation、sweepAngle设置值之后
            progressSweepAngle = Math.round(rotation.toDouble() / sumRotation * sweepAngle).toInt()

            //只有360度的圆圈可以有矫正度数
            if (sweepAngle == 360) {
                //纠正角度
                correctAngle = typedArray.getInt(R.styleable.TecCircleSeekBarStyle_correctAngle, correctAngle)
                //防止纠正角度超限
                if (correctAngle < 0) {
                    correctAngle = 0
                } else if (correctAngle > sweepAngle) {
                    correctAngle = sweepAngle
                }
            }

            //thumb
            val customThumb = typedArray.getDrawable(R.styleable.TecCircleSeekBarStyle_thumb)
            if (customThumb != null) {
                thumb = customThumb
            }
            val thumbHalfWidth = thumb!!.intrinsicWidth / 2
            val thumbHalfHeight = thumb!!.intrinsicHeight / 2
            thumb!!.setBounds(-thumbHalfWidth, -thumbHalfHeight, thumbHalfWidth, thumbHalfHeight)
            typedArray.recycle()

        }


        //绘制外圆
        val progressEffect = PathEffect()
        outerPaint = Paint()
        outerPaint!!.color = progressColor
        outerPaint!!.isAntiAlias = true
        outerPaint!!.style = Paint.Style.STROKE
        outerPaint!!.strokeWidth = progressWidth
        outerPaint!!.pathEffect = progressEffect

        val innerBgEffect = DashPathEffect(floatArrayOf(8f, 2f, 8f, 2f), 1f)
        innerBgPaint = Paint()
        innerBgPaint!!.color = innerProgressBgColor
        innerBgPaint!!.isAntiAlias = true
        innerBgPaint!!.style = Paint.Style.STROKE
        innerBgPaint!!.strokeWidth = innerProgressWidth
        innerBgPaint!!.pathEffect = innerBgEffect

        val innerProgressEffect = PathEffect()
        innerPaint = Paint()
        innerPaint!!.color = innerProgressColor
        innerPaint!!.isAntiAlias = true
        innerPaint!!.style = Paint.Style.STROKE
        innerPaint!!.strokeWidth = innerProgressWidth
        innerPaint!!.pathEffect = innerProgressEffect
    }

    /**
     * 设置成多少值
     * @param value 要设置显示的值
     */
    fun setRotation(value: Int) {
        this.rotation = value
        update(rotation, false)
    }

    /**
     * 设置该控件所能便是的最大值
     * @param sumRotation 表示的最大值
     */
    fun setSumRotation(sumRotation: Int) {
        this.sumRotation = sumRotation
        update(rotation, false)
    }

    /**
     * 设置监听接口
     * @param listener GeekSeekBarOnChangeListener
     */
    fun setChangeListener(listener: GeekSeekBarOnChangeListener) {
        this.changeListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        if (!clockWise) {
            // 开发者设置为逆时针
            canvas.scale(-1f, 1f, outerRectF.centerX(), outerRectF.centerY())
            canvas.scale(-1f, 1f, innerRectF.centerX(), innerRectF.centerY())
        }

        val circleStartAngle: Int
        if (clockWise) {
            circleStartAngle = (startAngle + correctAngle - 90) % 360
            canvas.drawArc(outerRectF, circleStartAngle.toFloat(), sweepAngle.toFloat(), false, outerPaint!!)
            canvas.drawArc(innerRectF, circleStartAngle.toFloat(), sweepAngle.toFloat(), false, innerBgPaint!!)
            canvas.drawArc(innerRectF, circleStartAngle.toFloat(), progressSweepAngle.toFloat(), false, innerPaint!!)
        } else {
            circleStartAngle = startAngle - correctAngle - 90 + sweepAngle
            canvas.drawArc(outerRectF, circleStartAngle.toFloat(), (-sweepAngle).toFloat(), false, outerPaint!!)
            canvas.drawArc(innerRectF, circleStartAngle.toFloat(), (-sweepAngle).toFloat(), false, innerBgPaint!!)
            canvas.drawArc(innerRectF, circleStartAngle.toFloat(), (-progressSweepAngle).toFloat(), false, innerPaint!!)
        }
        canvas.translate((translateX - thumbX).toFloat(), (translateY - thumbY).toFloat()) //移动画布原点
        thumb!!.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = Math.min(height, width) //选择设置的宽和高中最小的那一个，按照正方形绘制
        val top: Float
        val left: Float
        val arcDiameter: Float //外圆直径


        translateX = (width * 0.5f).toInt()
        translateY = (height * 0.5f).toInt()
        if (thumb != null) {
            arcDiameter = min.toFloat() - progressWidth - thumb!!.intrinsicWidth.toFloat() //、、这里做了修改
        } else {
            arcDiameter = min - progressWidth
        }
        circleRadius = arcDiameter / 2 //外圆半径
        //将圆画在控件宽高的长方形内正中心，计算出top和left值
        top = (height - arcDiameter) / 2 //顶部到这里
        left = (width - arcDiameter) / 2 //左边到这里

        //内圆参数
        val innerDiameter = arcDiameter - 2 * innerPadding - 2 * innerProgressWidth //内部空白直径
        val innerLeft: Float
        val innerTop: Float
        innerTop = (height - innerDiameter) / 2
        innerLeft = (width - innerDiameter) / 2

        outerRectF.set(left, top, left + arcDiameter, top + arcDiameter)
        innerRectF.set(innerLeft, innerTop, innerLeft + innerDiameter, innerTop + innerDiameter)

        forbiddenRadius = innerDiameter / 2 - 10 //该半径范围内禁止点击


        startAngle = (360 - sweepAngle / 2) % 360 //起始角度

        currentAngle = (startAngle + progressSweepAngle).toDouble()
        updateThumbPosition()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.parent.requestDisallowInterceptTouchEvent(true)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                onStartTrackingTouch()
                updateOnTouch(event)
            }
            MotionEvent.ACTION_MOVE -> updateOnTouch(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onStopTrackingTouch()
                this.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**
     * 开始监听触摸事件
     */
    private fun onStartTrackingTouch() {
        if (changeListener != null) {
            changeListener!!.onStartTrackingTouch(this)
        }
    }

    /**
     * 停止监听触摸事件
     */
    private fun onStopTrackingTouch() {
        if (changeListener != null) {
            changeListener!!.onStopTrackingTouch(this)
        }
        isPressed = false
    }

    /**
     * 更新控件上各组件的位置
     * @param event 触摸事件
     */
    private fun updateOnTouch(event: MotionEvent) {
        //该区域禁止点击，为了优化用户体验，内部空白处不允许点击
        val forbidden = forbiddenTouch(event.x, event.y)
        if (forbidden) {
            return
        }
        isPressed = true
        val currentSweepAngle = releativeMoveAngle(event.x, event.y).toInt()
        //防止进度条和进度条背景越界
        if (currentAngle >= 360 - sweepAngle / 2 || currentAngle <= sweepAngle / 2) {
            progressSweepAngle = currentSweepAngle //触摸过程中走了多少度
            rotation = angle2Rotation(progressSweepAngle.toDouble())
            update(rotation, true)
        }
    }

    private fun update(rotation: Int, fromUser: Boolean) {
        if (rotation > sumRotation) {
            this.rotation = sumRotation
        }
        if (!fromUser) {
            progressSweepAngle = Math.round(this.rotation.toDouble() / sumRotation * sweepAngle).toInt()
        }
        onProgressRefresh(this.rotation, fromUser)
    }

    /**
     * 设定的范围内不允许触摸
     */
    private fun forbiddenTouch(eventX: Float, eventY: Float): Boolean {
        var forbidden = false
        val x = eventX - translateX
        val y = eventY - translateY

        val touchRadius = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        if (touchRadius < forbiddenRadius) {
            forbidden = true
        }
        return forbidden
    }

    /**
     * 相对设置的初始点移动的角度
     * @param eventX 点击事件的X值
     * *
     * @param eventY 点击事件的X值
     * *
     * @return 移动的距离 double
     */
    private fun releativeMoveAngle(eventX: Float, eventY: Float): Double {
        var x = eventX - translateX
        val y = eventY - translateY
        x = if (clockWise) x else -x
        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()) + Math.PI / 2)
        if (angle < 0) {
            //修正度数，让9点钟方向是270,12点钟方向是360.一圈从0-360
            angle += 360
        }
        currentAngle = angle - correctAngle
        if (currentAngle > startAngle) {
            angle = currentAngle - startAngle
        } else {
            angle = currentAngle + 360 - startAngle
        }
        return angle
    }

    /**
     * 将旋转角度转为旋转的值
     * @param angle 旋转角度
     * *
     * @return 角度对应的值
     */
    private fun angle2Rotation(angle: Double): Int {
        var rotation = sumRotation.toDouble() / sweepAngle * angle
        if (rotation < 0 || rotation > sumRotation) {
            rotation = INVALID_ROTATION.toDouble()//不可能发生了，前面已经做了限制
        }
        return Math.round(rotation).toInt()
    }

    private fun onProgressRefresh(rotation: Int, fromUser: Boolean) {
        if (rotation == INVALID_ROTATION) {
            //无效的值
            return
        }
        if (changeListener != null) {
            changeListener!!.onProgressChanged(this, rotation, fromUser)
        }
        updateThumbPosition()
        invalidate()
    }

    /**
     * 更新thunb的位置
     */
    private fun updateThumbPosition() {
        val thumbAngle: Double
        if (clockWise) {
            thumbAngle = (currentAngle + 90.0 + correctAngle.toDouble()) % 360
        } else {
            thumbAngle = (360.0 - (currentAngle - 90) - correctAngle.toDouble()) % 360
        }
        thumbX = (circleRadius * Math.cos(Math.toRadians(thumbAngle))).toInt()
        thumbY = (circleRadius * Math.sin(Math.toRadians(thumbAngle))).toInt()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (thumb != null && thumb!!.isStateful) {
            val state = drawableState
            thumb!!.state = state
        }
        invalidate()
    }

    companion object {
        private val INVALID_ROTATION = -1
    }

}