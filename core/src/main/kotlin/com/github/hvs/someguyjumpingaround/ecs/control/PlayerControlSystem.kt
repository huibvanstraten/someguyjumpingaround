package com.github.hvs.someguyjumpingaround.ecs.control

import com.badlogic.gdx.controllers.Controller
import com.github.hvs.someguyjumpingaround.ecs.move.MoveComponent
import com.github.hvs.someguyjumpingaround.ecs.player.PlayerComponent
import com.github.hvs.someguyjumpingaround.controller.XboxInputProcessor
import com.github.hvs.someguyjumpingaround.screen.GameScreen
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.log.logger

@AllOf([PlayerControlComponent::class])
class PlayerControlSystem(
  world: World,
  private val moveComponents: ComponentMapper<MoveComponent>
): XboxInputProcessor {

  private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

  init {
      addXboxControllerListener()
  }

  override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
    when (buttonCode) {
      XboxInputProcessor.BUTTON_B -> println("B")
      XboxInputProcessor.BUTTON_A -> println("A")
      XboxInputProcessor.BUTTON_X -> println("X")
      XboxInputProcessor.BUTTON_Y -> println("Y")
      XboxInputProcessor.BUTTON_UP -> println("UP")
      XboxInputProcessor.BUTTON_DOWN -> println("DOWN")
      else -> return false
    }
    return true
  }

  override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
    updatePlayerMovement(axisCode, value)
    return true
  }

  override fun buttonUp(controller: Controller?, buttonCode: Int) = false

  //TODO: player is sliding backwards
  private fun updatePlayerMovement(axisCode: Int, value: Float) {
    playerEntities.forEach { player ->
      with (moveComponents[player]) {
        if (axisCode == XboxInputProcessor.AXIS_X_LEFT) {
          sine = value
        } else if (axisCode == XboxInputProcessor.AXIS_Y_LEFT) {
          cosine = -value
        }
      }
    }
  }

  companion object {
    private val log = logger<GameScreen>()
  }
}
