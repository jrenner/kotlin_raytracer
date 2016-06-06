package org.jrenner.raytracer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray


fun Vector3.randomize(): Vector3 {
    x = rand(-1f, 1f)
    y = rand(-1f, 1f)
    z = rand(-1f, 1f)
    this.nor()
    return this
}

fun Color.randomize(): Color {
    r = rand()
    g = rand()
    b = rand()
    return this
}

fun rand(n: Float=1f): Float {
    return MathUtils.random(n)
}

fun rand(n1: Float, n2: Float): Float {
    return MathUtils.random(n1, n2)
}

fun Vector3.clamp(): Vector3 {
    x = MathUtils.clamp(x, 0f, 1f)
    y = MathUtils.clamp(y, 0f, 1f)
    z = MathUtils.clamp(z, 0f, 1f)
    return this
}

fun <T> Array<T>.random(): T {
    val idx = MathUtils.random(size-1)
    return this[idx]
}

val Float.fmt: String get() = "%.2f".format(this)

val Vector3.fmt: String get() {
    return "x: ${x.fmt} y: ${y.fmt} z: ${z.fmt}"
}

val Ray.fmt: String get() {
    return "origin: ${origin.fmt}, dir: ${direction.fmt}"
}