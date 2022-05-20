package com.jiangyt.library.rating

import android.graphics.RectF


/**
 * @Title: NTCloud
 * @Package com.niu.view.rating
 * @Description: * Holds all vertexes (x,y) and bounds (outerRect) info about a drawing Star.
 * used by {@link RatingStarView}, Most calculations (about x,y、size etc.) is done here.
 *
 * <h1>[Based Idea or Concept] </h1>
 *
 * ## Standard Coordinate:<br />
 *  The coordinate is —— toward right for x+ ,toward up for y+ .
 * <br /><br />
 * ## 5 outer vertexes<br />
 * The outer circle's (means "circumcircle") radius is 1f, original point O is the star's center,
 * so, the 5 vertexes at 5 outer corner is (from top A, at clockwise order):
 *
 *  <li>A（0,1）</li>
 *  <li>B(cos18°,sin18°)</li>
 *  <li>C(cos54°,-sin54°)</li>
 *  <li>D(-cos54°,-sin54°)</li>
 *  <li>E(-cos18°,sin18°)</li>
 * </p>
 * @author jiangyt
 * @date 2022/5/19 7:00 下午
 * @version V1.0
 */
class StarModel(thicknessFactor: Float = DEFAULT_THICKNESS) {

    companion object {
        private const val TAG = "StarModel"
        const val DEFAULT_THICKNESS = 0.5f
        const val MIN_THICKNESS = 0.3f
        const val MAX_THICKNESS = 0.9f
        const val DEFAULT_SCALE_FACTOR = 0.9511f

        // region vertexes fields
        /**
         * 10 float values for star's 5 vertex's (x,y) —— outer circle's radius is 1f (
         * NOTE: In the "Standard Coordinate".) , first vertex is for top corner, in clockwise order.
         */
        private val starVertexes = floatArrayOf(
            -0.9511f, 0.3090f,  // E (left)
            0.0000f, 1.0000f,  // A (top vertex)
            0.9511f, 0.3090f,  // B (right)
            0.5878f, -0.8090f,  // C (bottom right)
            -0.5878f, -0.8090f
        )

        /**
         * ratio = height / width.
         * width is think as 1f, because the star's width is lager.
         * NOTE: In the　"Standard Coordinate"
         */
        private val aspectRatio =
            (starVertexes[3] - starVertexes[7]) / (starVertexes[4] - starVertexes[0])


        /**
         * ratio = height / width. width is think as 1f, because the star's width is lager.
         * NOTE: In the　"Standard Coordinate"
         *
         * @return ratio = height / width.
         */
        fun getOuterRectAspectRatio(): Float {
            return aspectRatio
        }

        fun getStarWidth(starHeight: Float): Float {
            return starHeight / getOuterRectAspectRatio()
        }
    }

    private var currentScaleFactor = DEFAULT_SCALE_FACTOR
    private val outerRect = RectF()
    private var currentThicknessFactor = DEFAULT_THICKNESS

    /**
     * firstVertex is vertex: E (very left one)
     *
     * @see StarModel
     */
    private val firstVertex: VertexF = VertexF()

    /**
     * All star vertexes, from the most left one. then clockwise.
     *
     * NOTE: init or update by [.initAllVertexesToStandard]
     *
     * @see .firstVertex
     *
     * @see .starVertexes
     */
    private val vertexes: Array<VertexF?> = arrayOfNulls(10)

    init {
        // 初始化
        vertexes.let {
            it[0] = firstVertex
            for (i in 1..9) {
                it[i] = VertexF()
                it[i - 1]!!.next = it[i]
            }
            // link tail and head
            it[9]!!.next = it[0]
        }
        reset(thicknessFactor)
    }

    /**
     * Reset all vertexes values to based on radius-1f, will call adjustCoordinate() automatically,
     * So after reset() the Coordinate is match with Android.
     *
     * @param thickness [.setThicknessOnStandardCoordinate]
     */
    private fun reset(thickness: Float) {
        currentScaleFactor = DEFAULT_SCALE_FACTOR
        initAllVertexesToStandard()
        updateOuterRect()
        setThicknessOnStandardCoordinate(thickness)
        adjustCoordinate()
    }

    fun setDrawingOuterRect(left: Int, top: Int, height: Int) {
        // ScaleFactor=1f means width is 1f
        val resizeFactor = height / aspectRatio
        offsetStar(-outerRect.left, -outerRect.top)
        changeScaleFactor(resizeFactor)
        offsetStar(left.toFloat(), top.toFloat())
        updateOuterRect()
    }

    fun moveStarTo(left: Float, top: Float) {
        val offsetX = left - outerRect.left
        val offsetY = left - outerRect.top
        offsetStar(offsetX, offsetY)
        updateOuterRect()
    }

    // endregion
    private fun initAllVertexesToStandard() {
        firstVertex.apply {
            x = starVertexes[0]
            y = starVertexes[1]
        }

        // update all 5 outer vertexes.
        var current = firstVertex
        for (i in 0..4) {
            current.x = starVertexes[i * 2]
            current.y = starVertexes[i * 2 + 1]
            current = current.next!!.next!!
        }

        // update all 5 inner vertexes.
        var prevOuter = firstVertex
        for (i in 0..4) {
            val innerV = prevOuter.next!!
            innerV.x = (prevOuter.x + innerV.next!!.x) / 2f
            innerV.y = (prevOuter.y + innerV.next!!.y) / 2f
            prevOuter = innerV.next!!
        }
    }

    /**
     * Get vertex at index in [.vertexes]
     * @param index see [.vertexes]
     */
    fun getVertex(index: Int): VertexF? {
        return vertexes[index]
    }

    fun getOuterRect(): RectF {
        return RectF(outerRect)
    }

    /**
     * Keep the star's outer bounds exactly.
     * NOTE: call this after any vertex value changed.
     */
    private fun updateOuterRect() {
        vertexes.let {
            outerRect.top = it[2]!!.y
            outerRect.right = it[4]!!.x
            outerRect.bottom = it[8]!!.y
            outerRect.left = it[0]!!.x
        }
    }

    private fun offsetStar(left: Float, top: Float) {
        vertexes.let {
            for (i in it.indices) {
                it[i]!!.x += left
                it[i]!!.y += top
            }
        }
    }

    private fun changeScaleFactor(newFactor: Float) {
        vertexes.let {
            val scale = newFactor / currentScaleFactor
            if (scale == 1f) return
            for (i in it.indices) {
                it[i]!!.x *= scale
                it[i]!!.y *= scale
            }
            currentScaleFactor = newFactor
        }
    }

    /**
     * change the thickness of star.
     * value [.DEFAULT_THICKNESS]is about to make a standard star.
     *
     * @param factor between [.MIN_THICKNESS] and [.MAX_THICKNESS].
     */
    fun setThickness(factor: Float) {
        if (currentThicknessFactor == factor) return
        val oldScale = currentScaleFactor
        val left = outerRect.left
        val top = outerRect.top
        reset(factor)
        changeScaleFactor(oldScale)
        moveStarTo(left, top)
    }

    private fun setThicknessOnStandardCoordinate(thicknessF: Float) {
        vertexes.let {
            var thicknessFactor = thicknessF
            if (thicknessFactor < MIN_THICKNESS) {
                thicknessFactor = MIN_THICKNESS
            } else if (thicknessFactor > MAX_THICKNESS) {
                thicknessFactor = MAX_THICKNESS
            }
            var i = 1
            while (i < it.size) {
                it[i]!!.x *= thicknessFactor
                it[i]!!.y *= thicknessFactor
                i += 2
            }
            currentThicknessFactor = thicknessFactor
        }
    }

    /**
     * reverse Y, and move to y=0
     */
    private fun adjustCoordinate() {
        vertexes.let {
            val offsetX = -outerRect.left
            val offsetY = outerRect.top
            for (i in it.indices) {
                it[i]!!.y = -it[i]!!.y + offsetY
                it[i]!!.x += offsetX

                // standard value is in radius = 1f, so..
                it[i]!!.x /= 2f
                it[i]!!.y /= 2f
            }
            updateOuterRect()
        }
    }

}