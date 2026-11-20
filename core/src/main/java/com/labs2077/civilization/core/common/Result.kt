package com.labs2077.civilization.core.common

import com.labs2077.civilization.core.models.Error as GameError

sealed interface Result<out T, out E : GameError> {
    data class Success<out T>(val value: T) : Result<T, Nothing>
    data class Error<out E : GameError>(val error: E) : Result<Nothing, E>
}
