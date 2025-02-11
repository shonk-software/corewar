package software.shonk.application.service

import software.shonk.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.domain.CompileError
import software.shonk.interpreter.internal.compiler.Compiler

class GetCompilationErrorsService : GetCompilationErrorsQuery {

    override fun getCompilationErrors(code: String): List<CompileError> {
        val compiler = Compiler(code)
        return compiler.allErrors.map {
            CompileError(
                line = it.lineNumber,
                message = it.message,
                columnStart = it.lineCharIndexStart,
                columnEnd = it.lineCharIndexEnd,
            )
        }
    }
}
