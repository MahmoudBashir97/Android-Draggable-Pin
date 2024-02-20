package com.example.draggableexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.otaliastudios.zoom.ZoomLayout
import io.github.hyuwah.draggableviewlib.DraggableImageView
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.DraggableView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class ZoomableActivity : AppCompatActivity() {

    private lateinit var draggablePin: DraggableImageView
    private lateinit var containerVidRelativeLayout: RelativeLayout
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var floorImageView: AppCompatImageView
    private val coOrdinatesFlow = MutableSharedFlow<Pair<Float, Float>>()

    private lateinit var mCanvas:Canvas
    private lateinit var bm:Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zoomable_activity)
        draggablePin = findViewById(R.id.draggablePin)
        containerVidRelativeLayout = findViewById(R.id.containerVidRelativeLayout)
        zoomLayout = findViewById(R.id.zoomLayout)
        floorImageView = findViewById(R.id.floorImageView)
        setEnableZooming(true)

        bm = BitmapFactory.decodeResource(resources, R.drawable.ic_floor)
        val workBitmap = bm.copy(Bitmap.Config.ARGB_8888, true)
        floorImageView.setImageBitmap(workBitmap)
        mCanvas = Canvas(workBitmap)


        //drawCustomLine()

        draggablePin.setListener(object : DraggableListener {
            override fun onPositionChanged(view: View) {
                setEnableZooming(true)
                lifecycleScope.launch {
                    coOrdinatesFlow.emit(Pair(view.x, view.y))
                }
            }

            override fun onLongPress(view: View) {

            }
        })

        lifecycleScope.launch {
            coOrdinatesFlow.debounce(500L)
                .collectLatest {
                    val x = it.first
                    val y = it.second



                   // drawLine(mCanvas,x+40,y+110)

                    addPoint(
                        View.generateViewId(),
                        containerVidRelativeLayout,
                        x.toInt(),
                        y.toInt(),
                        this@ZoomableActivity
                    )
                }
        }
    }

    private fun setEnableZooming(isEnabled: Boolean) {
        zoomLayout.setZoomEnabled(isEnabled)
        zoomLayout.setScrollEnabled(isEnabled)
    }

    @SuppressLint("InflateParams")
    private fun addPoint(
        pointId: Int, view: View, x: Int, y: Int, context: Context
    ) {
        val imageLayoutParams = LinearLayout.LayoutParams(
            24.px, 24.px
        )

        val pointImageView = ImageView(context)
        pointImageView.id = pointId

        setEnableZooming(true)
        draggablePin.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val pinWidth = draggablePin.width
                val pinHeight = draggablePin.height
                draggablePin.visibility = View.GONE

                Log.d("?", "CoordinatesV : point x: $x , point y: $y  , $pinWidth , $pinHeight")

                draggablePin.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })


        imageLayoutParams.setMargins(x + 40, y + 110, 0, 0) // work around to put the point
        pointImageView.layoutParams = imageLayoutParams

        pointImageView.setImageResource(R.drawable.ic_add_point_green)
        (view as ViewGroup).addView(pointImageView)
    }

    private fun drawLine(canvas: Canvas,endX:Float,endY:Float){
        val mPaint = Paint()
        mPaint.color = Color.GREEN
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.strokeWidth = 5F
        mPaint.isAntiAlias = true


        val startX = 100F
        val startY = 20F

        canvas.drawLine(startX,startY,endX,endY,mPaint)
        //floorImageView.setImageBitmap(bm)
    }

    private fun drawCustomLine(){
        val lineView = findViewById<View>(R.id.lineView)

        // Get the two points between which you want to draw the line
        val startPointX = 0
        val startPointY = 0
        val endPointX = 500
        val endPointY = 500

        // Wait for the line view to be laid out and get its width and height
        lineView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                lineView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Calculate the distance between the two points
                val distance = sqrt((endPointX - startPointX).toDouble().pow(2.0) + (endPointY - startPointY).toDouble()
                    .pow(2.0)
                )

                // Calculate the angle between the two points
                val angle = Math.toDegrees(atan2((endPointY - startPointY).toDouble(), (endPointX - startPointX).toDouble()))

                // Update the line view's width, height, and rotation
                val layoutParams = lineView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.width = distance.toInt()
                layoutParams.height = 5
                layoutParams.leftMargin = startPointX
                layoutParams.topMargin = startPointY
                lineView.rotation = angle.toFloat()

                // Apply the updated layout parameters to the line view
                lineView.layoutParams = layoutParams
            }
        })
    }
}

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
