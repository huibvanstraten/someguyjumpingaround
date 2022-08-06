package com.github.hvs.someguyjumpingaround.ecs.dead

import com.github.hvs.someguyjumpingaround.ecs.life.LifeComponent
import com.github.hvs.someguyjumpingaround.ecs.player.PlayerComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadComponents: ComponentMapper<DeadComponent>,
    private val lifeComponents: ComponentMapper<LifeComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val deadComp = deadComponents[entity]
        if (deadComp.reviveTime == 0f) {
            world.remove(entity)
            return
        }

        deadComp.reviveTime -= deltaTime
        if (deadComp.reviveTime <= 0f) {
            with(lifeComponents[entity]) { life = maximumLife }
            configureEntity(entity) { deadComponents.remove(entity) }
        }
    }
}
