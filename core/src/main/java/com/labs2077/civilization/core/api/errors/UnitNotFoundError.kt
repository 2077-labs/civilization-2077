@file:Suppress("CanBeParameter")

package com.labs2077.civilization.core.api.errors

import com.labs2077.civilization.core.models.Error
import com.labs2077.civilization.core.models.UnitId

class UnitNotFoundError(val unitId: UnitId) : //
    Error("Jednostka nie została odnaleziona: $unitId.")
