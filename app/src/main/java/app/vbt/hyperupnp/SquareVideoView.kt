package app.vbt.hyperupnp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.widget.VideoView


class SquareVideoView : VideoView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    private val fhandler = Handler(Looper.getMainLooper())
    private val delay = 10000L
    private var shouldRepeat = true


    override fun onAttachedToWindow() {
        fhandler.postDelayed(object : Runnable {
            override fun run() {
                Log.d("repeater", "preview seeking")
                var d = duration
                if (d < 0) d = 0
                seekTo((0..d).random())
                if (shouldRepeat) {
                    fhandler.postDelayed(this, delay)
                } else {
                    Log.d("repeater", "stopped preview seeking")
                }
            }
        }, delay)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        shouldRepeat = false
        super.onDetachedFromWindow()
    }
}