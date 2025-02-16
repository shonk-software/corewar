package software.shonk.interpreter.domain

import kotlinx.serialization.Serializable

@Serializable
data class CompileError(
    val line: Int,
    val message: String,
    val columnStart: Int,
    val columnEnd: Int,
)
