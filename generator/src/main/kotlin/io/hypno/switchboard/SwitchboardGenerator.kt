package io.hypno.switchboard

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@AutoService(Processor::class)
open class SwitchboardGenerator : KotlinAbstractProcessor(), KotlinMetadataUtils {

  /**
   * 
   */
  protected open val processingHooks =
      emptyMap<KClass<out Annotation>, (elements: Set<Element>) -> Unit>()

  override fun getSupportedSourceVersion() = SourceVersion.latest()!!
  override fun getSupportedAnnotationTypes(): Set<String> =
      mutableSetOf(Switchboard::class.qualifiedName!!) +
          processingHooks.map { it.key.qualifiedName!! }

  private val procEnv: ProcessingEnvironment
    get() = (this as KotlinAbstractProcessor).processingEnv

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    roundEnv.getElementsAnnotatedWith(Switchboard::class.java).forEach { element ->
      val switchboard = element.getAnnotation(Switchboard::class.java)
      element.generateSwitchboard(
          spec = SwitchboardSpec.fromAnnotation(switchboard),
          className = "${element.simpleName}Switchboard"
      )
    }
    processingHooks.forEach { (annotationClass, processFunc) ->
      roundEnv.getElementsAnnotatedWith(annotationClass.java).apply(processFunc)
    }
    return true
  }

  protected fun Element.generateSwitchboard(spec: SwitchboardSpec, className: String) {
    val connections = enclosedElements.filter { it.kotlinMetadata != null }
    val objects = connections.filter { it.classProto.classKind == ProtoBuf.Class.Kind.OBJECT }
    val dataClasses = connections.filter { it.classProto.isDataClass }
    val sealedClasses = connections.filter {
        it.modifiers.contains(Modifier.ABSTRACT) &&
                it.enclosedElements.any {
                    innerElement -> innerElement.kind == ElementKind.CLASS
                }
    }

    val requiresExhaustion = spec.isExhaustiveForElement(this) || connections.isEmpty()

    val file = FileSpec.builder(packageName, className)
        .addType(TypeSpec.interfaceBuilder(className)
            .addFunction(FunSpec.builder("patch")
                .addParameters(spec.patchFunParamSpecs)
                .addParameter(spec.connectionParamName, spec.connectionBaseTypeName)
                .addCode(CodeBlock.builder()
                    .indent()
                    .add("return when (%L) {\n", spec.connectionParamName)
                    .indent()
                    .apply {
                      val params = spec.patchFunParamNames + spec.connectionParamName
                      val paramOnlyInsert = spec.patchFunParamNames
                          .mapIndexed { index, _ ->
                            if (index == 0) "%L" else ", %L"
                          }.joinToString("", "", "")
                      val fullInsert = params.mapIndexed { index, _ ->
                        if (index == 0) "%L" else ", %L"
                      }.joinToString("", "", "")

                      objects.forEach {
                        addStatement(
                            "%T -> ${it.asFunName}($paramOnlyInsert)",
                            it.asType().asTypeName(),
                            *spec.patchFunParamNames
                        )
                      }
                      (dataClasses + sealedClasses).forEach {
                        addStatement(
                            "is %T -> ${it.asFunName}($fullInsert)",
                            it.asType().asTypeName(),
                            *params
                        )
                      }

                      if (requiresExhaustion) {
                        addStatement("else -> drop($fullInsert)", *params)
                      }
                    }
                    .unindent()
                    .add("}\n")
                    .build())
                .returns(spec.connectionReturnTypeName)
                .build())
            .apply {
              if (requiresExhaustion) {
                addFunction(FunSpec.builder("drop")
                    .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                    .addParameters(spec.patchFunParamSpecs)
                    .addParameter(spec.connectionParamName, spec.connectionBaseTypeName)
                    .returns(spec.connectionReturnTypeName)
                    .build())
              }
            }
            .addFunctions(objects.map {
              FunSpec.builder(it.asFunName)
                  .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                  .addParameters(spec.patchFunParamSpecs)
                  .returns(spec.connectionReturnTypeName)
                  .build()
            })
            .addFunctions(dataClasses.map {
              FunSpec.builder(it.asFunName)
                  .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                  .addParameters(spec.patchFunParamSpecs)
                  .addParameter(spec.connectionParamName, it.asType().asTypeName())
                  .returns(spec.connectionReturnTypeName)
                  .build()
            })
            .build())
        .build()

    file.writeTo(generatedDir ?: throw IllegalStateException("Please use kapt."))
  }

  protected fun logError(message: String) =
      procEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)

  private val Element.asFunName: String
    get() = simpleName.run { first().toLowerCase() + substring(1, simpleName.length) }

  private val Element.classProto: ProtoBuf.Class
    get() = (kotlinMetadata as KotlinClassMetadata).data.classProto

  private val Element.packageName
    get() = procEnv.elementUtils.getPackageOf(this).toString()
}
