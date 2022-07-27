package com.github.hvs.someguyjumpingaround

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.github.hvs.someguyjumpingaround.screen.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class SomeGuyJumpingAround : KtxGame<KtxScreen>() {

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        addScreen(GameScreen())
        setScreen<GameScreen>()
    }

    companion object {
        const val UNIT_SCALE = 1 / 16f
    }
}
