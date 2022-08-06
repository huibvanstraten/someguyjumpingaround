package com.github.hvs.someguyjumpingaround.ecs.control

import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.github.hvs.someguyjumpingaround.ecs.move.MoveComponent
import com.github.hvs.someguyjumpingaround.ecs.player.PlayerComponent
import com.github.hvs.someguyjumpingaround.controller.XboxInputProcessor
import com.github.hvs.someguyjumpingaround.ecs.attack.AttackComponent
import com.github.hvs.someguyjumpingaround.screen.GameScreen
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import ktx.log.logger

@AllOf([PlayerControlComponent::class])
class PlayerControlSystem(
  world: World,
  private val moveComponents: ComponentMapper<MoveComponent> = world.mapper(),
  private val attackComponents: ComponentMapper<AttackComponent> = world.mapper()
): XboxInputProcessor {

  private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
  private var playerCos = 0f
  private var playerSin = 0f
  private val pressedButtons = mutableSetOf<Int>()
  init {
      addXboxControllerListener()
  }

  override fun buttonDown(controller: Controller, buttonCode: Int): Boolean {
    pressedButtons += buttonCode
    if (buttonCode.isMovementKey()) {
      when (buttonCode) {
        UP -> playerCos = 1f
        DOWN -> playerCos = -1f
        LEFT -> playerSin = -1f
        RIGHT -> playerSin = 1f
      }
      updatePlayerMovement()
      return true
    } else if (buttonCode == XboxInputProcessor.BUTTON_B) {
      println("B")
//      playerEntities.forEach { attackComponents[it].doAttack = true }
      return true
    }
    return false
  }

  override fun buttonUp(controller: Controller, buttonCode: Int): Boolean {
    pressedButtons -= buttonCode
    if (buttonCode.isMovementKey()) {
      when (buttonCode) {
        UP -> playerCos = if (isPressed(DOWN)) -1f else 0f
        DOWN -> playerCos = if (isPressed(UP)) 1f else 0f
        RIGHT -> playerSin = if (isPressed(LEFT)) -1f else 0f
        LEFT -> playerSin = if (isPressed(RIGHT)) 1f else 0f
      }
      updatePlayerMovement()
      return true
    }
    return false
  }

  private fun updatePlayerMovement() {
    playerEntities.forEach { player ->
      with (moveComponents[player]) {
        cos = playerCos
        sin = playerSin
      }
    }
  }

  private fun Int.isMovementKey(): Boolean {
    return this == UP || this == DOWN || this == LEFT || this == RIGHT
  }

  private fun isPressed(buttonCode: Int): Boolean = buttonCode in pressedButtons

  override fun axisMoved(controller: Controller?, axisCode: Int, value: Float) = false

  companion object {
    private val log = logger<GameScreen>()

    private const val DOWN = XboxInputProcessor.BUTTON_DOWN
    private const val UP = XboxInputProcessor.BUTTON_UP
    private const val LEFT = XboxInputProcessor.BUTTON_LEFT
    private const val RIGHT = XboxInputProcessor.BUTTON_RIGHT
  }
}
