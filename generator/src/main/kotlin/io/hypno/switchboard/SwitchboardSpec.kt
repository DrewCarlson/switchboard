package io.hypno.switchboard

import com.squareup.kotlinpoet.*
import safeClassName
import safeTypeName
import safeTypeNames
import javax.lang.model.element.Element


data class SwitchboardSpec(
    val connectionBaseClass: TypeName,
    val connectionParamName: String,
    val connectionReturnClassName: ClassName,
    val connectionReturnProjections: Array<TypeName>,
    val connectionReturnTypeName: TypeName,
    val patchFunParamNames: Array<String>,
    val patchFunParams: Array<Pair<String, TypeName>>
) {
  companion object {
    fun fromAnnotation(switchboard: Switchboard): SwitchboardSpec {
      val patchFunParamNames = switchboard.patchFunParamNames
      val connectionReturnClassName = switchboard.safeClassName { connectionReturnClass }
      val connectionReturnProjections = switchboard.safeTypeNames { connectionReturnProjections }
      return SwitchboardSpec(
          connectionBaseClass = switchboard.safeTypeName(Switchboard::connectionBaseClass),
          connectionParamName = switchboard.connectionParamName,
          connectionReturnClassName = connectionReturnClassName,
          connectionReturnProjections = connectionReturnProjections,
          connectionReturnTypeName = if (connectionReturnProjections.isNotEmpty()) {
            // Apply projections
            ParameterizedTypeName.get(connectionReturnClassName, *connectionReturnProjections)
          } else {
            // No types needed
            switchboard.safeTypeName { this@safeTypeName.connectionReturnClass }
          },
          patchFunParamNames = patchFunParamNames,
          patchFunParams = switchboard.safeTypeNames { patchFunParams }
              .mapIndexed { i, typeName -> patchFunParamNames[i] to typeName }
              .toTypedArray()
      )
    }
  }

  val patchFunParamSpecs
    get() = patchFunParams.map { (name, type) ->
      ParameterSpec.builder(name, type).build()
    }

  fun isExhaustiveForElement(element: Element): Boolean {
    return connectionBaseClass != element.asType().asTypeName()
  }
}