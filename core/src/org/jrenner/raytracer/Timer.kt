package org.jrenner.raytracer

import com.badlogic.gdx.utils.TimeUtils
import java.util.*


class Timer(val name: String) {
    companion object {
        val timers = ArrayList<Timer>()

        fun reportAll() {
            timers.forEach {
                it.report()
            }
        }

        const val MAX_SIZE = 5
        val programStart = getTime()
        val now: Long get() = getTime() - programStart

        fun getTime(): Long {
            return TimeUtils.nanoTime() / 10000
        }
    }

    val startTimes = ArrayList<Long>()
    val endTimes = ArrayList<Long>()

    init {
        timers.add(this)
    }

    fun start() {
        startTimes.add(now)
        while (startTimes.size >= MAX_SIZE) {
            startTimes.removeAt(0)
        }
    }

    fun stop() {
        endTimes.add(now)
        while (endTimes.size >= MAX_SIZE) {
            endTimes.removeAt(0)
        }
    }

    fun report() {
        val len = Math.min(startTimes.size, endTimes.size)
        for (n in 0..len-1) {
            val s = startTimes[n]
            val e = endTimes[n]
            println("$name [$s] --> [$e]")
        }
    }
}

