package org.bitbucket.pshirshov.izumitk.akka.http.util.logging

/**
  */
class WSAppender[T] extends AppenderBase[T] {
  protected val layout = new PatternLayout()

  def setPattern(s: String): Unit = {
    layout.setContext(context)
    layout.setPattern(s)
    layout.start()
  }

  override def append(eventObject: T): Unit = {
    import scala.collection.JavaConversions._

    eventObject match {
      case le: ILoggingEvent =>
        val msg = layout.doLayout(le)

        WSAppender.subscribers.foreach {
          s =>
            try {
              s.onNext(msg)
            } catch {
              case f: Throwable =>
                f.printStackTrace()
            }
        }
      case _ =>
    }
  }
}

object WSAppender {
  val subscribers = new java.util.concurrent.ConcurrentLinkedQueue[Subscriber[String]]()
}