package org.jrenner.raytracer

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.FPSLogger
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import kotlin.properties.Delegates

var main: Main by Delegates.notNull()
var mx = 0
var my = 0

class Main : ApplicationAdapter() {
    val fpsLogger = FPSLogger()
    var view: View by Delegates.notNull()
    val input by lazy { InputManager() }
    val rayTracer by lazy { RayTracer() }

    override fun create() {
        // constant seed useful when debugging
        //MathUtils.random.setSeed(139040L)
        view = View()
        Gdx.input.inputProcessor = input
    }

    private var pause = false
    fun togglePause() {
        pause = !pause
    }

    var reportedOnce = false
    fun runRayTracer(debug: Boolean = false) {
        rayTracer.render(debug)
        if (!reportedOnce) {
            println("traces: ${rayTracer.traceCt}")
            println("hits: ${rayTracer.hitCt}")
            reportedOnce = true
        }
    }

    var frame = 0L

    init {
        main = this
    }

    override fun render() {
        fpsLogger.log()
        if (!pause) {
            rayTracer.spheres.forEach { it.update() }
            runRayTracer()
            frame++
            input.update()
            view.render(rayTracer.texture)
        }
    }
}