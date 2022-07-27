package com.github.hvs.someguyjumpingaround.ecs.physics

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.hvs.someguyjumpingaround.SomeGuyJumpingAround.Companion.UNIT_SCALE
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateCfg
import ktx.app.gdxError
import ktx.box2d.BodyDefinition
import ktx.box2d.body
import ktx.box2d.loop
import ktx.math.vec2

class PhysicsComponent {
    val previousPosition = vec2()
    val impulse = vec2()
    lateinit var body: Body

    companion object {

        fun EntityCreateCfg.physicsCompFromShape2D(
            world: World,
            x: Int,
            y: Int,
            shape: Shape2D
        ): PhysicsComponent {
            when(shape) {

                //for other shapes, see video 12 at 29:50
                is Rectangle -> {
                    val bodyX = x + shape.x * UNIT_SCALE
                    val bodyY = y + shape.y * UNIT_SCALE
                    val bodyWidth = shape.width * UNIT_SCALE
                    val bodyHeight = shape.height * UNIT_SCALE

                    return add {
                        body = world.body(BodyType.StaticBody) {
                            position.set(bodyX, bodyY)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f, 0f),
                                vec2(bodyWidth, 0f),
                                vec2(bodyWidth, bodyHeight),
                                vec2(0f, bodyHeight)
                            )
                        }
                    }

                }
                else -> gdxError("Shape $shape is not supported")
            }
        }

        fun EntityCreateCfg.physicsComponentFromImage(
            physicsWorld: World,
            image: Image,
            bodyType: BodyType,
            fixtureAction: BodyDefinition.(PhysicsComponent, Float, Float) -> Unit //wtf is this :)
        ): PhysicsComponent {
            val positionX = image.x
            val positionY = image.y
            val width = image.width
            val height = image.height

            return add {
                body = physicsWorld.body(bodyType) {
                    //box2d starting position is dead center instead of bottom left. Therefor this calculation needs to be made
                    position.set(
                        positionX + width * 0.5f,
                        positionY + height * 0.5f
                    )
                    fixedRotation = true
                    allowSleep = false
                    this.fixtureAction(this@add, width, height) //and wtf is this
                }
            }
        }

        class PhysicsComponentListener: ComponentListener<PhysicsComponent> {
            override fun onComponentAdded(entity: Entity, component: PhysicsComponent) {
                component.body.userData = entity
            }

            override fun onComponentRemoved(entity: Entity, component: PhysicsComponent) {
                val body = component.body
                component.body.world.destroyBody(body)
                body.userData = null
            }
        }
    }
}
