package com.github.hvs.someguyjumpingaround.ecs.physics

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.github.hvs.someguyjumpingaround.ecs.collision.CollisionComponent
import com.github.hvs.someguyjumpingaround.ecs.image.ImageComponent
import com.github.hvs.someguyjumpingaround.ecs.tiled.TiledComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

@AllOf([PhysicsComponent::class, ImageComponent::class])
class PhysicsSystem(
    private val physicsWorld: World,
    private val imageComponents: ComponentMapper<ImageComponent>,
    private val physicsComponents: ComponentMapper<PhysicsComponent>,
    private val tiledComponents: ComponentMapper<TiledComponent>,
    private val collisionComponents: ComponentMapper<CollisionComponent>
) : ContactListener,
    IteratingSystem(interval = Fixed(1 / 60f)) {  //Link to "Fix your timestep" article: https://gafferongames.com/post/fix_your_timestep/

    init {
        physicsWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (physicsWorld.autoClearForces) {
            log.error { "AutoClearForces must be set to false to guarantee a correct physic simulation." }
            physicsWorld.autoClearForces = false
        }
        super.onUpdate()
        physicsWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        physicsWorld.step(deltaTime, 6, 2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicsComponent = physicsComponents[entity]

        physicsComponent.previousPosition.set(physicsComponent.body.position)

        if (!physicsComponent.impulse.isZero) {
            physicsComponent.body.applyLinearImpulse(physicsComponent.impulse, physicsComponent.body.worldCenter, true)
            physicsComponent.impulse.setZero()
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val physicsComponent = physicsComponents[entity]
        val imageComponent = imageComponents[entity]

        //setting the image in the physicsWorld with Interpolation
        val (previousX, previousY) = physicsComponent.previousPosition
        val (currentX, currentY) = physicsComponent.body.position

        imageComponent.image.run {
            setPosition(
                MathUtils.lerp(previousX, currentX, alpha) - width * 0.5f,
                MathUtils.lerp(previousY, currentY, alpha) - height * 0.5f
            )
        }
    }

    override fun beginContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity
        val isEntityATiledCollisionSensor = entityA in tiledComponents && contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledComponents && contact.fixtureB.isSensor
        val isEntityATiledCollisionFixture = entityA in collisionComponents && !contact.fixtureA.isSensor
        val isEntityBTiledCollisionFixture = entityB in collisionComponents && !contact.fixtureB.isSensor

        when {
            isEntityATiledCollisionSensor && isEntityBTiledCollisionFixture -> {
                tiledComponents[entityA].nearbyEntities += entityB
            }
            isEntityBTiledCollisionSensor && isEntityATiledCollisionFixture -> {
                tiledComponents[entityB].nearbyEntities += entityA
            }
        }
    }

    override fun endContact(contact: Contact) {
        val entityA = contact.fixtureA.entity
        val entityB = contact.fixtureB.entity
        val isEntityATiledCollisionSensor = entityA in tiledComponents && contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledComponents && contact.fixtureB.isSensor

        when {
            isEntityATiledCollisionSensor && !contact.fixtureB.isSensor -> {
                tiledComponents[entityA].nearbyEntities -= entityB
            }
            isEntityBTiledCollisionSensor && !contact.fixtureA.isSensor -> {
                tiledComponents[entityB].nearbyEntities -= entityA
            }
        }
    }


    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        contact.isEnabled = (contact.fixtureA.isStaticBody() && contact.fixtureB.isDynamicBody()) ||
                (contact.fixtureA.isDynamicBody() && contact.fixtureB.isStaticBody())
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit

    private fun Fixture.isStaticBody() = this.body.type == BodyDef.BodyType.StaticBody
    private fun Fixture.isDynamicBody() = this.body.type == BodyDef.BodyType.DynamicBody

    companion object {
        private val log = logger<PhysicsSystem>()

        val Fixture.entity: Entity
            get() = this.body.userData as Entity
    }
}
