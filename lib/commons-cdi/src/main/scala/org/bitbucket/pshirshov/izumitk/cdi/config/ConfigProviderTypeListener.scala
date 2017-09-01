package org.bitbucket.pshirshov.izumitk.cdi.config

import java.lang.reflect.Field

import com.google.inject.{MembersInjector, TypeLiteral}
import com.google.inject.spi.{TypeEncounter, TypeListener}
import com.typesafe.config.Config

class ConfigProviderTypeListener(config: Config) extends TypeListener {

  def isAnnotated(field: Field): Option[Conf] = {
    val annClass = classOf[Conf]
    if (field.isAnnotationPresent(annClass)) {
      return Some(field.getAnnotation(annClass))
    }

    val clazz = field.getDeclaringClass
    val fname = field.getName
    clazz.getMethods.toSeq.filter(_.getName == fname).find(_.isAnnotationPresent(annClass)).foreach {
      m =>
        return Some(m.getAnnotation(annClass))
    }

    clazz.getConstructors.flatMap(_.getParameters.toSeq).find(p => p.getName == fname && p.isAnnotationPresent(annClass)).foreach {
      p =>
        return Some(p.getAnnotation(annClass))
    }


    None
  }

  override def hear[T](typeLiteral: TypeLiteral[T], typeEncounter: TypeEncounter[T]): Unit = {
    var clazz = typeLiteral.getRawType

    while ( {
      clazz != null
    }) {
      for (field <- clazz.getDeclaredFields) {
        isAnnotated(field) match {
          case Some(ann) =>
            typeEncounter.register(new HoconMembersInjector[T](field, ann, config))

          case None =>
        }

      }
      clazz = clazz.getSuperclass
    }

  }
}


