package org.jrenner.raytracer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import java.util.concurrent.TimeUnit

class View {
    val viewport = FitViewport(screenWidth.toFloat(), screenHeight.toFloat())
    val camera = OrthographicCamera(viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat())
    init {
        viewport.camera = camera
        camera.setToOrtho(true)
    }

    val batch = SpriteBatch()

//    val testTex: Texture
//    init {
//        val px = Pixmap(screenWidth, screenHeight - 30, Pixmap.Format.RGB888)
//        println("${px.width} -- ${px.height}")
//        px.setColor(Color.valueOf("#222255"))
//        (0..px.height - 1).forEach { y ->
//            (0..px.width - 1).forEach { x ->
//                px.drawPixel(x, y)
//            }
//        }
//        testTex = Texture(px)
//    }
    val gl: GL20 = Gdx.graphics.gL20

    val timer = Timer("view")

    fun render(texture: Texture) {
        main.rayTracer.renderLock.lock()
        timer.start()
        gl.glClearColor(0f, 0f, 0f, 1f)
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()

        batch.draw(texture, 0f, 0f)

        batch.end()

        timer.stop()
        main.rayTracer.renderLock.unlock()

        //Thread.sleep(50)
    }
}

val screenWidth: Int get() = Gdx.graphics.width
val screenHeight: Int get() = Gdx.graphics.height