/**
 * Annotate new code
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.BINARY)
annotation class Early
