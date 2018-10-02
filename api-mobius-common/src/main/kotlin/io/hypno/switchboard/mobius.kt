package io.hypno.switchboard

import kotlin.reflect.KClass


/**
 * A configuration flag that indicates the
 * first interface of the target class should
 * be used as the base input or output type.
 */
class UseInterface

/**
 * A configuration flag that indicates the target
 * class should be used as the base input or
 * output type.
 */
class UseTargetClass

/**
 * Generates a Switchboard compatible with a Mobius
 * Update<M, E, F> function by using Next<M, F>
 * for Patch, Drop, and all Connections.
 */
@ExperimentalMultiplatform
@OptionalExpectation
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
expect annotation class MobiusUpdateSpec(
    /**
     * By default, generated specs will be prefixed
     * with the target's class name. Use this option
     * to manually define the prefix.
     */
    val prefix: String = "",
    /**
     * The base type for M in a Update<M, E, F> function.
     */
    val baseModel: KClass<*> = Any::class,
    /**
     * The base type for E in a Update<M, E, F> function.
     */
    val baseEvent: KClass<*> = UseInterface::class,
    /**
     * The base type for F in a Update<M, E, F> function.
     */
    val baseEffect: KClass<*> = Any::class
)

/**
 * Generates a Switchboard that can be used for
 * the Connection<I> in a Connectable<I, O>.
 */
@ExperimentalMultiplatform
@OptionalExpectation
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
expect annotation class MobiusHandlerSpec(
    /**
     * The base type for I in a Connection<I, O>.
     */
    val baseEffect: KClass<*> = UseInterface::class
)
