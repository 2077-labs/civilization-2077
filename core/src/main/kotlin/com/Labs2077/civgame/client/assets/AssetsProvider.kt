package com.Labs2077.civgame.client.assets

import com.Labs2077.civgame.client.assets.audio.SfxId
import com.Labs2077.civgame.engine.rules.TerrainId
import com.Labs2077.civgame.engine.rules.UnitId
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

interface AssetProvider {

    fun terrain(id: TerrainId): TextureRegion

    fun icon(id: String): TextureRegion

    fun unitFrames(
        playerId: Int,
        unitId: UnitId,
        anim: String
    ): Array<out TextureRegion>

    fun cityFrames(
        playerId: Int,
        level: Int
    ): Array<out TextureRegion>

    fun fxFrames(id: String): Array<out TextureRegion>

    fun unitAnimation(
        playerId: Int,
        unitId: UnitId,
        anim: String,
        mode: Animation.PlayMode = Animation.PlayMode.LOOP
    ): Animation<TextureRegion>

    fun cityAnimation(
        playerId: Int,
        level: Int
    ): Animation<TextureRegion>

    fun sound(sound: SfxId): Sound

    val uiFont: BitmapFont
}
