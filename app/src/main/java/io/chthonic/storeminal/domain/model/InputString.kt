package io.chthonic.storeminal.domain.model

class InputString private constructor(val text: String) {
    companion object {
        fun validateOrNull(text: CharSequence?): InputString? =
            when {
                text.isNullOrBlank() -> null
                else -> InputString(text.trim().toString())
            }
    }
}