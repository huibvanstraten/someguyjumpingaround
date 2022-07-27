package com.github.hvs.someguyjumpingaround.ecs.collision

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent.Companion.physicsCompFromShape2D
import com.github.hvs.someguyjumpingaround.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.body
import ktx.box2d.loop
import ktx.math.vec2
import ktx.tiled.forEachLayer
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.shape
import ktx.tiled.width

@AllOf([PhysicsComponent::class])
class CollisionSpawnSystem(
    private val physicsWorld: World
) : EventListener, IteratingSystem() {

    override fun handle(event: Event): Boolean {
        return when (event) {
            is MapChangeEvent -> {
                event.map.forEachLayer<TiledMapTileLayer> { layer ->
                    layer.forEachCell(0, 0, maxOf(event.map.width, event.map.height)) { cell, x, y ->

                        if (cell.tile.objects.isEmpty()) {
                            //cell is not linked to collision object so we return
                            return@forEachCell
                        }

                        cell.tile.objects.forEach { mapObject ->
                            world.entity {
                                physicsCompFromShape2D(physicsWorld, x, y, mapObject.shape)
                            }
                        }
                    }
                }

                // world boundary
                world.entity {
                    val mapWidth = event.map.width.toFloat()
                    val mapHeight = event.map.height.toFloat()

                    add<PhysicsComponent> {
                        body = physicsWorld.body(BodyDef.BodyType.StaticBody) {
                            position.set(0f, 0f)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f, 0f),
                                vec2(mapWidth, 0f),
                                vec2(mapWidth, mapHeight),
                                vec2(0f, mapHeight)
                            )
                        }
                    }
                }
                true
            }
            else -> false
        }
    }


    override fun onTickEntity(entity: Entity) {

    }

    private fun TiledMapTileLayer.forEachCell(
        startX: Int,
        startY: Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ) {
        for (x in startX - size..startX + size) {
            for (y in startY - size..startY + size) {
                this.getCell(x, y)?.let { action(it, x, y) }
            }
        }
    }
}
