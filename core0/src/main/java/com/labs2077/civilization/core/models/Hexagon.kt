package com.labs2077.civilization.core.models

data class Hexagon(val q: Int, val r: Int) {
    val s: Int get() = -q - r
}
