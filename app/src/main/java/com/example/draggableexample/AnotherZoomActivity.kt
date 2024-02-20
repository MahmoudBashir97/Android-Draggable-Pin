package com.example.draggableexample

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class AnotherZoomActivity:AppCompatActivity(){

    private lateinit var pinImageView:ImageView

    private var xDelta: Float = 0f
    private var yDelta: Float = 0f

    private lateinit var containerVidRelativeLayout : FrameLayout
    private val coOrdinatesFlow = MutableSharedFlow<Pair<Float,Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.another_activity)

        pinImageView = findViewById(R.id.pinImageView)
        containerVidRelativeLayout = findViewById(R.id.containerVidRelativeLayout)

        doZoomableView()
        initObserver()
    }

    private fun doZoomableView(){
        pinImageView.setOnTouchListener { view, event ->
            val x = event.rawX
            val y = event.rawY

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val lParams = view.layoutParams as FrameLayout.LayoutParams
                    xDelta = x - lParams.leftMargin
                    yDelta = y - lParams.topMargin

                    lifecycleScope.launch {
                        coOrdinatesFlow.emit(Pair(xDelta,yDelta))
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = (x - xDelta).toInt()
                    layoutParams.topMargin = (y - yDelta).toInt()
                    view.layoutParams = layoutParams
                }
            }
            true
        }
    }

    private fun initObserver(){
        lifecycleScope.launch {
            coOrdinatesFlow.debounce(1000L)
                .collectLatest {
                    val x = it.first
                    val y = it.second
                    Log.d("???","coordinatesXY : $x , $y")
                    addPoint(
                        View.generateViewId(),
                        containerVidRelativeLayout,
                        x.toInt(),
                        y.toInt(),
                        this@AnotherZoomActivity
                    )
                }
        }
    }

    @SuppressLint("InflateParams")
    private fun addPoint(
        pointId: Int, view: View, x: Int, y: Int, context: Context
    ) {
        val imageLayoutParams = LinearLayout.LayoutParams(
            24.px, 24.px
        )
//        val lp2 = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
//        )
        val pointImageView = ImageView(context)
        pointImageView.id = pointId

        imageLayoutParams.setMargins(x, y , 0, 0) // work around to put the point
        //lp2.setMargins(x , y , 0, 0)// work around to put the bubble
        pointImageView.layoutParams = imageLayoutParams

        pointImageView.setImageResource(R.drawable.ic_add_point_green)
        (view as ViewGroup).addView(pointImageView)
    }
}