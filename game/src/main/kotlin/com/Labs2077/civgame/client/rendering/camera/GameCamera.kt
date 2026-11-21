package com.Labs2077.civgame.client.rendering.camera

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3

class GameCamera(
    viewportWidth: Float,
    viewportHeight: Float
) : Camera() {

    var zoom: Float = 1f
        set(value) {
            require(value > 0f)
            field = value
        }

    private val lookAtTarget = Vector3()

    init {
        require(viewportWidth > 0f)
        require(viewportHeight > 0f)

        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight

        position.set(0f, 0f, 10f)

        update()
    }

    fun moveBy(deltaX: Float, deltaY: Float) {
        position.add(deltaX, deltaY, 0f)
        update()
    }

    fun changeZoom(newZoom: Float) {
        zoom = newZoom
        update()
    }

    fun moveTo(x: Float, y: Float) {
        position.set(x, y, position.z)
        update()
    }

    override fun update() {
        update(true)
    }

    override fun update(updateFrustum: Boolean) {
        lookAtTarget.set(position)
        lookAtTarget.add(direction)
        val halfWidth = viewportWidth * zoom / 2f
        val halfHeight = viewportHeight * zoom / 2f

        /*
        * Tworzy ortograficzną macierz projekcji.
        * Określa granice obszaru świata widocznego przez kamerę.
        *
        * Dzięki projekcji ortograficznej obiekty zachowują ten sam rozmiar
        * niezależnie od odległości od kamery.
        */
        projection.setToOrtho(
            -halfWidth, halfWidth,
            -halfHeight, halfHeight,
            near, far
        )
        view.setToLookAt(position, lookAtTarget, up)

        // Łączy macierz projekcji z macierzą widoku kamery.
        // Wynikowa macierz jest wykorzystywana przez SpriteBatch podczas rysowania.
        combined.set(projection)
        combined.mul(view)

        invProjectionView.set(combined)
        invProjectionView.inv()

        if (updateFrustum) {
            frustum.update(invProjectionView)
        }
    }

}
