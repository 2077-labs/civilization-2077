package com.Labs2077.civgame.client.assets

import com.Labs2077.civgame.client.assets.audio.SfxId
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.labs2077.civilization.core.models.CivilizationId
import com.labs2077.civilization.core.models.TerrainDefinitionId
import com.labs2077.civilization.core.models.UnitDefinitionId

interface AssetProvider {

    fun icon(id: String): TextureRegion
    fun sound(sound: SfxId): Sound
    val uiFont: BitmapFont

    fun fxFrames(id: String): Array<out TextureRegion>

    fun cityFrames(
        civilizationId: CivilizationId,
        level: Int,
    ): Array<out TextureRegion>

    fun cityAnimation(
        civilizationId: CivilizationId,
        level: Int,
    ): Animation<TextureRegion>

    fun unitFrames(
        civilizationId: CivilizationId,
        unitTypeId: UnitDefinitionId,
        animation: String,
    ): Array<out TextureRegion>

    fun unitAnimation(
        civilizationId: CivilizationId,
        unitTypeId: UnitDefinitionId,
        animation: String,
        mode: Animation.PlayMode = Animation.PlayMode.LOOP,
    ): Animation<TextureRegion>

    fun terrain(
        id: TerrainDefinitionId,
    ): TextureRegion
}
