package com.github.hvs.someguyjumpingaround.ecs.attack

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.ecs.life.LifeComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsSystem.Companion.entity
import com.github.hvs.someguyjumpingaround.ecs.spawn.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import com.github.hvs.someguyjumpingaround.enums.AttackState
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.query
import ktx.math.component1
import ktx.math.component2

@AllOf([AttackComponent::class, PhysicsComponent::class, ImageComponent::class])
class AttackSystem(
    private val attackComponents: ComponentMapper<AttackComponent>,
    private val physicsComponents: ComponentMapper<PhysicsComponent>,
    private val imageComponents: ComponentMapper<ImageComponent>,
    private val lifeComponents: ComponentMapper<LifeComponent>,
    private val physicsWorld: World
): IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val attackComponent = attackComponents[entity]

        if (attackComponent.isReady && !attackComponent.doAttack) {
            return
        }

        if (attackComponent.isPrepared && attackComponent.doAttack) {
            attackComponent.doAttack = false
            attackComponent.state = AttackState.ATTACKING
            attackComponent.delay = attackComponent.maxDelay
            return
        }

        attackComponent.delay -= deltaTime
        if (attackComponent.delay <= 0f && attackComponent.isAttacking) {
            attackComponent.state = AttackState.DEAL_DAMAGE

            val physicsComp = physicsComponents[entity]
            val image = imageComponents[entity].image
            val attackLeft = image.flipX
            val (x, y) = physicsComp.body.position
            val (offsetX, offsetY) = physicsComp.offset
            val (sizeW, sizeH) = physicsComp.size
            val halfWidth = sizeW * 0.5f
            val halfHeight = sizeH * 0.5f

            if (attackLeft) {
                AAABB_RECTANGLE.set(
                    x + offsetX - halfWidth - attackComponent.extraRange,
                    y + offsetY - halfHeight,
                    x + offsetX + halfWidth,
                    y + offsetY + halfHeight
                )

            } else {
                AAABB_RECTANGLE.set(
                    x + offsetX - halfWidth,
                    y + offsetY - halfHeight,
                    x + offsetX + halfWidth - attackComponent.extraRange,
                    y + offsetY + halfHeight
                )

            }

            physicsWorld.query(AAABB_RECTANGLE.x, AAABB_RECTANGLE.y, AAABB_RECTANGLE.width, AAABB_RECTANGLE.height) { fixture ->
                if (fixture.userData != HIT_BOX_SENSOR) {
                    return@query true
                }

                val fixtureEntity = fixture.entity
                if (fixtureEntity == entity) {
                    return@query true
                }

                configureEntity(fixtureEntity) {
                    lifeComponents.getOrNull(it)?.let { lifeComponent ->
                        lifeComponent.takeDamage += attackComponent.damage
                    }
                }
                return@query true
            }
        }

        attackComponent.state = AttackState.READY
    }

    companion object {
        val AAABB_RECTANGLE = Rectangle()
    }

}
