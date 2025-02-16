package software.shonk.interpreter.application.port.incoming

import software.shonk.interpreter.domain.CompileError

interface GetCompilationErrorsQuery {

    fun getCompilationErrors(code: String): List<CompileError>
}
