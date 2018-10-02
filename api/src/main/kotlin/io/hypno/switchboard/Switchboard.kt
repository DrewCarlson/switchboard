package io.hypno.switchboard

import kotlin.reflect.KClass

actual annotation class Switchboard(
    actual val patchFunParams: Array<KClass<*>>,
    actual val patchFunParamNames: Array<String>,
    actual val connectionBaseClass: KClass<*>,
    actual val connectionParamName: String,
    actual val connectionReturnClass: KClass<*>,
    actual val connectionReturnProjections: Array<KClass<*>>
)
