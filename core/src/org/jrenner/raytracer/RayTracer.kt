package org.jrenner.raytracer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


const val MAX_RAY_DEPTH = 10
const val AMBIENT_LIGHT = 0.05f
val EYE_POSITION = Vector3(0f, 0f, 0f)


class RayTracer {
    private val bg = 0.15f
    val BACKGROUND = Vector3(bg, bg, bg)
    val width = screenWidth
    val height = screenHeight

    val pixmap by lazy { Pixmap(width, height, Pixmap.Format.RGB888) }
    val texture: Texture by lazy {
        val tex = Texture(pixmap)
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        tex
    }

    val spheres = ArrayList<RTSphere>()
    init { SphereBuilder.addSpheres(spheres) }

    val lights = ArrayList<Light>()

    fun createLight(x: Float, y: Float, z: Float): Light {
        val lt = Light()
        lt.position.set(x, y, z)
        lights.add(lt)
        return lt
    }
    init {
        Light.createLights(this)
    }

    // debug statistics
    var traceCt = 0 // number of ray traces
    var hitCt = 0 // number of ray trace hits (spheres)

    // convenience wrapper
    fun raySphereIntersection(ray: Ray, sphere: RTSphere, hitPos: Vector3?): Boolean {
        return Intersector.intersectRaySphere(ray, sphere.center, sphere.radius, hitPos)
    }

    fun trace(ray: Ray, depth: Int, debugTrace: Boolean = false, debugColor: Boolean = false): Vector3 {
        if (debugTrace) println("\n\n==========================")
        val result = Vector3()
        traceCt += 1

        val hitNorm = Vector3()
        var minDist = Float.MAX_VALUE
        var hitSphere: RTSphere? = null
        val hitPos = Vector3()

        var inShadow = false

        for (collideSphere in spheres) {
            val collidePos = Vector3()
            val hit = raySphereIntersection(ray, collideSphere, collidePos)
            if (hit) {
                val dist = collidePos.dst(ray.origin)
                if (dist < minDist) {
                    if (hitSphere != null && debugTrace) {
                        println("${hitSphere.name} = id:${hitSphere.id}: ${minDist.fmt}, replaced by ${collideSphere.name} -- ${dist.fmt}")
                    }
                    minDist = dist
                    hitSphere = collideSphere
                    hitPos.set(collidePos)
                }
            }
        }

        if (debugTrace) {
            println("hit: ${hitSphere?.name ?: "no hit"} id: ${hitSphere?.id ?: "--"}")
            if (hitSphere != null) {
                println("hitpos: ${hitPos.fmt} -- hitSphereCenter: ${hitSphere.center.fmt}")
            }
        }

        if (hitSphere != null) {

            hitNorm.set(hitPos).sub(hitSphere!!.center).nor()
            // TODO fix inside testing -- but it seems unnecessary??
//            var inside = false
//            if (Gdx.input.isKeyPressed(Input.Keys.R)) {
//                //hitNorm.scl(-1f)
//                if (Vector3(ray.direction).nor().dot(hitNorm) > 0f) {
//                    inside = true
//                    println("inside")
//                    hitNorm.scl(-1f)
//                }
//            }
            var lightNum = -1
            val shadowHitPos = Vector3()
            for (light in lights) {
                lightNum++
                val shadowOrigin = Vector3(hitPos)
                val shadowDirection = Vector3(light.position).sub(hitPos)
                val shadowRay = Ray(shadowOrigin, shadowDirection)
                //println("shadowray: $shadowRay")
                for (shadowSphere in spheres) {
                    if (shadowSphere.equals(hitSphere)) {
                        continue
                    }
                    var hit = raySphereIntersection(shadowRay, shadowSphere, shadowHitPos)
                    if (hit) {
                        if (debugTrace) {
                            println("SHADOW--------------")
                            println("hit: ${hitSphere!!.center.fmt}")
                            println("light $lightNum, shadow: ${shadowSphere.name}, ${shadowSphere.center.fmt}")
                            println("shadow ray: ${shadowRay.fmt}")
                            println("--------------------")
                        }
                        inShadow = true
                        break
                    }
                }

                var shade: Float = 0f
                if (!inShadow) {
                    val lightVec = Vector3()
                    lightVec.set(light.position).sub(hitPos).nor()
                    shade = lightVec.dot(hitNorm)
                    if (shade < 0) {
                        if (debugTrace) {
                            println("hitNorm: ${hitNorm.fmt}, lightVec: ${lightVec.fmt}")
                        }
                        shade = 0f
                    }
                    //println(shade)
                    if (depth < MAX_RAY_DEPTH && hitSphere!!.reflection > 0f) {
                        val reflectDir = getReflectedRayDirection(hitNorm, ray.direction)
                        val reflectRay = Ray(hitPos, reflectDir)
                        val reflection = trace(reflectRay, depth + 1).scl(hitSphere!!.reflection)
                        result.add(reflection)
                    }
                } else {  // IN SHADOW
                    shade = 0.2f
                }
                shade = Math.max(AMBIENT_LIGHT, shade)
                val lightColor = Vector3(hitSphere!!.surfColor).scl(shade * light.brightness)
                result.add(lightColor)
                if (debugTrace) {
                    println("light: $lightNum, shade: $shade, inShadow: $inShadow "
                     + " lightColor: ${lightColor.fmt} result: ${result.fmt}")
                }
            }


        } else {
            // not hit
            if (debugTrace) println("NO HIT")
            result.set(BACKGROUND)
        }
        if (debugColor) {
            result.set(0f, 1f, 0f)
        }

        // TODO is clamp really the right thing to do here?
        return result.clamp()
    }

    val renderLock = ReentrantLock()
    val timer = Timer("rayTracer")
    val traceResults: Array<Array<Vector3>> = Array(height, { Array(width, { Vector3() }) })
    val threadPool: ThreadPoolExecutor
    init {
        val corePoolSize = 8
        val maxPoolSize = 32
        val keepAliveTime = 1000L
        val queue = ArrayBlockingQueue<Runnable>(10000)
        threadPool = ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queue)
    }


    /**
     * Rendering is done in parallel using a ThreadPoolWorker
     * the work is divided up by horizontal lines
     * We use a countdown latch to determine when all tasks are finished, and we lock until that time
     * this ensures that we don't try to render a frame in View while we are still tracing rays
     */
    fun render(debug: Boolean = false) {
        //depthMap.clear()
        renderLock.lock()
        timer.start()

        traceCt = 0
        hitCt = 0
        val invWidth = 1f / width.toFloat()
        val invHeight = 1f / height.toFloat()
        val fov = 40f
        val aspectRatio: Float = width / height.toFloat()
        val angle = Math.tan(Math.PI * 0.5 * fov / 180.0)
        val countDown = CountDownLatch(height)
        // trace rays
        (0..height-1).forEach { y ->
            threadPool.execute {
                (0..width - 1).forEach { x ->
                    val xx = (2 * ((x + 0.5) * invWidth) - 1) * angle * aspectRatio
                    val yy = (1 - 2 * ((y + 0.5) * invHeight)) * angle;
                    val rayDir = Vector3(xx.toFloat(), yy.toFloat(), -1f).nor()
                    val ray = Ray()
                    ray.origin.set(EYE_POSITION)
                    ray.direction.set(rayDir)
                    //println(rayDir)
                    val debugTrace = x == mx && y == screenHeight - my && debug
                    val debugColor = x == mx && y == screenHeight - my
                    val pixel = trace(ray, 0, debugTrace = debugTrace, debugColor = debugColor)
                    traceResults[y][x].set(pixel)

                    //println("pixel: [$x, $y] -- ${pixel.x}, ${pixel.y}, ${pixel.z}")
                }
                countDown.countDown()
            }
        }
        countDown.await()
        val pixColor = Color()
        (0..height-1).forEach { y ->
            (0..width-1).forEach { x->
                val pixel = traceResults[y][x]
                pixColor.set(pixel.x, pixel.y, pixel.z, 1f)
                pixmap.setColor(pixColor)
                pixmap.drawPixel(x, y)
            }
        }
        texture.draw(pixmap, 0, 0)
        renderLock.unlock()
        timer.stop()
    }

    fun getReflectedRayDirection(surfNorm: Vector3, origRayDir: Vector3): Vector3 {
        //        c1 = -dot_product( N, V )
        //        RL = V + (2 * N * c1 )
        // V - orig ray dir
        // N - surface normal
        val c1 = -1f * surfNorm.dot(origRayDir)
        val reflectDir = (Vector3(surfNorm).scl(2f).scl(c1)).add(origRayDir)
        return reflectDir
    }

    //    fun getRefractedRayDirection(surfNorm: Vector3, origRayDir: Vector3) {
    //
    //
    //        //        n1 = index of refraction of original medium
    //        //                n2 = index of refraction of new medium
    //        //                n = n1 / n2
    //        //        c2 = sqrt( 1 - n2 * (1 - c1^2) )
    //        //
    //        //        Rr = (n * V) + (n * c1 - c2) * N
    //        val refrac = refracOrig / refracNew
    //        val c2 = Math.sqrt(1.0 - refracNew * (1.0 - (c1 * c1))).toFloat()
    //        val RR = Vector3(origRayDir).scl(refrac).add(Vector3(surfNorm).scl(refrac * c1 - c2))
    //    }

}

fun Vector3.setFromColor(color: Color): Vector3 {
    this.x = color.r % 255
    this.y = color.g % 255
    this.z = color.b % 255
    return this
}

fun <T> ArrayList<T>.shuffle() {
    fun randIdx(): Int {
        return MathUtils.random(0, size-1)
    }
    val newList = ArrayList<T>()
    while (this.size > 0) {
        val item: T = this.removeAt(randIdx())
        newList.add(item)
    }
    this.addAll(newList)
}

fun <T> ArrayList<T>.shuffled(): ArrayList<T> {
    val res = ArrayList<T>(this)
    res.shuffle()
    return res
}

fun <T> ArrayList<T>.copy(): ArrayList<T> {
    return ArrayList<T>(this)
}