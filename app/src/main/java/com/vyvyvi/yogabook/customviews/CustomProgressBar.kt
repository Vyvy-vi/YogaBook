package com.vyvyvi.yogabook.customviews

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.animation.doOnEnd
import com.vyvyvi.yogabook.utils.InternalStorageHelper
import kotlin.math.min


class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val bitmapFile: String,
    val duration: Long = 10000
) : View(context, attr, defStyleAttr) {
    private var progressRect = RectF()
    private var progress = 0f
    private var isAnimating = false
    private var animator: ValueAnimator? = null
    private var timeText = getTimeString(duration)

    init {
        startProgressAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)



        val cx = width / 2f
        val cy = cx


        val radius = min(width, height) / 2f - 20
        canvas.drawText(timeText, cx, cy + radius + 140, textPaint)
        val clipPath = Path()
        clipPath.addCircle(cx, cy, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        val bitmap = InternalStorageHelper.loadImageFromInternalStorage(context, bitmapFile)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, width, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        if (isAnimating) {
            progressRect.set(
                cx - radius + progresBarPaint.strokeWidth / 2,
                cy - radius + progresBarPaint.strokeWidth / 2,
                cx + radius - progresBarPaint.strokeWidth / 2,
                cy + radius - progresBarPaint.strokeWidth / 2
            )

            val sweepAngle = (progress / 100f)  * 360
            canvas.drawArc(progressRect, -90f, sweepAngle, false, progresBarPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isAnimating) {
                startProgressAnimation()
            } else {
                stopProgressAnimation()
            }
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun stopProgressAnimation() {
        isAnimating = false
        animator?.pause()
    }
    private fun startProgressAnimation() {
        isAnimating = true

        if (animator == null || progress == 100f) {
            animator = ValueAnimator.ofFloat(0f, 100f)
            animator?.duration = duration

            animator?.addUpdateListener {
                progress = it.animatedValue as Float
                timeText = getTimeString(duration - animator?.currentPlayTime!!)
                invalidate()
            }

            animator?.doOnEnd {
                isAnimating = false
            }
            animator?.start()
        } else {
            animator?.resume()
        }
    }

    private val progresBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 140f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    fun getTimeString(duration: Long): String {
        var secondsTotal = duration / 1000;
        var minutes = secondsTotal / 60;
        var seconds = secondsTotal % 60;

        return String.format("%02d:%02d", minutes, seconds)
    }
}