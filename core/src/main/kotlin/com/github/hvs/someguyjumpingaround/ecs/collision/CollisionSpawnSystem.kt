package com.github.hvs.someguyjumpingaround.ecs.collision

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent.Companion.physicsCompFromShape2D
import com.github.hvs.someguyjumpingaround.ecs.tiled.TiledComponent
import com.github.hvs.someguyjumpingaround.event.CollisionDespawnEvent
import com.github.hvs.someguyjumpingaround.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.body
import ktx.box2d.loop
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.forEachLayer
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.layer
import ktx.tiled.shape
import ktx.tiled.width

@AllOf([PhysicsComponent::class, CollisionComponent::class])
class CollisionSpawnSystem(
    private val physicsWorld: World,
    private val physicsComponents: ComponentMapper<PhysicsComponent>
): EventListener, IteratingSystem() {

    private val tiledLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<Cell>()

    override fun handle(event: Event): Boolean {
        return when (event) {
            is MapChangeEvent -> {
                event.map.layers.getByType(TiledMapTileLayer::class.java, tiledLayers)

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
            is CollisionDespawnEvent -> {
                processedCells.remove(event.cell)
                true
            }
            else -> false
        }
    }


    override fun onTickEntity(entity: Entity) {
        val (entityX, entityY) = physicsComponents[entity].body.position

        tiledLayers.forEach { layer ->
            layer.forEachCell(entityX.toInt(), entityY.toInt(), SPAWN_AREA_SIZE) { cell, x, y ->

                if (cell.tile.objects.isEmpty()) {
                    //cell is not linked to collision object so we return
                    return@forEachCell
                }

                if (cell in processedCells) {
                    return@forEachCell
                }

                processedCells.add(cell)
                cell.tile.objects.forEach { mapObject ->
                    world.entity {
                        physicsCompFromShape2D(physicsWorld, x, y, mapObject.shape)
                        add<TiledComponent> {
                            this.cell = cell
                            nearbyEntities.add(entity)
                        }
                    }
                }
            }
        }
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

    companion object {
        const val SPAWN_AREA_SIZE = 3
    }
}
