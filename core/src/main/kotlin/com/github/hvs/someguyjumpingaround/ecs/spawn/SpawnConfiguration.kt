package com.github.hvs.someguyjumpingaround.ecs.spawn

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.github.hvs.someguyjumpingaround.enums.AnimationModel
import ktx.math.vec2

data class SpawnConfiguration(
    val model: AnimationModel,
    val speedScaling: Float = 1f,
    val physicsScaling: Vector2 = vec2(1f, 1f),
    val physicsOffset: Vector2 = vec2(0f, 0f),
    val bodyType: BodyType = BodyType.DynamicBody
) {

    companion object {
        const val DEFAULT_SPEED = 3f
    }
}
