package io.hypno.switchboard

import asTypeElement
import safeClassName
import com.google.auto.service.AutoService
import kt.mobius.Next
import javax.annotation.processing.Processor
import javax.lang.model.element.Element
import kotlin.reflect.KClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

@AutoService(Processor::class)
class MobiusSwitchboardGenerator : SwitchboardGenerator() {

  override val processingHooks: Map<KClass<out Annotation>, (Set<Element>) -> Unit> = mapOf(
      MobiusUpdateSpec::class to { elements ->
        elements.forEach { element ->
          val updateSpec = element.getAnnotation(MobiusUpdateSpec::class.java)
          element.generateSwitchboard(
              spec = updateSpec.asSwitchboardSpec(element),
              className = when {
                updateSpec.prefix.isNotBlank() ->
                  "${updateSpec.prefix}UpdateSpec"
                else -> "${element.simpleName}UpdateSpec"
              }
          )
        }
      },
      MobiusHandlerSpec::class to { elements ->
        elements.forEach {
          val mobiusHandlerSpec = it.getAnnotation(MobiusHandlerSpec::class.java)
          it.generateSwitchboard(
              spec = mobiusHandlerSpec.asSwitchboardSpec(it),
              className = "${it.simpleName}HandlerSpec"
          )
        }
      }
  )

  private fun MobiusUpdateSpec.asSwitchboardSpec(targetElement: Element): SwitchboardSpec {
    val targetTypeMirror = targetElement.asType()
    val targetElementInterfaces = targetTypeMirror.asTypeElement().interfaces
    val baseModelClassName = safeClassName { baseModel }
    val baseEventClassName = safeClassName { baseEvent }
    val baseEffectClassName = safeClassName { baseEffect }
    return SwitchboardSpec(
        patchFunParams = arrayOf("model" to when (baseModelClassName.simpleName) {
          "Object" -> Any::class.asTypeName()
          else -> baseModelClassName
        }),
        patchFunParamNames = arrayOf("model"),
        connectionBaseTypeName = when {
          // UseInterface and one is available
          baseEventClassName.isKClass(UseInterface::class) && targetElementInterfaces.isNotEmpty() -> {
            targetElementInterfaces.first().asTypeName()
          }
          // UseInterface and none are available, behave like UseTargetClass
          baseEventClassName.isKClass(UseInterface::class) && targetElementInterfaces.isEmpty() -> {
            targetTypeMirror.asTypeName()
          }
          baseEventClassName.isKClass(UseTargetClass::class) -> {
            targetTypeMirror.asTypeName()
          }
          // No option (or Any/Unit), using Any
          baseEventClassName.simpleName == "Object" -> Any::class.asTypeName()
          // Use provided type
          else -> baseEventClassName
        },
        connectionParamName = "event",
        connectionReturnClassName = Next::class.asClassName(),
        connectionReturnTypeName = Next::class.asClassName()
            .parameterizedBy(
                when (baseModelClassName.simpleName) {
                  "Object" -> Any::class.asTypeName()
                  else -> baseModelClassName
                },
                when (baseEffectClassName.simpleName) {
                  "Object" -> Any::class.asTypeName()
                  else -> baseEffectClassName
                }
            )
    )
  }

  private fun MobiusHandlerSpec.asSwitchboardSpec(targetElement: Element): SwitchboardSpec {
    val targetTypeMirror = targetElement.asType()
    val targetElementInterfaces = targetTypeMirror.asTypeElement().interfaces
    val baseEffectClassName = safeClassName { baseEffect }
    return SwitchboardSpec(
        patchFunParams = emptyArray(),
        patchFunParamNames = emptyArray(),
        connectionBaseTypeName = when {
          // UseInterface and one is available
          baseEffectClassName.isKClass(UseInterface::class) && targetElementInterfaces.isNotEmpty() -> {
            targetElementInterfaces.first().asTypeName()
          }
          // UseInterface and none are available, behave like UseTargetClass
          baseEffectClassName.isKClass(UseInterface::class) && targetElementInterfaces.isEmpty() -> {
            targetTypeMirror.asTypeName()
          }
          baseEffectClassName.isKClass(UseTargetClass::class) -> {
            targetTypeMirror.asTypeName()
          }
          // No option (or Any/Unit), using Any
          baseEffectClassName.simpleName == "Object" -> Any::class.asTypeName()
          // Use provided type
          else -> baseEffectClassName
        },
        connectionParamName = "effect",
        connectionReturnClassName = Unit::class.asClassName(),
        connectionReturnTypeName = Unit::class.asTypeName()
    )
  }

  private fun ClassName.isKClass(comp: KClass<*>) = simpleName == comp.simpleName
}
