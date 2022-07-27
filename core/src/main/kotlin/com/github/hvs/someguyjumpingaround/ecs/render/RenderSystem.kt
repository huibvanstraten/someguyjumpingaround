package com.github.hvs.someguyjumpingaround.ecs.render

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.hvs.someguyjumpingaround.SomeGuyJumpingAround.Companion.UNIT_SCALE
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.collection.compareEntity
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.tiled.forEachLayer

@AllOf([ImageComponent::class])
class RenderSystem(
    private val stage: Stage,
    private val imageComponents: ComponentMapper<ImageComponent>
) : EventListener,
    IteratingSystem(comparator = compareEntity { e1, e2 -> imageComponents[e1].compareTo(imageComponents[e2]) }
    ) {

    private val backgroundLayers = mutableListOf<TiledMapTileLayer>()
    private val foregroundLayers = mutableListOf<TiledMapTileLayer>()
    private val mapRenderer = OrthogonalTiledMapRenderer(null, UNIT_SCALE, stage.batch)
    private val orthoCam = stage.camera as OrthographicCamera

    override fun onTick() {
        super.onTick()

        with(stage) {
            viewport.apply()

            AnimatedTiledMapTile.updateAnimationBaseTime() //for animations in the map. mapRenderer.Render() does this automatically
            mapRenderer.setView(orthoCam)

            //investigate mapRenderer.Render() as substitute. Why is it necessary to map first background, then foreground?
            if (backgroundLayers.isNotEmpty()) {
                stage.batch.use(orthoCam.combined) {
                    backgroundLayers.forEach { mapRenderer.renderTileLayer(it)}
                }
            }

            act(deltaTime)
            draw()

            if (foregroundLayers.isNotEmpty()) {
                stage.batch.use(orthoCam.combined) {
                    foregroundLayers.forEach { mapRenderer.renderTileLayer(it)}
                }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        imageComponents[entity].image.toFront()
    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is MapChangeEvent -> {
                backgroundLayers.clear()
                foregroundLayers.clear()

                event.map.forEachLayer<TiledMapTileLayer> { layer ->
                    if (layer.name.startsWith("fore")) {
                        foregroundLayers.add(layer)
                    } else {
                        backgroundLayers.add(layer)
                    }
                }
                return true
            }
        }
        return false
    }

    override fun onDispose() {
        mapRenderer.disposeSafely()
    }
}
