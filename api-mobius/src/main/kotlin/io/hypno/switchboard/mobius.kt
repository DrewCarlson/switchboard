package io.hypno.switchboard

import kotlin.reflect.KClass

actual annotation class MobiusUpdateSpec(
    actual val prefix: String,
    actual val baseModel: KClass<*>,
    actual val baseEvent: KClass<*>,
    actual val baseEffect: KClass<*>
)

actual annotation class MobiusHandlerSpec(
    actual val baseEffect: KClass<*>
)
