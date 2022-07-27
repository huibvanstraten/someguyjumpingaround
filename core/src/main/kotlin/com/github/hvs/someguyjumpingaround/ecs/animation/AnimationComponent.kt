package com.github.hvs.someguyjumpingaround.ecs.animation

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.hvs.someguyjumpingaround.enums.AnimationModel
import com.github.hvs.someguyjumpingaround.enums.AnimationType

class AnimationComponent(
    var model: AnimationModel = AnimationModel.UNDEFINED,
    var stateTime: Float = 0f,
    val playMode: Animation.PlayMode = Animation.PlayMode.LOOP
) {
    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = AnimationType.NO_ANIMATION

    fun nextAnimation(model: AnimationModel, type: AnimationType) {
        this.model = model
        nextAnimation = "${model.atlasKey}/${type.atlasKey}"
    }
}
