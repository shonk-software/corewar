package software.shonk.interpreter.internal

/*
 * Interface for an action that should run post instruction execution
 */
fun interface PostExecuteAction {
    fun run()
}
