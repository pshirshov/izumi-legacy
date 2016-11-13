package org.bitbucket.pshirshov.izumitk.cdi

import akka.actor.{Actor, ActorSystem, IndirectActorProducer, Props}
import com.google.inject.{Inject, Injector}

import scala.reflect._

case class GuiceSystem @Inject()(
                               system: ActorSystem
                               , injector: Injector

                             ) {
  def create[Producer <: IndirectActorProducer : ClassTag](args: AnyRef*) = {
    system.actorOf(Props(classTag[Producer].runtimeClass, Seq(injector) ++ args.toSeq :_*))
  }
}


class GuiceActorProducer[T <: Actor : ClassTag](injector: Injector) extends IndirectActorProducer {
  override def produce(): Actor = injector.getInstance(actorClass)

  override def actorClass: Class[_ <: Actor] = classTag[T].runtimeClass.asInstanceOf[Class[T]]
}


trait GuiceActorFactory[T <: Actor] {
  type AT = T
}

abstract class GuiceActorFactoryProducer[FT <: GuiceActorFactory[_] : ClassTag](injector: Injector) extends IndirectActorProducer {
  protected val factory = injector.getInstance(classTag[FT].runtimeClass).asInstanceOf[FT]

  override def actorClass: Class[_ <: Actor] = classTag[FT#AT].runtimeClass.asInstanceOf[Class[Actor]]
}

