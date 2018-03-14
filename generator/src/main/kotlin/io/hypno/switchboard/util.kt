import com.google.auto.common.MoreTypes
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.hypno.switchboard.Switchboard
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import kotlin.reflect.KClass

fun Switchboard.safeTypeName(func: Switchboard.() -> KClass<*>): TypeName {
  return try {
    func().asTypeName()
  } catch (e: MirroredTypeException) {
    e.typeMirror.asTypeName()
  }
}

fun Switchboard.safeClassName(func: Switchboard.() -> KClass<*>): ClassName {
  return try {
    func().asClassName()
  } catch (e: MirroredTypeException) {
    MoreTypes.asTypeElement(e.typeMirror).asClassName()
  }
}

fun Switchboard.safeTypeNames(func: Switchboard.() -> Array<KClass<*>>): Array<TypeName> {
  return try {
    func().map { it.asTypeName() }.toTypedArray()
  } catch (e: MirroredTypesException) {
    e.typeMirrors.map { it.asTypeName() }.toTypedArray()
  }
}