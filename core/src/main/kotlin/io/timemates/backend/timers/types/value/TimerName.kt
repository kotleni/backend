package io.timemates.backend.timers.types.value

import io.timemates.backend.validation.FailureMessage
import io.timemates.backend.validation.SafeConstructor
import io.timemates.backend.validation.ValidationFailureHandler
import io.timemates.backend.validation.reflection.wrapperTypeName

@JvmInline
value class TimerName private constructor(val string: String) {
    companion object : SafeConstructor<TimerName, String>() {
        override val displayName: String by wrapperTypeName()
        val LENGTH_RANGE = 3..50

        context(ValidationFailureHandler)
        override fun create(value: String): TimerName {
            return when (value.length) {
                0 -> onFail(io.timemates.backend.validation.FailureMessage.ofBlank())
                in LENGTH_RANGE -> TimerName(value)
                else -> onFail(io.timemates.backend.validation.FailureMessage.ofSize(LENGTH_RANGE))
            }
        }
    }
}