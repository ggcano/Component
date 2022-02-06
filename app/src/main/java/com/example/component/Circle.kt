package com.example.component


import android.content.Context
import android.graphics.*
import android.os.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.*


class Circle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mWheelPaint: Paint? = null
    private var mWheelPaintWhite: Paint? = null
    var mReachedPaint: Paint? = null
    private var mReachedEdgePaint: Paint? = null
    private var mThumbPaint: Paint? = null
    private var mPointerPaintStroke: Paint? = null
    private var mCircleStatic: Paint? = null
    private var mArcCirclePaint: Paint? = null
    var mCircleStaticGreyMinus: Paint? = null
    var mCircleArcStaticWhite: Paint? = null
    var thumbStaticGreyInit: Paint? = null
    private var mMaxProcess = 0
    private var mCurProcess = 0
    private var mUnreachedRadius = 0f
    private var mReachedColor = 0
    private var mUnreachedColor = 0
    private var mReachedWidth = 45f
    private var mUnreachedWidth = 45f
    private var isHasReachedCornerRound = false
    private var mThumbColor = 0
    private var mPointerRadius = 0f
    private var mCurAngle = 0.0
    private var mWheelCurX = 0f
    private var mWheelCurY = 0f
    private var isHasWheelShadow = false
    private var isHasPointerShadow = false
    private var mWheelShadowRadius = 0f
    private var mPointerShadowRadius = 0f
    private var isHasCache = false
    private var mCacheCanvas: Canvas? = null
    lateinit var mCacheBitmap: Bitmap
    private var isCanTouch = false
    private var isScrollOneCircle = false
    private var mDefShadowOffset = 0f
    var currentValue: Int = 0
        set(value) {
            field = value
            setCurrentProcess(value)
        }
    var maxValue: Int = 100
        set(value) {
            field = value
            mMaxProcess = value
        }
    var isCheckIcon = false



    var selectorColor: Map<Int, Pair<Int, Int>> = ColorState.defaultColorSector
        set(value) {
            field = value
            paintSectors(value,currentValue)
        }

    var configurationList: Config? = null
        set(value) {
            field = value
            maxValue = value?.maxValue ?: 100
            isCheckIcon = value?.useIcon ?: false
            value?.colorSector?.let { selectorColor = it }
            value?.currentValue?.let { setCurrentProcess(it) }
        }

    var onChangedValue: (Int) -> Unit = {

    }

    var onMaxValue: (Unit) -> Unit = {

    }

    private fun initPaints() {
        mDefShadowOffset = getDimen(R.dimen.def_shadow_offset)
        mWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = mUnreachedColor
                style = Paint.Style.STROKE
                strokeWidth = mUnreachedWidth
                if (isHasWheelShadow) {
                    setShadowLayer(
                        mWheelShadowRadius,
                        mDefShadowOffset,
                        mDefShadowOffset,
                        Color.DKGRAY
                    )
                }
            }

        mWheelPaintWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = mUnreachedWidth + R.dimen.size_4.dpToPx()
        }

        mReachedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mReachedColor
            style = Paint.Style.STROKE
            strokeWidth = mReachedWidth
            if (isHasReachedCornerRound) {
                strokeCap = Paint.Cap.BUTT
            }
        }


        mCircleStatic = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = mUnreachedColor
                style = Paint.Style.FILL
                strokeWidth = mReachedWidth
            }

        mArcCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = mUnreachedColor
                style = Paint.Style.STROKE
                strokeWidth = mReachedWidth
                strokeCap = Paint.Cap.ROUND
            }

        mCircleStaticGreyMinus = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL
            }

        mCircleArcStaticWhite = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }


        thumbStaticGreyInit = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = ContextCompat.getColor(context, R.color.circle_static_thumb_init)
                style = Paint.Style.FILL_AND_STROKE
            }

        mThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = mThumbColor
                style = Paint.Style.FILL

            }
        mPointerPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 10f
                if (isHasPointerShadow) {
                    setShadowLayer(
                        mPointerShadowRadius,
                        mDefShadowOffset,
                        mDefShadowOffset,
                        Color.GRAY
                    )
                }
            }


        mReachedEdgePaint = Paint(mReachedPaint)
            .apply {
                style = Paint.Style.FILL
            }
    }

    private fun initAttrs(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, defStyle, 0)
        mMaxProcess = a.getInt(R.styleable.CircleSeekBar_wheel_max_process, 100)
        mCurProcess = a.getInt(R.styleable.CircleSeekBar_wheel_cur_process, 0)
        if (mCurProcess > mMaxProcess) mCurProcess = mMaxProcess
        mReachedColor = a.getColor(
            R.styleable.CircleSeekBar_wheel_reached_color,
            ContextCompat.getColor(context, R.color.red_dark)
        )
        mUnreachedColor = a.getColor(
            R.styleable.CircleSeekBar_wheel_unreached_color,
            ContextCompat.getColor(context, R.color.def_wheel_color)
        )
        mUnreachedWidth = a.getDimension(
            R.styleable.CircleSeekBar_wheel_unreached_width,
            getDimen(R.dimen.def_wheel_width)
        )
        isHasReachedCornerRound =
            a.getBoolean(R.styleable.CircleSeekBar_wheel_reached_has_corner_round, true)
        mReachedWidth =
            a.getDimension(R.styleable.CircleSeekBar_wheel_reached_width, mUnreachedWidth)
        mThumbColor = a.getColor(
            R.styleable.CircleSeekBar_wheel_pointer_color,
            ContextCompat.getColor(context, R.color.def_pointer_color)
        )
        mPointerRadius =
            a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_radius, mReachedWidth / 2)
        isHasWheelShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_wheel_shadow, false)
        if (isHasWheelShadow) {
            mWheelShadowRadius = a.getDimension(
                R.styleable.CircleSeekBar_wheel_shadow_radius,
                getDimen(R.dimen.def_shadow_radius)
            )
        }
        isHasPointerShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_pointer_shadow, false)
        if (isHasPointerShadow) {
            mPointerShadowRadius = a.getDimension(
                R.styleable.CircleSeekBar_wheel_pointer_shadow_radius,
                getDimen(R.dimen.def_shadow_radius)
            )
        }
        isHasCache = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_cache, isHasWheelShadow)
        isCanTouch = a.getBoolean(R.styleable.CircleSeekBar_wheel_can_touch, true)
        isScrollOneCircle =
            a.getBoolean(R.styleable.CircleSeekBar_wheel_scroll_only_one_circle, false)

        a.recycle()
    }

    private fun initPadding() {
        val paddingStart = 0
        val paddingEnd = 0
        val maxPadding = max(
            paddingLeft, max(
                paddingTop,
                max(paddingRight, max(paddingBottom, max(paddingStart, paddingEnd)))
            )
        )
        setPadding(maxPadding, maxPadding, maxPadding, maxPadding)
    }

    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
        refreshPosition()
        refreshUnreachedWidth()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        val left = paddingLeft + mUnreachedWidth / 2
        val top = paddingTop + mUnreachedWidth / 2
        val right = width - paddingRight - mUnreachedWidth / 2
        val bottom = height - paddingBottom - mUnreachedWidth / 2
        val centerX = (left + right) / 2
        val margin1dp = R.dimen.size_1.dpToPx()
        val rectangle = RectF(left, top, right, bottom)

        mCircleArcStaticWhite?.let {
            canvas?.drawCircle(
                centerX + margin1dp,
                paddingTop + mUnreachedWidth / 2,
                (mReachedWidth / 2) + margin1dp,
                it
            )
        }

        mCircleStatic?.let {
            canvas?.drawCircle(
                centerX,
                paddingTop + mUnreachedWidth / 2,
                mReachedWidth / 2,
                it
            )
        }
        mArcCirclePaint?.let {
            canvas?.drawArc(rectangle, 270f, -2f, false, it)
        }

        mCircleStaticGreyMinus?.let {
            canvas?.drawCircle(
                centerX,
                paddingTop + mUnreachedWidth / 2,
                mReachedWidth / 5,
                it
            )
        }

        configurationList?.dottedPositionList?.let { list ->
            list.forEach {
                val point = getGreyFromCurve(it)
                thumbStaticGreyInit?.let { thumb ->
                    canvas?.drawCircle(
                        point.first,
                        point.second,
                        mReachedWidth / 5,
                        thumb
                    )
                }
            }
        }

        super.dispatchDraw(canvas)
        mPointerPaintStroke?.let { canvas?.drawCircle(mWheelCurX, mWheelCurY, mPointerRadius, it) }
        mThumbPaint?.let { canvas?.drawCircle(mWheelCurX, mWheelCurY, mPointerRadius - 5, it) }



    }


    override fun onDraw(canvas: Canvas) {

        val left = paddingLeft + mUnreachedWidth / 2
        val top = paddingTop + mUnreachedWidth / 2
        val right = width - paddingRight - mUnreachedWidth / 2
        val bottom = height - paddingBottom - mUnreachedWidth / 2
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2
        val wheelRadius = (width - paddingLeft - paddingRight) / 2 - mUnreachedWidth / 2

        if (isHasCache) {
            if (mCacheCanvas == null) {
                buildCache(centerX, centerY, wheelRadius)
            }
            mCacheBitmap.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }

        } else {
            mWheelPaintWhite?.let { canvas.drawCircle(centerX, centerY, wheelRadius, it) }
            mWheelPaint?.let { canvas.drawCircle(centerX, centerY, wheelRadius, it) }

        }
        mReachedPaint?.let {
            canvas.drawArc(
                getRectF(left, top, right, bottom),
                -90f,
                mCurAngle.toFloat(),
                false,
                it
            )
        }

    }

    private fun getRectF(left: Float, top: Float, right: Float, bottom: Float) =
        RectF(left, top, right, bottom)

    private fun Int.dpToPx() = resources.getDimensionPixelSize(this)


    private fun buildCache(centerX: Float, centerY: Float, wheelRadius: Float) {
        mCacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCacheCanvas = Canvas(mCacheBitmap)
        mWheelPaint?.let { mCacheCanvas?.drawCircle(centerX, centerY, wheelRadius, it) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        val x = event.x
        val y = event.y
        return if (isCanTouch && isCanGoBack(event) && (event.action == MotionEvent.ACTION_MOVE || isTouch(
                x,
                y
            ))
        ) {
            // compute
            var cos = computeCos(x, y)
            // x,y
            val angle: Double = if (x < width / 2) { // 180
                Math.PI * RADIAN + acos(cos.toDouble()) * RADIAN
                // val x: Float = r * cos(t) + h
            } else { // 180
                Math.PI * RADIAN - acos(cos.toDouble()) * RADIAN
            }
            if (isScrollOneCircle) {
                if (mCurAngle > 270 && angle < 90) {
                    mCurAngle = 360.0
                    cos = -1f
                } else if (mCurAngle < 90 && angle > 270) {
                    mCurAngle = 0.0
                    cos = -1f
                } else {
                    mCurAngle = angle
                }
            } else {
                mCurAngle = angle
            }
            val oldCurrentProcess = mCurProcess
            mCurProcess = getSelectedValue()
            refreshWheelCurPosition(cos.toDouble())
            if (event.action and (MotionEvent.ACTION_MOVE or MotionEvent.ACTION_UP) > 0) {
                paintSectors(currentValue = mCurProcess)
                onChangedValue(mCurProcess)
                onMaxValue.takeIf { mCurProcess == mMaxProcess }?.invoke(Unit)
            }
            invalidate()

            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun doPaintSector(colorSector: Pair<Int, Int>, currentValue: Int) {
        val right = width - paddingRight - mUnreachedWidth / 2

        setPointerColor(ContextCompat.getColor(context, colorSector.second), currentValue)
        mReachedPaint?.shader = LinearGradient(
            0f,
            0f,
            right,
            0f,
            ContextCompat.getColor(context, colorSector.second),
            ContextCompat.getColor(context, colorSector.first),
            Shader.TileMode.CLAMP
        )
    }

    private fun paintSectors(
        mapColor: Map<Int, Pair<Int, Int>> = selectorColor
            ?: mapOf(), currentValue: Int
    ) {
        if (mapColor.size > 1) {
            val keySet = mapColor.keys.toIntArray()
            for (key in keySet) {
                when {
                    key == keySet.first() && 0 <= currentValue && currentValue <= key.toRuleOfThree() -> {
                        mapColor[key]?.let {
                            doPaintSector(it, currentValue)
                        }
                        break
                    }
                    key == keySet.last() && keySet[keySet.size - 2].toRuleOfThree() <= currentValue && currentValue <= key.toRuleOfThree() -> {
                        mapColor[key]?.let { doPaintSector(it, currentValue) }
                        break
                    }
                    key != keySet.first() && key != keySet.last() && keySet[keySet.indexOf(key) - 1].toRuleOfThree() < currentValue && currentValue <= key.toRuleOfThree() -> {
                        mapColor[key]?.let { doPaintSector(it, currentValue) }
                        break
                    }
                }
            }
        } else if (mapColor.size == 1) {
            mapColor[mapColor.keys.toIntArray().firstOrNull()]?.let {
                doPaintSector(
                    it,
                    currentValue
                )
            }
        }
        isCheckIcon = configurationList?.useIcon == true && currentValue == maxValue
    }

    private fun isCanGoBack(event: MotionEvent): Boolean {
        return if (configurationList?.lockMode?.not() == true)
            true
        else {
            configurationList?.initValue ?: 0 <= calculateCurrentProcess(event)
        }

    }

    private fun calculateCurrentProcess(event: MotionEvent): Int {
        // compute
        val cos = computeCos(event.x, event.y)
        // x,y
        val angle: Double = if (event.x < width / 2) { // 180
            Math.PI * RADIAN + acos(cos.toDouble()) * RADIAN
        } else { // 180
            Math.PI * RADIAN - acos(cos.toDouble()) * RADIAN
        }

        val curAngle: Double = if (isScrollOneCircle) {
            if (mCurAngle > 270 && angle < 90) {
                360.0
            } else if (mCurAngle < 90 && angle > 270) {
                0.0
            } else {
                angle
            }
        } else {
            angle
        }
        return getSelectedValue(curAngle)
    }


    private fun isTouch(x: Float, y: Float): Boolean {
        val radius = ((width - paddingLeft - paddingRight + getCircleWidth()) / 2).toDouble()
        val centerX = (width / 2).toDouble()
        val centerY = (height / 2).toDouble()
        return (centerX - x).pow(2.0) + (centerY - y).pow(2.0) < radius * radius
    }

    private fun getCircleWidth(): Float {
        return mUnreachedWidth.coerceAtLeast(max(mReachedWidth, mPointerRadius))
    }

    private fun refreshUnreachedWidth() {
        mUnreachedRadius = (measuredWidth - paddingLeft - paddingRight - mUnreachedWidth) / 2
    }

    private fun refreshWheelCurPosition(cos: Double) {
        mWheelCurX = calcXLocationInWheel(mCurAngle, cos)
        mWheelCurY = calcYLocationInWheel(cos)
    }

    private fun getGreyFromCurve(value: Int): Pair<Float, Float> {
        val angle = getStaticPoint(value)
        val cos = -cos(Math.toRadians(angle))
        val x = calcXLocationInWheel(angle, cos)
        val y = calcYLocationInWheel(cos)
        return Pair(x, y)
    }

    private fun refreshPosition() {
        mCurAngle = mCurProcess.toDouble() / mMaxProcess * 360.0
        val cos = -cos(Math.toRadians(mCurAngle))
        refreshWheelCurPosition(cos)
    }

    private fun calcXLocationInWheel(angle: Double, cos: Double): Float {
        return if (angle < 180) {
            (measuredWidth / 2 + sqrt(1 - cos * cos) * mUnreachedRadius).toFloat()
        } else {
            (measuredWidth / 2 - sqrt(1 - cos * cos) * mUnreachedRadius).toFloat()
        }
    }

    private fun calcYLocationInWheel(cos: Double): Float {
        return measuredWidth / 2 + mUnreachedRadius * cos.toFloat()
    }

    private fun computeCos(x: Float, y: Float): Float {
        val width = x - width / 2
        val height = y - height / 2
        val slope = sqrt((width * width + height * height).toDouble()).toFloat()
        return height / slope
    }

    private fun Int.toRuleOfThree(maxValue: Int = this@Circle.maxValue) =
        maxValue * this / 100

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_MAX_PROCESS, mMaxProcess)
        bundle.putInt(INSTANCE_CUR_PROCESS, mCurProcess)
        bundle.putInt(INSTANCE_REACHED_COLOR, mReachedColor)
        bundle.putFloat(INSTANCE_REACHED_WIDTH, mReachedWidth)
        bundle.putBoolean(INSTANCE_REACHED_CORNER_ROUND, isHasReachedCornerRound)
        bundle.putInt(INSTANCE_UNREACHED_COLOR, mUnreachedColor)
        bundle.putFloat(INSTANCE_UNREACHED_WIDTH, mUnreachedWidth)
        bundle.putInt(INSTANCE_POINTER_COLOR, mThumbColor)
        bundle.putFloat(INSTANCE_POINTER_RADIUS, mPointerRadius)
        bundle.putBoolean(INSTANCE_POINTER_SHADOW, isHasPointerShadow)
        bundle.putFloat(INSTANCE_POINTER_SHADOW_RADIUS, mPointerShadowRadius)
        bundle.putBoolean(INSTANCE_WHEEL_SHADOW, isHasWheelShadow)
        bundle.putFloat(INSTANCE_WHEEL_SHADOW_RADIUS, mPointerShadowRadius)
        bundle.putBoolean(INSTANCE_WHEEL_HAS_CACHE, isHasCache)
        bundle.putBoolean(INSTANCE_WHEEL_CAN_TOUCH, isCanTouch)
        bundle.putBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE, isScrollOneCircle)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            mMaxProcess = state.getInt(INSTANCE_MAX_PROCESS)
            mCurProcess = state.getInt(INSTANCE_CUR_PROCESS)
            mReachedColor = state.getInt(INSTANCE_REACHED_COLOR)
            mReachedWidth = state.getFloat(INSTANCE_REACHED_WIDTH)
            isHasReachedCornerRound = state.getBoolean(INSTANCE_REACHED_CORNER_ROUND)
            mUnreachedColor = state.getInt(INSTANCE_UNREACHED_COLOR)
            mUnreachedWidth = state.getFloat(INSTANCE_UNREACHED_WIDTH)
            mThumbColor = state.getInt(INSTANCE_POINTER_COLOR)
            mPointerRadius = state.getFloat(INSTANCE_POINTER_RADIUS)
            isHasPointerShadow = state.getBoolean(INSTANCE_POINTER_SHADOW)
            mPointerShadowRadius = state.getFloat(INSTANCE_POINTER_SHADOW_RADIUS)
            isHasWheelShadow = state.getBoolean(INSTANCE_WHEEL_SHADOW)
            mPointerShadowRadius = state.getFloat(INSTANCE_WHEEL_SHADOW_RADIUS)
            isHasCache = state.getBoolean(INSTANCE_WHEEL_HAS_CACHE)
            isCanTouch = state.getBoolean(INSTANCE_WHEEL_CAN_TOUCH)
            isScrollOneCircle = state.getBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE)
            initPaints()
        } else {
            super.onRestoreInstanceState(state)
        }
        paintSectors(currentValue = mCurProcess)
        onChangedValue(mCurProcess)
        onMaxValue.takeIf { mCurProcess == mMaxProcess }?.invoke(Unit)

    }

    private fun getSelectedValue(curleAngle: Double = mCurAngle): Int {
        return (mMaxProcess * (curleAngle.toFloat() / 360)).roundToInt()
    }

    private fun getStaticPoint(selectedValue: Int): Double {
        return ((selectedValue * 360) / mMaxProcess).toDouble()
    }

    private fun setCurrentProcess(curProcess: Int) {
        mCurProcess = if (curProcess > mMaxProcess) mMaxProcess else curProcess
        paintSectors(currentValue = curProcess)

        refreshPosition()
        invalidate()
    }

    private fun setPointerColor(pointerColor: Int, currentValue: Int) {
        val color = if (currentValue == 0) {
            mUnreachedColor
        } else {
            pointerColor
        }
        mThumbColor = color
        mThumbPaint?.color = color
    }





    companion object {
        private const val RADIAN = 180 / Math.PI
        private const val INSTANCE_STATE = "state"
        private const val INSTANCE_MAX_PROCESS = "max_process"
        private const val INSTANCE_CUR_PROCESS = "cur_process"
        private const val INSTANCE_REACHED_COLOR = "reached_color"
        private const val INSTANCE_REACHED_WIDTH = "reached_width"
        private const val INSTANCE_REACHED_CORNER_ROUND = "reached_corner_round"
        private const val INSTANCE_UNREACHED_COLOR = "unreached_color"
        private const val INSTANCE_UNREACHED_WIDTH = "unreached_width"
        private const val INSTANCE_POINTER_COLOR = "pointer_color"
        private const val INSTANCE_POINTER_RADIUS = "pointer_radius"
        private const val INSTANCE_POINTER_SHADOW = "pointer_shadow"
        private const val INSTANCE_POINTER_SHADOW_RADIUS = "pointer_shadow_radius"
        private const val INSTANCE_WHEEL_SHADOW = "wheel_shadow"
        private const val INSTANCE_WHEEL_SHADOW_RADIUS = "wheel_shadow_radius"
        private const val INSTANCE_WHEEL_HAS_CACHE = "wheel_has_cache"
        private const val INSTANCE_WHEEL_CAN_TOUCH = "wheel_can_touch"
        private const val INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE = "wheel_scroll_only_one_circle"
    }


    init {
        initAttrs(attrs, defStyleAttr)
        initPadding()
        initPaints()
    }

    data class Config(
        val initValue: Int? = null,
        var currentValue: Int? = 0,
        var maxValue: Int = 100,
        val useIcon: Boolean = false,
        val lockMode: Boolean = false,
        val colorSector: Map<Int, Pair<Int, Int>> = ColorState.defaultColorSector,
        val dottedPositionList: List<Int> = listOf()
    )

    object ColorState {
        val defaultColorSector =
            mutableMapOf<Int, Pair<Int, Int>>().apply {
                put(33, Pair(R.color.red_dark, R.color.red_light))
                put(66, Pair(R.color.yellow_dark, R.color.yellow_light))
                put(100, Pair(R.color.green_dark, R.color.green_light))
            }.toMap()

    }



}

