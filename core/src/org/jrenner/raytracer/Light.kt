package org.jrenner.raytracer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class Light {

    companion object {
        fun createLights(rt: RayTracer) {
            rt.createLight(100f, -100f, 1000f)
            //rt.createLight(1000f, -1000f, 0f)
            //rt.createLight(-50f, -50f, 0f)
            //rt.createLight(0f, 50f, 0f)
            //rt.createLight(30f, 10f, 0f)
//            rt.createLight(-30f, -50f, 0f)
//            rt.createLight(-50f, -30f, 0f)
        }
    }

    var brightness = 0.8f
    val position = Vector3(-10f, 0f, 0f)
    val color = Color.WHITE

    val attenConst = 0f
    val attenLin = 0.01f
    val attenExp = 0.01f

    fun brightnessAtDistance(dist: Float): Float {
        return brightness / (attenConst + (attenLin * dist) + (attenExp * (dist * dist)))
    }

    fun lightAtDistance(dist: Float): Vector3 {
        val res = Vector3()
        val bright = brightnessAtDistance(dist)
        res.set(color.r, color.g, color.b).scl(bright)
        return res
    }
}
