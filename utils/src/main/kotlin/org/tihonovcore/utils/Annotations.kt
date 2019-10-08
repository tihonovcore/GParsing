package org.tihonovcore.utils

/**
 * Annotate new code
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.BINARY)
annotation class Early

/**
 * Annotate experimental features
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.BINARY)
annotation class Experimental
