package com.example.alleycat.generators

import com.example.alleycat.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import java.util.UUID

/**
 * Custom Kotest arbitrary generator for FoodItem.
 * Generates valid FoodItem instances at various lifecycle stages.
 */
fun Arb.Companion.foodItem(): Arb<FoodItem> = arbitrary {
    val x = Arb.float(-200f..2000f).bind()
    val y = Arb.float(0f..FALL_OFF_THRESHOLD).bind()
    val velocityY = Arb.float(FOOD_INITIAL_VELOCITY_Y..50f).bind()
    val foodType = Arb.enum<FoodType>().bind()
    val sourceDustbinId = UUID.randomUUID().toString()

    FoodItem(
        id = UUID.randomUUID().toString(),
        x = x,
        y = y,
        width = FOOD_WIDTH,
        height = FOOD_HEIGHT,
        velocityY = velocityY,
        isCollected = false,
        sourceDustbinId = sourceDustbinId,
        foodType = foodType
    )
}
