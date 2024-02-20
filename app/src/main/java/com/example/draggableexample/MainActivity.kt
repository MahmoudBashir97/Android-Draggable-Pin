package com.example.draggableexample

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var pinImage: ImageView
    private var xDelta = 0
    private var yDelta = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pinImage = findViewById(R.id.pinImage)

        pinImage.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                if (event != null){
                    val x = event.rawX?.toInt() ?: 0
                    val y = event.rawY?.toInt() ?: 0

                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_DOWN -> {
                            val lParams = view?.layoutParams as (FrameLayout.LayoutParams)
                            xDelta = x - lParams.leftMargin
                            yDelta = y - lParams.topMargin
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val layoutParams = view?.layoutParams as (FrameLayout.LayoutParams)
                            layoutParams.leftMargin = x - xDelta
                            layoutParams.topMargin = y - yDelta
                            layoutParams.rightMargin = -250
                            layoutParams.bottomMargin = -250
                            view.layoutParams = layoutParams
                        }
                    }
                }
                return true
            }
        })
    }
}