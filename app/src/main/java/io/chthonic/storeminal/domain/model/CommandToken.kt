package io.chthonic.storeminal.domain.model

internal enum class CommandToken(val value: String) {
    GET("GET"),
    SET("SET"),
    DELETE("DELETE"),
    COUNT("COUNT"),
    BEGIN("BEGIN"),
    COMMIT("COMMIT"),
    ROLLBACK("ROLLBACK")
}