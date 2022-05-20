package com.jiangyt.library.rating

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.abs


/**
 * @Title: NTCloud
 * @Package com.niu.view.rating
 * @Description: RatingStar is specific RatingBar use star drawable as the progress mark.
 * @author apple
 * @date 2022/5/20 9:32 上午
 * @version V1.0
 */
class RatingStarView : View, View.OnClickListener {

    companion object {
        private const val TAG = "RatingStarView"
        private const val DEFAULT_STAR_HEIGHT = 32
    }

    private var cornerRadius = 4f

    @ColorInt
    private var starForegroundColor: Int = Color.parseColor("#ED4A4B")

    @ColorInt
    private var strokeColor: Int = starForegroundColor

    @ColorInt
    private var starBackgroundColor: Int = Color.WHITE

    /** used to make round smooth star horn  */
    private var pathEffect: CornerPathEffect
    private var paint: Paint

    private var starList: ArrayList<StarModel>? = null
    private var rating = 0f

    /**
     * expected star number.
     */
    private var starNum = 5

    /**
     * real drawn star number.
     */
    private var starCount = 0

    /** calculated value  */
    private var starWidth = 0f

    /** calculated value  */
    private var starHeight = 0f
    private var starMargin = 8f
    private var strokeWidth = 2f
    private var drawStrokeForFullStar = false
    private var drawStrokeForHalfStar = true
    private var drawStrokeForEmptyStar = true
    private var enableSelectRating = false
    private var onlyHalfStar = true
    private var clickHalfStar = true
    private var starThicknessFactor = StarModel.DEFAULT_THICKNESS
    private var dividerX = 0f
    private var clickedX = 0f
    private var clickedY = 0f

    private var mOuterOnClickListener: OnClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        loadAttributes(attrs, defStyle)
        // init paint
        paint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            strokeWidth = this@RatingStarView.strokeWidth
        }
        // properties
        pathEffect = CornerPathEffect(cornerRadius)
        // click to rate
        super.setOnClickListener(this)
    }

    private fun loadAttributes(attrs: AttributeSet?, defStyle: Int) {
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.RatingStarView, defStyle, 0)
            strokeColor =
                typedArray.getColor(R.styleable.RatingStarView_rsv_strokeColor, strokeColor)
            starForegroundColor = typedArray.getColor(
                R.styleable.RatingStarView_rsv_starForegroundColor,
                starForegroundColor
            )
            starBackgroundColor = typedArray.getColor(
                R.styleable.RatingStarView_rsv_starBackgroundColor,
                starBackgroundColor
            )
            cornerRadius =
                typedArray.getDimension(R.styleable.RatingStarView_rsv_cornerRadius, cornerRadius)
            starMargin =
                typedArray.getDimension(R.styleable.RatingStarView_rsv_starMargin, starMargin)
            strokeWidth =
                typedArray.getDimension(R.styleable.RatingStarView_rsv_strokeWidth, strokeWidth)
            starThicknessFactor = typedArray.getFloat(
                R.styleable.RatingStarView_rsv_starThickness,
                starThicknessFactor
            )
            rating = typedArray.getFloat(R.styleable.RatingStarView_rsv_rating, rating)
            starNum = typedArray.getInteger(R.styleable.RatingStarView_rsv_starNum, starNum)
            drawStrokeForEmptyStar =
                typedArray.getBoolean(R.styleable.RatingStarView_rsv_drawStrokeForEmptyStar, true)
            drawStrokeForFullStar =
                typedArray.getBoolean(R.styleable.RatingStarView_rsv_drawStrokeForFullStar, false)
            drawStrokeForHalfStar =
                typedArray.getBoolean(R.styleable.RatingStarView_rsv_drawStrokeForHalfStar, true)
            enableSelectRating =
                typedArray.getBoolean(R.styleable.RatingStarView_rsv_enableSelectRating, false)
            onlyHalfStar = typedArray.getBoolean(R.styleable.RatingStarView_rsv_onlyHalfStar, true)
            clickHalfStar =
                typedArray.getBoolean(R.styleable.RatingStarView_rsv_clickHalfStar, clickHalfStar)
            typedArray.recycle()
        }
    }

    private fun setStarBackgroundColor(color: Int) {
        starBackgroundColor = color
        invalidate()
    }

    /**
     * @see StarModel.setThickness
     */
    fun setStarThickness(thicknessFactor: Float) {
        starList?.let {
            for (star in it) {
                star.setThickness(thicknessFactor)
            }
            invalidate()
        }

    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        invalidate()
    }

    /**
     * Finally progress is: progress = rating / starNum
     * @param rating should be [0, starNum]
     */
    fun setRating(rating: Float) {
        if (rating != this.rating) {
            this.rating = rating
            invalidate()
        }
    }

    /**
     * Set the smooth of the star's horn.
     * @param cornerRadius corner circle radius
     */
    fun setCornerRadius(cornerRadius: Float) {
        this.cornerRadius = cornerRadius
        invalidate()
    }

    /**
     * The horizontal margin between two stars. The [.setCornerRadius] would make extra space
     * as it make the star smaller.
     * @param margin horizontal space
     */
    fun setStarMargin(margin: Int) {
        starMargin = margin.toFloat()
        calcStars()
        invalidate()
    }

    /**
     * How many stars to show, one star means one score = 1f. See [.setRating]<br></br>
     * NOTE: The star's height is made by contentHeight by default.So, be sure to has defined the
     * correct StarView's height.
     * @param count star count.
     */
    fun setStarNum(count: Int) {
        if (starNum != count) {
            starNum = count
            calcStars()
            invalidate()
        }
    }

    private fun onPaddingChanged() {
        starList?.let {
            val left = paddingLeft
            val top = paddingTop
            for (star in it) {
                star.moveStarTo(left.toFloat(), top.toFloat())
            }
        }
    }

    fun setDrawStrokeForFullStar(draw: Boolean) {
        drawStrokeForFullStar = draw
    }

    fun setDrawStrokeForEmptyStar(draw: Boolean) {
        drawStrokeForEmptyStar = draw
    }

    /**
     * Create all stars data, according to the contentWidth/contentHeight.
     */
    private fun calcStars() {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        var left = paddingLeft

        // according to the View's height , make star height.
        var starHeight = contentHeight
        if (contentHeight > contentWidth) {
            starHeight = contentWidth
        }
        if (starHeight <= 0) return
        val startWidth = StarModel.getStarWidth(starHeight.toFloat())

        // starCount * startWidth + (starCount - 1) * starMargin = contentWidth
        var starCount = ((contentWidth + starMargin) / (startWidth + starMargin)).toInt()
        if (starCount > starNum) {
            starCount = starNum
        }
        this.starHeight = starHeight.toFloat()
        this.starWidth = startWidth
        Log.d(
            TAG, "drawing starCount = " + starCount + ", contentWidth = " + contentWidth
                    + ", startWidth = " + startWidth + ", starHeight = " + starHeight
        )
        starList = ArrayList<StarModel>(starCount).apply {
            for (i in 0 until starCount) {
                val star = StarModel(starThicknessFactor)
                add(star)
                star.setDrawingOuterRect(left, paddingTop, starHeight)
                left += (startWidth + 0.5f + starMargin).toInt()
            }
        }

        this.starCount = starCount
        this.starWidth = startWidth
        this.starHeight = starHeight.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Float
        var height: Int // must have height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_STAR_HEIGHT;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = height.coerceAtMost(heightSize);
            }
        }
        val starHeight = (height - paddingBottom - paddingTop).toFloat()

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize.toFloat()
        } else {
            // get the perfect width
            width = (paddingLeft + paddingRight).toFloat()
            if (starNum > 0) {
                if (starHeight > 0) {
                    width += starMargin * (starNum - 1)
                    width += StarModel.getStarWidth(starHeight) * starNum
                }
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                width = widthSize.toFloat().coerceAtMost(width)
            }
        }

        Log.d(
            TAG, "[onMeasure] width = " + width + ", pLeft = " + paddingLeft
                    + ", pRight = " + paddingRight + ", starMargin = " + starMargin
                    + ", starHeight = " + starHeight + ", starWidth = " + StarModel.getStarWidth(
                starHeight
            )
        )

        var widthInt = width.toInt()
        if (widthInt < width) {
            widthInt++
        }

        setMeasuredDimension(widthInt, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (starList == null) {
            calcStars()
        }

        starList?.let {
            if (it.isNotEmpty()) {
                for (i in it.indices) {
                    if (rating >= i + 1) {
                        drawFullStar(it[i], canvas)
                    } else {
                        var decimal = rating - i
                        if (decimal > 0) {
                            if (onlyHalfStar) {
                                decimal = 0.5f
                            }
                            drawPartialStar(it[i], canvas, decimal)
                        } else {
                            drawEmptyStar(it[i], canvas)
                        }
                    }
                }
            }
        }
    }

    private fun drawFullStar(star: StarModel, canvas: Canvas) {
        drawSolidStar(star, canvas, starForegroundColor)
        if (drawStrokeForFullStar) {
            drawStarStroke(star, canvas)
        }
    }

    private fun drawEmptyStar(star: StarModel, canvas: Canvas) {
        drawSolidStar(star, canvas, starBackgroundColor)
        if (drawStrokeForEmptyStar) {
            drawStarStroke(star, canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {
            calcStars();
        }
    }

    private fun drawPartialStar(star: StarModel, canvas: Canvas, percent: Float) {
        Log.d(TAG, "drawPartialStar percent = $percent")
        if (percent <= 0) {
            drawEmptyStar(star, canvas)
            return
        } else if (percent >= 1) {
            drawFullStar(star, canvas)
            return
        }

        // layer 1
        drawSolidStar(star, canvas, starBackgroundColor)
        val dividerX = star.getOuterRect().left + star.getOuterRect().width() * percent
        this.dividerX = dividerX

        // layer 2
        val r = star.getOuterRect()
//        canvas.saveLayerAlpha(r.left, r.top, r.right, r.bottom, 0xff, CLIP_SAVE_FLAG)
        canvas.saveLayerAlpha(r.left, r.top, r.right, r.bottom, 0xff)
        val clip = RectF(star.getOuterRect())
        clip.right = dividerX
        canvas.clipRect(clip)
        drawSolidStar(star, canvas, starForegroundColor)
        canvas.restore()

        // layer 1
        if (drawStrokeForHalfStar) {
            drawStarStroke(star, canvas)
        }
    }

    private fun drawSolidStar(star: StarModel, canvas: Canvas, fillColor: Int) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = fillColor
        paint.pathEffect = pathEffect
        var prev = star.getVertex(1)
        val path = Path()
        for (i in 0..4) {
            path.rewind()
            path.moveTo(prev!!.x, prev.y)
            val next = prev.next
            path.lineTo(next!!.x, next.y)
            path.lineTo(next.next!!.x, next.next!!.y)
            path.lineTo(next.next!!.x, next.next!!.y)
            canvas.drawPath(path, paint)
            prev = next.next
        }

        // fill the middle hole. use +1.0 +1.5 because the path-API will leave 1px gap.
        path.rewind()
        prev = star.getVertex(1)
        path.moveTo(prev!!.x - 1f, prev.y - 1f)
        prev = prev.next!!.next
        path.lineTo(prev!!.x + 1.5f, prev.y - 0.5f)
        prev = prev.next!!.next
        path.lineTo(prev!!.x + 1.5f, prev.y + 1f)
        prev = prev.next!!.next
        path.lineTo(prev!!.x, prev.y + 1f)
        prev = prev.next!!.next
        path.lineTo(prev!!.x - 1f, prev.y + 1f)
        paint.pathEffect = null
        canvas.drawPath(path, paint)
    }

    private fun drawStarStroke(star: StarModel, canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        paint.pathEffect = pathEffect
        var prev = star.getVertex(1)
        val path = Path()
        for (i in 0..4) {
            path.rewind()
            path.moveTo(prev!!.x, prev.y)
            val next = prev.next
            path.lineTo(next!!.x, next.y)
            path.lineTo(next.next!!.x, next.next!!.y)
            path.lineTo(next.next!!.x, next.next!!.y)
            canvas.drawPath(path, paint)
            prev = next.next
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            clickedX = event.x
            clickedY = event.y
        }
        return super.onTouchEvent(event)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mOuterOnClickListener = l
    }

    override fun onClick(v: View) {
        mOuterOnClickListener?.onClick(v)
        if (enableSelectRating) {
            changeRatingByClick()
        }
    }

    private fun changeRatingByClick() {
        val paddingTop = paddingTop
        if (clickedY < paddingTop || clickedY > paddingTop + starHeight) {
            return
        }
        val paddingLeft = paddingLeft
        val starWidth = starWidth
        val starMargin = starMargin
        var left = paddingLeft.toFloat()
        for (i in 1..starCount) {
            val right = left + starWidth
            if (clickedX in left..right) {
                if (clickHalfStar) {
                    if (i - rating == 0f) { // 5-4，else 全亮
                        // 全星减半星
                        setRating(i - 0.5f)
                    } else if (abs(i - rating) <= 0.5f) { // 5 4.5
                        // 已经是半星，减成空
                        setRating(rating - 0.5f) // 4，再次点击，5
                    } else {
                        setRating(i.toFloat())
                    }
                } else {
                    if (rating == i.toFloat()) {
                        setRating(i - 1f)
                    } else {
                        setRating(i.toFloat())
                    }
                }
                break
            }
            left += starWidth + starMargin
        }
    }

    fun getRating(): Float {
        return rating
    }
}