package org.jrenner.raytracer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.IntSet
import com.badlogic.gdx.utils.ObjectSet
import java.util.*

class InputManager : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        pressed.add(keycode)
        when (keycode) {
            ESCAPE -> { println("exit"); Gdx.app.exit() }
            M -> {
                main.rayTracer.spheres.forEach {
                    val shift = Vector3().randomize()
//                    shift.z = 0f
                    it.center.add(shift)
                }
            }
            P -> {
                main.togglePause()
            }

        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        pressed.remove(keycode)
        return false
    }

    val pressed = HashSet<Int>()

    val cameraMovement = Vector3()
    val moveSpeed = 1f

    fun processPressedKeys() {
        cameraMovement.setZero()
        for (k in pressed) {
            when (k) {
                W -> cameraMovement.z += moveSpeed * -1f
                S -> cameraMovement.z += moveSpeed
                A -> cameraMovement.x += moveSpeed * -1f
                D -> cameraMovement.x += moveSpeed
                Q -> cameraMovement.y += moveSpeed * -1f
                E -> cameraMovement.y += moveSpeed
            }
        }
    }

    fun update() {
        processPressedKeys()
        if (!cameraMovement.isZero) {
            //main.view.camera.position.add(cameraMovement)
            EYE_POSITION.add(cameraMovement)
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        mx = screenX
        my = screenY
        main.runRayTracer(debug=true)
        return false
    }
}
