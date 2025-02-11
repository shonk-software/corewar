package software.shonk.application.port.incoming

import software.shonk.domain.CompileError

interface GetCompilationErrorsQuery {

    fun getCompilationErrors(code: String): List<CompileError>
}
