package com.Labs2077.civgame.engine.terrain

import com.Labs2077.civgame.engine.resources.ResourceBundle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Określa sposób zastosowania efektu budynku do produkcji terenu
 */
@Serializable
enum class ProductionEffectMode {
    /** Dodaje zasoby do aktualnej produkcji */
    ADD,
    /** Zastępuje produkcję bazową terenu */
    REPLACE
}

/**
 * Określa sposób obliczania produkcji zasobów przez dany teren
 */
@Serializable
sealed interface TerrainProductionRule {
    fun calculateProduction(builtBuildings: Set<String>): ResourceBundle
    /**
     * Reprezentuje stałą produkcję terenu, niezależną od posiadanych budynków.
     *
     * @property resources zasoby produkowane przez teren
     */
    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val resources: ResourceBundle
    ) : TerrainProductionRule {
        override fun calculateProduction(builtBuildings: Set<String>): ResourceBundle = resources
    }
    /**
     * Oblicza produkcję terenu z uwzględnieniem efektów posiadanych budynków
     *
     * Efekt REPLACE o najwyższym priorytecie zastępuje produkcję bazową,
     * a aktywne efekty ADD są do niej dodawane
     *
     * @property baseResources bazowa produkcja terenu
     * @property modifiers lista efektów zależnych od budynków
     */
    @Serializable
    @SerialName("building_modifiers")
    data class BuildingModifiers(
        val baseResources: ResourceBundle,
        val modifiers: List<BuildingProductionModifier>
    ) : TerrainProductionRule {

        /**
         * Oblicza produkcję terenu na podstawie posiadanych budynków
         *
         * @param builtBuildings identyfikatory budynków dostępnych w mieście
         * @return zestaw wyprodukowanych zasobów
         */
        override fun calculateProduction(
            builtBuildings: Set<String>
        ): ResourceBundle {
            val activeModifiers = modifiers.filter {
                it.buildingId in builtBuildings
            }

            val replacement = activeModifiers
                .filter { it.mode == ProductionEffectMode.REPLACE }
                .maxByOrNull { it.priority }

            var result = replacement?.resources ?: baseResources

            activeModifiers
                .filter { it.mode == ProductionEffectMode.ADD }
                .forEach { modifier ->
                    result += modifier.resources
                }
            return result
        }
    }
}
