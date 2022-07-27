package com.github.hvs.someguyjumpingaround.ecs.collision

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.hvs.someguyjumpingaround.ecs.tiled.TiledComponent
import com.github.hvs.someguyjumpingaround.event.CollisionDespawnEvent
import com.github.hvs.someguyjumpingaround.event.fire
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    private val tiledComponents: ComponentMapper<TiledComponent>,
    private val stage: Stage
): IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(tiledComponents[entity]) {
            if (nearbyEntities.isEmpty()) {
                stage.fire(CollisionDespawnEvent(cell))
                world.remove(entity)
            }
        }
    }
}
