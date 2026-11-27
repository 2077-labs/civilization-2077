package com.labs2077.civilization.engine.fog_of_war

import com.labs2077.civilization.core.models.Civilization
import com.labs2077.civilization.core.models.Color
import com.labs2077.civilization.core.models.Hexagon
import com.labs2077.civilization.core.models.Player
import com.labs2077.civilization.core.models.Resources
import com.labs2077.civilization.core.models.Unit
import com.labs2077.civilization.core.models.UnitDefinition
import com.labs2077.civilization.core.models.UnitDefinitionId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UnitVisibilityProviderTests {

    private val civilization = createCivilization()
    private val enemyCivilization = createCivilization()

    private fun createCivilization() = Civilization(player = Player(), color = Color(0x0))

    private fun addUnit(civilization: Civilization, position: Hexagon, vision: Int) {
        val definition = UnitDefinition(
            id = UnitDefinitionId("unit"),
            displayName = "Unit",
            attack = 0,
            defense = 1,
            health = 1,
            movement = 1.0,
            vision = vision,
            cost = Resources(),
        )
        civilization.add(
            Unit(
                definition = definition,
                civilization = civilization,
                position = position
            )
        )
    }

    @Test
    fun `given hexagons around own unit then visibility matches vision distance`() {
        val unitPosition = Hexagon(0, 0)
        val vision = 2

        addUnit(civilization, unitPosition, vision)

        for (q in -vision..vision) {
            for (r in -vision..vision) {
                val position = Hexagon(q, r)
                val expectedVisible = unitPosition.distanceTo(position) <= vision

                Assertions.assertEquals(
                    expectedVisible,
                    UnitVisibilityProvider.isVisible(civilization, position),
                    "Incorrect visibility for hexagon $position"
                )
            }
        }
    }

    @Test
    fun `given position beyond vision of own units then not visible`() {
        addUnit(civilization, Hexagon(0, 0), vision = 2)

        Assertions.assertFalse(UnitVisibilityProvider.isVisible(civilization, Hexagon(0, 3)))
    }

    @Test
    fun `given any own unit sees the position then visible`() {
        addUnit(civilization, Hexagon(0, 0), vision = 1)
        addUnit(civilization, Hexagon(0, 3), vision = 1)

        Assertions.assertTrue(UnitVisibilityProvider.isVisible(civilization, Hexagon(0, 4)))
    }

    @Test
    fun `given only enemy unit sees the position then not visible`() {
        addUnit(enemyCivilization, Hexagon(0, 0), vision = 5)

        Assertions.assertFalse(UnitVisibilityProvider.isVisible(civilization, Hexagon(0, 1)))
    }

    @Test
    fun `given civilization without units then nothing is visible`() {
        Assertions.assertFalse(UnitVisibilityProvider.isVisible(civilization, Hexagon(0, 0)))
    }
}
