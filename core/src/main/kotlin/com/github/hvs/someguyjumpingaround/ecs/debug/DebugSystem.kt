package com.github.hvs.someguyjumpingaround.ecs.debug

import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.IntervalSystem
import ktx.assets.disposeSafely

class DebugSystem(
    private val physicsWorld: World,
    private val stage: Stage
): IntervalSystem(enabled = true) {

    private lateinit var physicsRenderer: Box2DDebugRenderer

    init {
        if (enabled) {
            physicsRenderer = Box2DDebugRenderer()
        }
    }

    override fun onTick() {
        physicsRenderer.render(physicsWorld, stage.camera.combined)
    }

    override fun onDispose() {
        if(enabled) {
            physicsRenderer.disposeSafely()
        }
    }
}
