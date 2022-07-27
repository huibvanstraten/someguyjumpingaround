package com.github.hvs.someguyjumpingaround.ecs.spawn

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Scaling
import com.github.hvs.someguyjumpingaround.SomeGuyJumpingAround.Companion.UNIT_SCALE
import com.github.hvs.someguyjumpingaround.ecs.animation.AnimationComponent
import com.github.hvs.someguyjumpingaround.ecs.collision.CollisionComponent
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.ecs.move.MoveComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent.Companion.physicsComponentFromImage
import com.github.hvs.someguyjumpingaround.ecs.player.PlayerComponent
import com.github.hvs.someguyjumpingaround.ecs.spawn.SpawnConfiguration.Companion.DEFAULT_SPEED
import com.github.hvs.someguyjumpingaround.enums.AnimationModel
import com.github.hvs.someguyjumpingaround.enums.AnimationType
import com.github.hvs.someguyjumpingaround.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.app.gdxError
import ktx.box2d.box
import ktx.math.vec2
import ktx.tiled.layer
import ktx.tiled.x
import ktx.tiled.y

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    private val physicsWorld: World,
    private val atlas: TextureAtlas,
    private val spawnComponents: ComponentMapper<SpawnComponent>
) : EventListener, IteratingSystem() {
    private val cachedSpawnConfigurations = mutableMapOf<String, SpawnConfiguration>()
    private val cachedSizes = mutableMapOf<AnimationModel, Vector2>()

    override fun handle(event: Event): Boolean {
        when (event) {
            is MapChangeEvent -> {
                val entityLayer = event.map.layer("entities")
                entityLayer.objects.forEach { mapObject ->
                    val name = mapObject.name
                        ?: gdxError("MapObject $mapObject does not have a name")  //actually it is class instead of type inside the editor. problem?
                    world.entity {
                        add<SpawnComponent> {
                            this.type = name
                            this.location.set(mapObject.x * UNIT_SCALE, mapObject.y * UNIT_SCALE)
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun onTickEntity(entity: Entity) {
        with(spawnComponents[entity]) {
            val entityConfig = setSpawnConfiguration(type)
            val entitySize = getEntitySizeByIdleImage(entityConfig.model)

            world.entity {
                val imageComp = add<ImageComponent> {
                    image = Image().apply {
                        setPosition(location.x, location.y)
                        setScaling(Scaling.fill)
                        setSize(entitySize.x, entitySize.y)
                    }
                }

                add<AnimationComponent> {
                    nextAnimation(entityConfig.model, AnimationType.IDLE)
                }

                physicsComponentFromImage(physicsWorld, imageComp.image, entityConfig.bodyType)
                { _, w, h ->
                    val width = w * entityConfig.physicsScaling.x
                    val height = h * entityConfig.physicsScaling.y

                    // hit box
                    box(width, height, entityConfig.physicsOffset) {
                        isSensor = entityConfig.bodyType != BodyDef.BodyType.StaticBody
                    }

                    if (entityConfig.bodyType != BodyDef.BodyType.StaticBody) {
                        //such entities will create/remove collision objects
                        add<CollisionComponent>()

                        //collision box
                        val collisionHeight = height * 0.4f
                        val collisionOffset = vec2().apply { set(entityConfig.physicsOffset) }
                        collisionOffset.y -= height * 0.5f - collisionHeight * 0.5f
                        box(width, collisionHeight, collisionOffset)
                    }
                }

                if (entityConfig.speedScaling > 0f) {
                    add<MoveComponent> {
                        speed = DEFAULT_SPEED * entityConfig.speedScaling
                    }
                }

                if (type == "Player") {
                    add<PlayerComponent>()
                }
            }
        }
        world.remove(entity)
    }

    private fun setSpawnConfiguration(type: String): SpawnConfiguration = cachedSpawnConfigurations.getOrPut(type) {
        when (type) {
            "Player" -> SpawnConfiguration(
                model = AnimationModel.PLAYER,
                physicsScaling = vec2(0.3f, 0.3f),
                physicsOffset = vec2(0f, -10f * UNIT_SCALE)
            )
            "Slime" -> SpawnConfiguration(
                model = AnimationModel.SLIME,
                physicsScaling = vec2(0.3f, 0.3f),
                physicsOffset = vec2(0f, -2f * UNIT_SCALE)
            )
            "Chest" -> SpawnConfiguration(
                model = AnimationModel.CHEST,
                speedScaling = 0f,
                bodyType = BodyDef.BodyType.StaticBody
            )
            else -> gdxError("Type $type has no spawnConfiguration set up")
        }
    }

    private fun getEntitySizeByIdleImage(model: AnimationModel) = cachedSizes.getOrPut(model) {
        val regions = atlas.findRegions("${model.atlasKey}/${AnimationType.IDLE.atlasKey}")
        if (regions.isEmpty) {
            gdxError("There are no regions for the idle animation of model $model")
        }

        vec2(
            x = regions.first().originalWidth * UNIT_SCALE,
            y = regions.first().originalHeight * UNIT_SCALE
        )
    }
}
