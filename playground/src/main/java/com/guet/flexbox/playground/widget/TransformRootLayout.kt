package com.guet.flexbox.playground.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.Shader.TileMode
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.didichuxing.doraemonkit.util.UIUtils

class TransformRootLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val myMatrix = Matrix()
    var cornerRadius = UIUtils.dp2px(context, 15f)
    var animationDuration: Long = 500L
    var offset: Float = UIUtils.dp2px(context, 30f).toFloat()
    private var bitmap: Bitmap? = null
    private var useRadius: Float = 0f
    private val canvas = Canvas()
    private val rectF = RectF()
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val src = FloatArray(8)
    private val dst = FloatArray(8)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return bitmap != null
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    fun move() {
        val bitmap = Glide.get(context).bitmapPool[
                (width / 2f).toInt(), (height / 2f).toInt(),
                Config.RGB_565
        ]
        canvas.setBitmap(bitmap)
        canvas.scale(0.5f, 0.5f)
        draw(canvas)
        canvas.setBitmap(null)
        val shader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)
        paint.shader = shader
        val matrix = Matrix()
        matrix.setRectToRect(
                RectF(
                        0f, 0f,
                        bitmap.width.toFloat(),
                        bitmap.height.toFloat()
                ),
                RectF(0f, 0f,
                        width.toFloat(),
                        height.toFloat()
                ),
                Matrix.ScaleToFit.FILL
        )
        shader.setLocalMatrix(matrix)
        bitmap.prepareToDraw()
        this.bitmap = bitmap
        Choreographer.getInstance().postFrameCallback {
            src[0] = 0f
            src[1] = 0f
            src[2] = width.toFloat()
            src[3] = 0f
            src[4] = width.toFloat()
            src[5] = height.toFloat()
            src[6] = 0f
            src[7] = height.toFloat()
            foreground = ColorDrawable(Color.BLACK).apply {
                alpha = 0
            }
            ValueAnimator.ofFloat(0f, 2f).apply {
                addUpdateListener {
                    var value = it.animatedValue as Float
                    (foreground as ColorDrawable).alpha = ((255f / 2f) * (value / 2f)).toInt()
                    useRadius = (value / 2f) * cornerRadius
                    if (value <= 1) {
                        //lt-x
                        dst[0] = value * offset
                        //lt-y
                        dst[1] = value * offset
                        //rt-x
                        dst[2] = width - value * offset
                        //rt-y
                        dst[3] = value * offset
                        //rb-x
                        dst[4] = width.toFloat()
                        //rb-y
                        dst[5] = height.toFloat()
                        //lb-x
                        dst[6] = 0f
                        //lb-y
                        dst[7] = height.toFloat()
                    } else {
                        value -= 1
                        //lt-x
                        dst[0] = offset
                        //lt-y
                        dst[1] = offset
                        //rt-x
                        dst[2] = width - offset
                        //rt-y
                        dst[3] = offset
                        //rb-x
                        dst[4] = width - value * offset
                        //rb-y
                        dst[5] = height - value * offset
                        //lb-x
                        dst[6] = value * offset
                        //lb-y
                        dst[7] = height - value * offset
                    }
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        //lt-x
                        dst[0] = offset
                        //lt-y
                        dst[1] = offset
                        //rt-x
                        dst[2] = width - offset
                        //rt-y
                        dst[3] = offset
                        //rb-x
                        dst[4] = width - offset
                        //rb-y
                        dst[5] = height - offset
                        //lb-x
                        dst[6] = offset
                        //lb-y
                        dst[7] = height - offset
                        useRadius = cornerRadius.toFloat()
                        (foreground as ColorDrawable).alpha = 255 / 2
                        invalidate()
                    }
                })
                interpolator = DecelerateInterpolator()
                duration = animationDuration
            }.start()
        }
    }

    fun reset() {
        ValueAnimator.ofFloat(0f, 2f).apply {
            addUpdateListener {
                var value = it.animatedValue as Float
                (foreground as ColorDrawable).alpha = ((255f / 2f) - (255f / 2f) * (value / 2f)).toInt()
                useRadius = cornerRadius - (value / 2f) * cornerRadius
                if (value <= 1) {
                    dst[0] = offset
                    dst[1] = offset
                    dst[2] = width - offset
                    dst[3] = offset
                    dst[4] = width - (1f - value) * offset
                    dst[5] = height - (1f - value) * offset
                    dst[6] = (1f - value) * offset
                    dst[7] = height - (1f - value) * offset
                } else {
                    value -= 1
                    dst[0] = (1f - value) * offset
                    dst[1] = (1f - value) * offset
                    dst[2] = width - (1f - value) * offset
                    dst[3] = (1f - value) * offset
                    dst[4] = width.toFloat()
                    dst[5] = height.toFloat()
                    dst[6] = 0f
                    dst[7] = height.toFloat()
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    foreground = null
                    useRadius = 0f
                    bitmap?.let { Glide.get(context).bitmapPool.put(it) }
                    paint.shader = null
                    bitmap = null
                }
            })
            interpolator = DecelerateInterpolator()
            duration = animationDuration
        }.start()

    }

    override fun dispatchDraw(canvas: Canvas) {
        val picture = this.bitmap
        if (picture != null) {
            canvas.save()
            canvas.drawColor(Color.BLACK)
            myMatrix.reset()
            myMatrix.setPolyToPoly(
                    src,
                    0,
                    dst,
                    0,
                    src.size shr 1
            )
            canvas.concat(myMatrix)
            canvas.drawRoundRect(
                    rectF.apply {
                        set(0f, 0f, width.toFloat(), height.toFloat())
                    },
                    useRadius,
                    useRadius,
                    paint
            )
            canvas.restore()
        } else {
            super.dispatchDraw(canvas)
        }
    }
}