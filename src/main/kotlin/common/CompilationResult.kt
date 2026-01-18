package common

sealed class CompilationResult {
    data class Success(val content: ViewerContent, val warnings: List<String> = emptyList()) : CompilationResult()
    data class Error(val message: String) : CompilationResult()
}
