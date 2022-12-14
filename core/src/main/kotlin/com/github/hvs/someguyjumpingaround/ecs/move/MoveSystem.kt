package com.github.hvs.someguyjumpingaround.ecs.move

import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2


// https://www.iforce2d.net/b2dtut/
@AllOf([MoveComponent::class, PhysicsComponent::class])
class MoveSystem(
    private val moveComponents: ComponentMapper<MoveComponent>,
    private val physicsComponents: ComponentMapper<PhysicsComponent>,
    private val imageComponents: ComponentMapper<ImageComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val moveComp = moveComponents[entity]
        val physicsComp = physicsComponents[entity]

        if (moveComp.sin == 0f && moveComp.cos == 0f) {  //if we are not pushing a moving button, we stop the body
            if (!physicsComp.body.linearVelocity.isZero) {
                // entity is moving -> stop it
                val bodyMass = physicsComp.body.mass
                val (velocityX, velocityY) = physicsComp.body.linearVelocity

                physicsComp.impulse.set(
                    bodyMass * (0f - velocityX),
                    bodyMass * (0f - velocityY)
                )
                return
            }
        }

        val bodyMass = physicsComp.body.mass
        val (velocityX, velocityY) = physicsComp.body.linearVelocity

        physicsComp.impulse.set( //if we are pushing we need to move
            bodyMass * (moveComp.speed * moveComp.sin - velocityX),
            bodyMass * (moveComp.speed * moveComp.cos - velocityY) //how to handle x and y speed?
        )

        imageComponents.getOrNull(entity)?.let { imageComponent ->
            if (moveComp.sin != 0f) {
                imageComponent.image.flipX = moveComp.sin < 0
            }
        }
    }
}
