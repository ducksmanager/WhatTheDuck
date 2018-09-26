package net.ducksmanager.util

import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout

object DraggableRelativeLayout {
    fun makeDraggable(view: RelativeLayout) {
        view.setOnTouchListener(object : View.OnTouchListener {
            internal var dX: Float = 0.toFloat()
            internal var dY: Float = 0.toFloat()
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }

                    MotionEvent.ACTION_MOVE -> view.animate()
                            .x(event.rawX + dX)
                            .y(event.rawY + dY)
                            .setDuration(0)
                            .start()
                    else -> return false
                }
                return true
            }
        })
    }
}
