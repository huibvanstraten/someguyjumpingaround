package com.github.hvs.someguyjumpingaround.ecs.life

data class LifeComponent(
    var life: Float = 30f,
    var maximumLife: Float = 30f,
    var regeneration: Float = 1f,
    var takeDamage: Float = 0f
) {

    val isDead: Boolean
    get() = life <= 0f
}
