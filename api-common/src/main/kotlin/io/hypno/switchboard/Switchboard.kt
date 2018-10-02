package io.hypno.switchboard

import kotlin.reflect.KClass

/**
 * Apply this annotation to a sealed class to generate
 * a `Class`Switchboard interface, a {when} statement
 * inside of a {patch(...)} function that routes each
 * child class to a corresponding connection function.
 */
@ExperimentalMultiplatform
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
expect annotation class Switchboard(
    /**
     * An array of params that will be added in order
     * to each connection function..
     */
    val patchFunParams: Array<KClass<*>> = [],
    /**
     * An array of variable names for the corresponding [patchFunParams].
     */
    val patchFunParamNames: Array<String> = [],
    /**
     * The base class for the Switchboards connection functions.
     * Generally this is the sealed class itself, a higher-level
     * parent, or [Any].
     */
    val connectionBaseClass: KClass<*> = Any::class,
    /**
     * The param name used for a connection.
     */
    val connectionParamName: String = "connection",
    /**
     * The type returned by a {patch()} function and each
     * connection function.
     */
    val connectionReturnClass: KClass<*> = Unit::class,
    /**
     * Type projections applied to the [connectionReturnClass].
     */
    val connectionReturnProjections: Array<KClass<*>> = []
)
