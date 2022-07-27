package com.github.hvs.someguyjumpingaround.screen

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.hvs.someguyjumpingaround.ecs.animation.AnimationSystem
import com.github.hvs.someguyjumpingaround.ecs.camera.CameraSystem
import com.github.hvs.someguyjumpingaround.ecs.collision.CollisionSpawnSystem
import com.github.hvs.someguyjumpingaround.ecs.control.PlayerControlSystem
import com.github.hvs.someguyjumpingaround.ecs.debug.DebugSystem
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.ecs.move.MoveSystem
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsComponent
import com.github.hvs.someguyjumpingaround.ecs.physics.PhysicsSystem
import com.github.hvs.someguyjumpingaround.ecs.render.RenderSystem
import com.github.hvs.someguyjumpingaround.ecs.spawn.EntitySpawnSystem
import com.github.hvs.someguyjumpingaround.event.MapChangeEvent
import com.github.hvs.someguyjumpingaround.event.fire
import com.github.quillraven.fleks.World
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2

class GameScreen: KtxScreen {
    private val textureAtlas = TextureAtlas("assets/graphics/game.atlas")
    private val stage: Stage = Stage(ExtendViewport(16f, 9f))
    private val physicsWorld = createWorld(gravity = vec2()).apply {
        autoClearForces = false
    }
    private val entityWorld = entityWorld()
    lateinit var currentMap: TiledMap
    //TODO: AssetManager (7)

    init {
        entityWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                stage.addListener(sys)
            }
        }

        PlayerControlSystem(entityWorld, entityWorld.mapper())

    }

    override fun show() {
        log.debug { "This is the GameScreen" }
                currentMap = TmxMapLoader().load("maps/map1.tmx")
        //if performance issues, change to Pool, to not create a new MapChangeEvent every time it is called
        stage.fire(MapChangeEvent(currentMap))
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        entityWorld.update(delta.coerceAtMost(0.25f))
    }

    override fun dispose() {
        stage.disposeSafely()
        textureAtlas.disposeSafely()
        currentMap.disposeSafely()
        physicsWorld.disposeSafely()
        entityWorld.dispose()
    }

    private fun entityWorld():World {
        return World {
            inject(stage)
            inject(textureAtlas)
            inject(physicsWorld)

            componentListener<ImageComponent.Companion.ImageComponentListener>()
            componentListener<PhysicsComponent.Companion.PhysicsComponentListener>()

            system<EntitySpawnSystem>()
            system<CollisionSpawnSystem>()
            system<PhysicsSystem>()
            system<AnimationSystem>()
            system<MoveSystem>()
            system<CameraSystem>()
            system<RenderSystem>()
            system<DebugSystem>()
        }
    }

    companion object {
        private val log = logger<GameScreen>()
    }
}
