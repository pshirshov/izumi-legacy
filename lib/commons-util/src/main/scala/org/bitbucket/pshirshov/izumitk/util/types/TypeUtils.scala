package org.bitbucket.pshirshov.izumitk.util.types

/**
  */
object TypeUtils {

  import scala.reflect.runtime.universe._

  def typeToTypeTag[T](
                        tpe: Type,
                        mirror: reflect.api.Mirror[reflect.runtime.universe.type]
                      ): TypeTag[T] = {
    TypeTag(mirror, new reflect.api.TypeCreator {
      def apply[U <: reflect.api.Universe with scala.Singleton](m: reflect.api.Mirror[U]): U#Type = {
        assert(m eq mirror, s"TypeTag[$tpe] defined in $mirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
  }
}
