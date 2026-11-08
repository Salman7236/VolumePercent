package io.github.salman7236.volumepercent

import kotlin.math.roundToInt

object VolumeMath {
    fun percent(index: Int, max: Int): Int =
        if (max <= 0) 0 else (index * 100f / max).roundToInt().coerceIn(0, 100)
}
