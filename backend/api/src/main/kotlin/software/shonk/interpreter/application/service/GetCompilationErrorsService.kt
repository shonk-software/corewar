package software.shonk.interpreter.application.service

import software.shonk.interpreter.application.port.incoming.GetCompilationErrorsQuery
import software.shonk.interpreter.domain.CompileError
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
