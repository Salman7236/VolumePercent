package io.github.salman7236.volumepercent

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeMathTest {
    @Test fun zeroIsZero() = assertEquals(0, VolumeMath.percent(0, 25))
    @Test fun fullIsHundred() = assertEquals(100, VolumeMath.percent(25, 25))
    @Test fun pixelStepIsFourPercent() = assertEquals(20, VolumeMath.percent(5, 25))
    @Test fun roundsToNearest() = assertEquals(33, VolumeMath.percent(1, 3))
    @Test fun clampsAboveMax() = assertEquals(100, VolumeMath.percent(30, 25))
    @Test fun zeroMaxDoesNotCrash() = assertEquals(0, VolumeMath.percent(3, 0))
}
