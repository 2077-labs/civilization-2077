package com.Labs2077.civgame.client.input

import com.Labs2077.civgame.client.rendering.camera.GameCamera
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import kotlin.math.sqrt

class MapInputController(
    private val camera: GameCamera
) : InputAdapter() {

    /*
    * Przechowują informacje o maksymalnie dwóch punktach dotyku.
    *
    * pointerDown określa, czy dany palec aktualnie dotyka ekranu.
    * pointerX i pointerY przechowują jego ostatnią pozycję w pikselach ekranu.
    *
    * previousPinchDistance zapamiętuje poprzednią odległość między palcami,
    * dzięki czemu możemy obliczyć zmianę zoomu podczas gestu pinch.
    */
    private val pointerDown = BooleanArray(2)
    private val pointerX = FloatArray(2)
    private val pointerY = FloatArray(2)

    private var previousPinchDistance = 0f

    /**
     * Oblicza aktualną odległość między dwoma punktami dotyku.
     *
     * Wykorzystuje twierdzenie Pitagorasa do obliczenia długości
     * odcinka łączącego pozycje obu palców na ekranie.
     **/
    private fun distanceBetweenPointers(): Float {
        val dx = pointerX[1] - pointerX[0]
        val dy = pointerY[1] - pointerY[0]

        return sqrt(dx * dx + dy * dy)
    }


    override fun touchDown(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {
        if (pointer !in pointerDown.indices) return false

        pointerDown[pointer] = true
        pointerX[pointer] = screenX.toFloat()
        pointerY[pointer] = screenY.toFloat()

        if (pointerDown.all { it }) {
            previousPinchDistance = distanceBetweenPointers()
        }

        return true
    }

    override fun touchDragged(
        screenX: Int,
        screenY: Int,
        pointer: Int
    ): Boolean {
        if (pointer !in pointerDown.indices) return false
        if (!pointerDown[pointer]) return false

        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height

        if (screenWidth == 0 || screenHeight == 0) return false

        val deltaX = screenX - pointerX[pointer]
        val deltaY = screenY - pointerY[pointer]

        pointerX[pointer] = screenX.toFloat()
        pointerY[pointer] = screenY.toFloat()

        if (pointerDown.count { it } == 2) {

            val currentDistance = distanceBetweenPointers()

            if (previousPinchDistance > 0f && currentDistance > 0f) {
                val newZoom = (
                        camera.zoom * previousPinchDistance / currentDistance
                        ).coerceIn(0.5f, 3f)

                camera.changeZoom(newZoom)
            }

            previousPinchDistance = currentDistance

        } else if (pointerDown.count { it } == 1) {
            /*
             * Przeliczamy przesunięcie palca z pikseli ekranu na jednostki świata gry.
             *
             * Kamera ortograficzna wyświetla obszar świata o szerokości:
             * viewportWidth * zoom
             * oraz wysokości:
             * viewportHeight * zoom.
             *
             * Obszar ten zajmuje odpowiednio screenWidth i screenHeight pikseli.
             * Dzieląc rozmiar widocznego świata przez rozmiar ekranu,
             * otrzymujemy liczbę jednostek świata przypadających na jeden piksel.
             *
             * Mnożąc tę wartość przez przesunięcie palca (deltaX lub deltaY),
             * otrzymujemy odpowiadające mu przesunięcie w jednostkach świata gry.
             *
             * Uwzględnienie zoomu sprawia, że przesuwanie działa poprawnie
             * niezależnie od aktualnego poziomu przybliżenia kamery.
             */
            val worldDeltaX =
                deltaX * camera.viewportWidth *
                        camera.zoom / screenWidth

            val worldDeltaY =
                deltaY * camera.viewportHeight *
                        camera.zoom / screenHeight

            camera.moveBy(-worldDeltaX, worldDeltaY)
        }

        return true
    }

    override fun touchUp(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {
        if (pointer !in pointerDown.indices) return false

        pointerDown[pointer] = false
        pointerX[pointer] = screenX.toFloat()
        pointerY[pointer] = screenY.toFloat()

        previousPinchDistance = 0f

        return true
    }
}
