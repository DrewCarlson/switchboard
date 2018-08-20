import com.google.auto.common.MoreTypes
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

fun TypeMirror.asTypeElement() = MoreTypes.asTypeElement(this)

inline fun <T> T.safeTypeName(func: T.() -> KClass<*>) = try {
  func().asTypeName()
} catch (e: MirroredTypeException) {
  MoreTypes.asTypeElement(e.typeMirror).asType().asTypeName()
}

inline fun <T> T.safeTypeNames(func: T.() -> Array<KClass<*>>): Array<TypeName> = try {
  func().map { it.asTypeName() }.toTypedArray()
} catch (e: MirroredTypesException) {
  e.typeMirrors.map { it.asTypeName() }.toTypedArray()
}

inline fun <T> T.safeClassName(func: T.() -> KClass<*>) = try {
  func().asClassName()
} catch (e: MirroredTypeException) {
  MoreTypes.asTypeElement(e.typeMirror).asClassName()
}