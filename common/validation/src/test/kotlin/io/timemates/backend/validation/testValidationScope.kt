package io.timemates.backend.validation

import io.timemates.backend.validation.ValidationFailureHandler

fun testValidationScope(block: context(ValidationFailureHandler) () -> Unit) {
    val scope = ValidationFailureHandler { message ->
        throw AssertionError("Validation failed: $message.")
    }
}