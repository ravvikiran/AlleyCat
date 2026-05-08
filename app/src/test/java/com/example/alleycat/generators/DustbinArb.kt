package com.example.alleycat.generators

import com.example.alleycat.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.float
import java.util.UUID

/**
 * Custom Kotest arbitrary generator for Dustbin.
 * Generates valid Dustbin instances respecting the mutual exclusion invariant:
 * a dustbin cannot have both a hazard and food simultaneously.
 */
fun Arb.Companion.dustbin(): Arb<Dustbin> = arbitrary {
    val x = Arb.float(-200f..3000f).bind()
    val hasHazard = Arb.boolean().bind()

    val hazardType = if (hasHazard) {
        // When hasHazard is true, hazardType must be DOG or CRAZY_CAT (never NONE)
        if (Arb.boolean().bind()) HazardType.DOG else HazardType.CRAZY_CAT
    } else {
        HazardType.NONE
    }

    val hazardYOffset = if (hasHazard) {
        Arb.float(0f..1f).bind()
    } else {
        0f
    }

    // Mutual exclusion: hasFood must be false when hasHazard is true
    val hasFood = if (hasHazard) {
        false
    } else {
        Arb.boolean().bind()
    }

    // foodCollected can only be true when hasFood is true
    val foodCollected = if (hasFood) {
        Arb.boolean().bind()
    } else {
        false
    }

    Dustbin(
        id = UUID.randomUUID().toString(),
        x = x,
        width = DUSTBIN_WIDTH,
        height = DUSTBIN_HEIGHT,
        hasHazard = hasHazard,
        hazardType = hazardType,
        hazardYOffset = hazardYOffset,
        hasFood = hasFood,
        foodCollected = foodCollected
    )
}
