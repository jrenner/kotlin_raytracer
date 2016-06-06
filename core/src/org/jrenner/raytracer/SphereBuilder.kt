package org.jrenner.raytracer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import java.util.*

object SphereBuilder {

    fun ArrayList<RTSphere>.create(x: Float,
            y: Float,
            z: Float,
            radius: Float,
            color: Color
            ): RTSphere {
        val sp = RTSphere(Vector3(x, y, z), radius)
        sp.emissionColor.set(color.r, color.g, color.b)
        sp.surfColor.set(color.r, color.g, color.b)
        add(sp)
        return sp
    }

    var repositionCounts = 0

    fun addSpheres(spheres: ArrayList<RTSphere>) {
        // green
//        val sp1 = spheres.create(-1f, 1f, -12f, 1.5f, Color(0.2f, 1f, 0.2f, 1f))
//        sp1.name = "green"
//        // purple
//        val sp2 = spheres.create(0f, -1f, -10f, 1f, Color(0.8f, 0.2f, 0.8f, 1f))
//        sp2.name = "purple"
//        // blue
//        //spheres.create(0f, 0f, -18f, 3f, Color(0.2f, 0.2f, 1f, 1f))
//        // red
//        val sp4 = spheres.create(0f, 0f, -8f, 0.5f, Color(1f, 0.2f, 0.2f, 1f))
//        sp4.name = "red"

        fun randomSphere() {
            val rcolor = Color().randomize()
            val baseRadius = 1.5f
            val radius = (MathUtils.random() * 1f) + baseRadius
            //val radius = baseRadius
            val x = rand(-3f, 3f)
            val y = rand(-3f, 3f)
            val z = rand(-10f, -15f)
            val rsp = spheres.create(x, y, z, radius, Color().randomize())

            rsp.reflection = 0.25f

            while (repositionCounts < 1000) {
                var collide = false
                for (other in spheres) {
                    if (other.equals(rsp)) continue
                    val diff = Vector3(rsp.origCenter).sub(other.origCenter)
                    val thresh = rsp.radius + other.radius
                    val dist = diff.len() - thresh
                    collide = dist <= 0
                    if (collide) {
                        repositionCounts += 1
                        //println("${rsp} reposition $rsp -- $other")
                        val delta = Vector3(diff).scl(1f)
                        //println("delta: ${delta.fmt}")
                        rsp.origCenter.add(delta)
                        rsp.center.set(rsp.origCenter)
                        break
                    }
                }
                if (!collide) break
            }
            rsp.reflection = rand(0.1f, 0.3f)
        }

        (1..8).forEach { randomSphere() }

        val c = 0.7f
        val bigSphere = spheres.create(0f, 0f, -100f, radius=80f, color=Color(c, c, c, 1f))
        bigSphere.reflection = 0.5f
        bigSphere.static = true

        println("repositions: $repositionCounts")


        spheres.shuffle()

    }
}
