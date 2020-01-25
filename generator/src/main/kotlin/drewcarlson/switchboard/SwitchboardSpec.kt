package drewcarlson.switchboard

import com.squareup.kotlinpoet.*
import safeClassName
import safeTypeName
import safeTypeNames
import javax.lang.model.element.Element
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


data class SwitchboardSpec(
    val connectionBaseTypeName: TypeName,
    val connectionParamName: String,
    val connectionReturnClassName: ClassName,
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
          connectionBaseTypeName = switchboard.safeTypeName { connectionBaseClass },
          connectionParamName = switchboard.connectionParamName,
          connectionReturnClassName = connectionReturnClassName,
          connectionReturnTypeName = if (connectionReturnProjections.isNotEmpty()) {
            // Apply projections
            connectionReturnClassName.parameterizedBy(*connectionReturnProjections)
          } else {
            // No types needed
            switchboard.safeTypeName { connectionReturnClass }
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
    return connectionBaseTypeName != element.asType().asTypeName()
  }
}
