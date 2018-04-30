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
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class SwitchboardGenerator : KotlinAbstractProcessor(), KotlinMetadataUtils {

  override fun getSupportedAnnotationTypes() = mutableSetOf(
      Switchboard::class.java.name
  )

  override fun getSupportedSourceVersion() = SourceVersion.latest()!!

  private val procEnv: ProcessingEnvironment
    get() = (this as KotlinAbstractProcessor).processingEnv

  override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
    roundEnv.getElementsAnnotatedWith(Switchboard::class.java)
        .forEach {
          val switchboard = it.getAnnotation(Switchboard::class.java)
          val className = it.simpleName.toString()
          val pack = procEnv.elementUtils.getPackageOf(it).toString()
          generateSwitchboard(it, SwitchboardSpec.fromAnnotation(switchboard), className, pack)
        }
    return true
  }

  private fun generateSwitchboard(specElement: Element, spec: SwitchboardSpec, className: String, pack: String) {
    val fileName = "${className}Switchboard"

    val connections = specElement.enclosedElements.filter { it.kotlinMetadata != null }
    val objects = connections.filter { it.classProto.classKind == ProtoBuf.Class.Kind.OBJECT }
    val dataClasses = connections.filter { it.classProto.isDataClass }

    val requiresExhaustion = spec.isExhaustiveForElement(specElement) || connections.isEmpty()

    val file = FileSpec.builder(pack, fileName)
        .addType(TypeSpec.interfaceBuilder(fileName)
            .addFunction(FunSpec.builder("patch")
                .addParameters(spec.patchFunParamSpecs)
                .addParameter(spec.connectionParamName, spec.connectionBaseClass)
                .addCode(CodeBlock.builder()
                    .indent()
                    .add("return when (%L) {\n", spec.connectionParamName)
                    .indent()
                    .apply {
                      val params = spec.patchFunParamNames + spec.connectionParamName
                      val paramOnlyInsert = spec.patchFunParamNames
                          .mapIndexed { index, s ->
                            if (index == 0) "%L" else ", %L"
                          }.joinToString("", "", "")
                      val fullInsert = params.mapIndexed { index, s ->
                        if (index == 0) "%L" else ", %L"
                      }.joinToString("", "", "")

                      objects.forEach {
                        addStatement("%T -> ${it.asFunName}($paramOnlyInsert)", it.asType().asTypeName(), *spec.patchFunParamNames)
                      }
                      dataClasses.forEach {
                        addStatement("is %T -> ${it.asFunName}($fullInsert)", it.asType().asTypeName(), *params)
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
                    .addParameter(spec.connectionParamName, spec.connectionBaseClass)
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

    file.writeTo(File(generatedDir, "$fileName.kt"))
  }

  private fun logError(message: String) =
      procEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)

  private val Element.asFunName: String
    get() = simpleName.run { first().toLowerCase() + substring(1, simpleName.length) }

  private val Element.classProto: ProtoBuf.Class
    get() = (kotlinMetadata as KotlinClassMetadata).data.classProto
}
