package software.shonk.interpreter.internal.memory

internal data class ResolvedAddresses(
    val aFieldRead: Int,
    val aFieldWrite: Int,
    val bFieldRead: Int,
    val bFieldWrite: Int,
)
