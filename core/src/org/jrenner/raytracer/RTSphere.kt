package org.jrenner.raytracer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Sphere
import com.badlogic.gdx.utils.GdxRuntimeException

class RTSphere(center: Vector3, radius: Float) : Sphere(center, radius) {
    companion object {
        private var nextFreeID = 0

        fun getNewID(): Int {
            val res = nextFreeID
            nextFreeID++
            return res
        }
    }

    val id = getNewID()
    var name = "no name"
    var static = false

    override fun equals(other: Any?): Boolean {
        when (other) {
            is RTSphere -> {
                return other.id == this.id
            }
            else -> {
                throw GdxRuntimeException("cannot compare equality between sphere and non-sphere object")
            }
        }
        return false
    }

    override fun toString(): String {
        return "$id - ${center.fmt}"
    }

    val origCenter = Vector3(center)
    val raidusSqr: Float get() = radius * radius
    val surfColor = Vector3(1f, 1f, 1f)
    val emissionColor = Vector3(0.1f, 0.1f, 0.25f)
    var reflection = 0.2f
    var transpency = 0.1f

    val velocity = Vector3()
    val direction = Vector3(rand(1f), rand(1f), rand(1f))
    val accel = 0.005f

    val drive = Vector3().randomize()

    val tmp = Vector3()
    val axes: Array<Vector3> = Array(3, {Vector3()})
    init {
        axes[0].set(Vector3.X)
        axes[1].set(Vector3.Y)
        axes[2].set(Vector3.Z)
    }

    fun applyGravity() {
        val towardCenter = tmp.set(origCenter).sub(center).scl(0.001f)
        //val towardCenter = tmp.set(0f, 0f, -50f).nor().scl(0.01f)
        velocity.add(towardCenter)
    }


    fun randomMovement() {
        if (main.frame % 5L == 0L) drive.rotate(axes.random(), 10f)
        //drive.z = 0f
        tmp.set(drive).scl(accel)
        velocity.add(tmp)
    }

    fun velocityDecay() {
        velocity.scl(0.98f)
    }

    fun move() {
        applyGravity()
        randomMovement()
        center.add(velocity)
        velocityDecay()
        //center.set(origCenter).add(0f, 0f, zOff)
    }

    fun collide() {
        main.rayTracer.spheres.forEach { other ->
            val thresh = other.radius + radius
            val dist = other.center.dst(center)
            if (dist <= thresh) {
                if (other != this) {
                    //println("collide $dist, $thresh")
                    tmp.set(center).sub(other.center).nor().scl(thresh + 0.0001f)
                    center.set(other.center).add(tmp)
                }
            }
        }
    }

    fun update() {
        if (!static) {
            move()
            collide()
        }
    }

    fun distanceFromPointToSurfaceSqr(point: Vector3): Float {
        return point.dst2(center) - radius
    }
}