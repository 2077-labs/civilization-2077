package com.Labs2077.civgame.data

import com.Labs2077.civgame.client.decoration.TerrainDecorations

interface DecorationDefinitionRepository {
    fun getAll(): List<TerrainDecorations>
}
