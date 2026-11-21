package com.Labs2077.civgame.client.screens

import com.Labs2077.civgame.CivGame
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

/** Ekran ładowania: co klatkę woła assets.updateLoading(), a gdy skończy, przechodzi do menu. */
class MenuLoadingScreen(private val game: CivGame) : ScreenAdapter() {
    private val viewport = FitViewport(1280f, 720f)
    private lateinit var shapes: ShapeRenderer

    override fun show() {
        shapes = ShapeRenderer()
    }

    override fun render(delta: Float) {
        // AssetManager ładuje po trochu w każdej klatce, więc nie blokujemy wątku renderowania. Gdy skończy, przechodzimy do menu.
        if (game.assets.updateLoading()) {
            game.screen = MenuScreen(game)
            return
        }
        ScreenUtils.clear(0.07f, 0.08f, 0.12f, 1f)
        viewport.apply()
        shapes.projectionMatrix = viewport.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.setColor(0.2f, 0.24f, 0.32f, 1f)
        shapes.rect(340f, 340f, 600f, 40f)
        shapes.setColor(0.1f, 0.65f, 0.95f, 1f)
        shapes.rect(340f, 340f, 600f * game.assets.progress, 40f)
        shapes.end()
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)

    override fun hide() {
        shapes.dispose()
    }
}
