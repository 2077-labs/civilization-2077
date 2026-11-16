package com.labs2077.civilization.core.models

import com.labs2077.civilization.core.polyfill.allUniqueBy

// TODO: Zastanowić się, czy nie warto uprościć tego systemu - istnieje on tylko ze względu na pole radiacji i budynek deep-vault.
//       Efektywnie jest to mechanizm ulepszeń terenu, tylko że wprowadzony na potrzeby budynków.
//       Czy nie byłoby dobrze dla każdego pola radiacji wymagać deep-vault jako ulepszenia (a nie jednego budynku w mieście)?
//       Ulepszenia byłyby wówczas częścią kafelka (teren + jednostki + ulepszenia), a obliczenia odbywałyby się tylko w jego ramach.

/**
 * Określa sposób zastosowania efektu budynku do produkcji terenu.
 */
enum class ProductionEffectMode {
    /** Dodaje zasoby do aktualnej produkcji. */
    ADD,

    /** Zastępuje produkcję bazową terenu. */
    REPLACE,
}

/**
 * Opisuje pojedynczy efekt budynku wpływający na produkcję terenu
 *
 * @property buildingTypeId identyfikator budynku aktywującego efekt
 * @property mode określa, czy efekt dodaje zasoby, czy zastępuje produkcję bazową.
 * @property resources wartości zasobów związane z efektem.
 * @property priority priorytet efektu REPLACE; większa wartość oznacza pierwszeństwo.
 */
data class BuildingProductionModifier(
    val buildingTypeId: BuildingDefinitionId,
    val mode: ProductionEffectMode,
    val resources: Resources,
    val priority: Int,
)

/**
 * Określa sposób obliczania produkcji zasobów przez dany teren.
 */
sealed interface TerrainProduction {
    fun calculateProduction(buildingTypeIds: Set<BuildingDefinitionId>): Resources

    /**
     * Reprezentuje stałą produkcję terenu, niezależną od posiadanych budynków.
     *
     * @property resources zasoby produkowane przez teren
     */
    data class Fixed(val resources: Resources) : TerrainProduction {
        override fun calculateProduction(buildingTypeIds: Set<BuildingDefinitionId>): Resources {
            return resources
        }
    }

    /**
     * Oblicza produkcję terenu z uwzględnieniem efektów posiadanych budynków.
     *
     * Efekt REPLACE o najwyższym priorytecie zastępuje produkcję bazową,
     * a aktywne efekty ADD są do niej dodawane.
     *
     * @property baseResources bazowa produkcja terenu
     * @property modifiers lista efektów zależnych od budynków
     */
    data class BuildingModifiers(
        val baseResources: Resources, val modifiers: List<BuildingProductionModifier>
    ) : TerrainProduction {

        init {
            require(hasUniqueReplacePriorities()) {
                "Efekty typu REPLACE muszą mieć różne priorytety: $modifiers"
            }
        }

        /**
         * Oblicza produkcję terenu na podstawie posiadanych budynków.
         *
         * @param buildingTypeIds budynki dostępne w mieście
         * @return zestaw wyprodukowanych zasobów
         */
        override fun calculateProduction(buildingTypeIds: Set<BuildingDefinitionId>): Resources {
            val activeModifiers = modifiers.filter {
                it.buildingTypeId in buildingTypeIds
            }

            val replacement = activeModifiers //
                .filter { it.mode == ProductionEffectMode.REPLACE }
                .maxByOrNull { it.priority }

            var result = replacement?.resources ?: baseResources

            activeModifiers //
                .filter { it.mode == ProductionEffectMode.ADD }
                .forEach { modifier ->
                    result += modifier.resources
                }

            return result
        }

        /** Sprawdza, czy wszystkie modyfikatory typu REPLACE mają unikatowe priorytety. */
        private fun hasUniqueReplacePriorities(): Boolean {
            val replaceModifiers = modifiers.filter { it.mode == ProductionEffectMode.REPLACE }
            return replaceModifiers.allUniqueBy { it.priority }
        }
    }
}
