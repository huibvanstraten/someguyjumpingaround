package com.github.hvs.someguyjumpingaround.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage

fun Stage.fire(event: Event) {
    this.root.fire(event)
}

class CollisionDespawnEvent(val cell: TiledMapTileLayer.Cell): Event()

data class MapChangeEvent(val map: TiledMap): Event()
