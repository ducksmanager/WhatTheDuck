package net.ducksmanager.util

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

object DraggableRelativeLayout {
    @SuppressLint("ClickableViewAccessibility")
    fun makeDraggable(view: View) {
        view.setOnTouchListener(object : OnTouchListener {
            var dX = 0f
            var dY = 0f
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