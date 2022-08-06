package com.github.hvs.someguyjumpingaround.ecs.life

import com.github.hvs.someguyjumpingaround.ecs.dead.DeadComponent
import com.github.hvs.someguyjumpingaround.ecs.player.PlayerComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeComponents: ComponentMapper<LifeComponent>,
    private val deadComponents: ComponentMapper<DeadComponent>,
    private val playerComponents: ComponentMapper<PlayerComponent>
): IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val lifeComp = lifeComponents[entity]
        lifeComp.life = (lifeComp.life + lifeComp.regeneration * deltaTime). coerceAtMost(lifeComp.maximumLife)

        if(lifeComp.takeDamage > 0f) {
            lifeComp.life -= lifeComp.takeDamage
            lifeComp.takeDamage = 0f
        }

        if (lifeComp.isDead) {
            configureEntity(entity) {
                deadComponents.add(it) {
                    if (it in playerComponents) {
                        reviveTime = 7f
                    }
                }
            }
        }
    }
}
