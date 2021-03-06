package com.anwesh.uiprojects.squareballstepview

/**
 * Created by anweshmishra on 31/10/18.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

val nodes : Int = 5

val sides : Int = 4

val balls : Int = 3

val delay : Long = 12

val scaleSpeed : Float = 0.05f

fun Float.divideScale(j : Int, n : Int) : Float = Math.min((1f/n), Math.max(0f, this - (1f/ n) * j)) * n

fun Canvas.drawSBSNode(i : Int, scale : Float, paint : Paint) {
    paint.color = Color.parseColor("#01579B")
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val deg : Float = 360f / sides
    val size : Float = gap / 3
    val xGap : Float = (2 * size) / (balls)
    val r : Float = xGap / 2
    paint.strokeWidth = Math.min(w, h) / 120
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w/2, gap + i * gap)
    for (j in 0..(sides - 1)) {
        val sc : Float = scale.divideScale(j, sides)
        save()
        rotate(deg * j)
        translate(-size,  -size)
        for (k in 0..(balls - 1)) {
            val scb : Float = sc.divideScale(k, balls)
            save()
            translate(xGap + xGap * k, 0f)
            paint.style = Paint.Style.STROKE
            drawCircle(0f, 0f, r, paint)
            paint.style = Paint.Style.FILL
            drawCircle(0f, 0f, r * scb, paint)
            restore()
        }
        restore()
    }
    restore()
}

fun Float.updateScale(dir : Float, n : Int) : Float = this + (scaleSpeed / n) * dir

class SquareBallStepsView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale = scale.updateScale(dir, balls * sides)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SBSNode(var i : Int, val state : State = State()) {

        private var prev : SBSNode? = null

        private var next : SBSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SBSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSBSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SBSNode {
            var curr : SBSNode? = next
            if (dir == -1) {
                curr = prev
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SquareBallStep(var i : Int) {
        private val root : SBSNode = SBSNode(0)

        private var curr : SBSNode = root

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquareBallStepsView) {

        private val sbs : SquareBallStep = SquareBallStep(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            sbs.draw(canvas, paint)
            animator.animate {
                sbs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sbs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : SquareBallStepsView {
            val view : SquareBallStepsView = SquareBallStepsView(activity)
            activity.setContentView(view)
            return view
        }
    }
}