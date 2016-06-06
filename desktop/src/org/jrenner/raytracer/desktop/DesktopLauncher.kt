package org.jrenner.raytracer.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import org.jrenner.raytracer.Main

object DesktopConfig : LwjglApplicationConfiguration() {

    enum class ScreenSize {
        SMALL, MEDIUM, LARGE, FULL
    }
    init {
        val size: ScreenSize = ScreenSize.MEDIUM
        r = 8
        g = 8
        b = 8
        a = 8
        samples = 0
        when (size) {
            ScreenSize.SMALL -> {
                width = 400
                height = 300
            }
            ScreenSize.MEDIUM -> {
                width = 640
                height = 480
            }
            ScreenSize.LARGE -> {
                width = 1280
                height = 800
            }
            else -> {
                width = 400
                height = 300
            }
        }

        foregroundFPS = 300
        backgroundFPS = 300
        vSyncEnabled = false
        fullscreen = false
    }
}

fun main(args: Array<String>) {
    val app = LwjglApplication(Main(), DesktopConfig)
}
