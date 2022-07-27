package com.github.hvs.someguyjumpingaround.ecs.animation

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.enums.AnimationType.Companion.NO_ANIMATION
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.app.gdxError
import ktx.collections.map
import ktx.log.logger

@AllOf([AnimationComponent::class, ImageComponent::class])
class AnimationSystem(
    private val textureAtlas: TextureAtlas,
    private val animationComponents: ComponentMapper<AnimationComponent>,
    private val imageComponents: ComponentMapper<ImageComponent>
): IteratingSystem() {

    //if performance issues, change to GdxCollections, perhaps empty it during some time
    private val cachedAnimations = mutableMapOf<String, Animation<TextureRegionDrawable>>()

    override fun onTickEntity(entity: Entity) {
        val animationComp = animationComponents[entity]

        if (animationComp.nextAnimation == NO_ANIMATION) {
            animationComp.stateTime += deltaTime
        } else {
            animationComp.animation = animation(animationComp.nextAnimation)
            animationComp.stateTime = 0f
            animationComp.nextAnimation = NO_ANIMATION
        }

        animationComp.animation.playMode = animationComp.playMode
        imageComponents[entity].image.drawable = animationComp.animation.getKeyFrame(animationComp.stateTime)
    }

    private fun animation(anyKeyPath: String): Animation<TextureRegionDrawable> {
        return cachedAnimations.getOrPut(anyKeyPath) {
            log.debug { "New animation is created for $anyKeyPath" }
            val regions = textureAtlas.findRegions(anyKeyPath)
            if (regions.isEmpty) {
                gdxError("There are nog texture regions for $anyKeyPath")
            }
            Animation(DEFAULT_FRAME_DURATION, regions.map { TextureRegionDrawable(it) })
        }
    }

    companion object {
        private val log = logger<AnimationSystem>()

        private const val DEFAULT_FRAME_DURATION = 1 / 8f
    }
}
