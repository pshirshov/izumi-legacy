package org.bitbucket.pshirshov.izumitk.cassandra.facade

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.ZonedDateTime
import java.util.{Date, UUID}

import com.datastax.driver.core.{LocalDate, ResultSet, TupleValue, UDTValue}
import org.bitbucket.pshirshov.izumitk.cassandra.facade.WithSafeCassandraFacade.CassandraValue

import scala.collection.convert.{DecorateAsJava, DecorateAsScala}

trait SafeCassandraFacade
  extends CassandraFacadeBase
    with WithSafeCassandraFacade {
  override protected def facade: CassandraFacadeBase = this
}

trait WithSafeCassandraFacade
  extends DecorateAsJava with DecorateAsScala {

  protected def facade: CassandraFacadeBase

  // that is to reduce conversion boilerplate and increase safety a little when executing C* queries
  // http://spray.io/blog/2012-12-13-the-magnet-pattern/
  type IsCassandraValue[T] = T => CassandraValue

  import scala.language.implicitConversions

  implicit final val intAllowed: IsCassandraValue[Int] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val longAllowed: IsCassandraValue[Long] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val byteAllowed: IsCassandraValue[Byte] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val shortAllowed: IsCassandraValue[Short] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val floatAllowed: IsCassandraValue[Float] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val doubleAllowed: IsCassandraValue[Double] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val bigIntAllowed: IsCassandraValue[BigInt] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val bigDecimalAllowed: IsCassandraValue[BigDecimal] = z => CassandraValue.fromAnyRef(z.underlying())
  implicit final val booleanAllowed: IsCassandraValue[Boolean] = z => CassandraValue.fromAnyRef(Boolean.box(z))
  implicit final val byteBufferAllowed: IsCassandraValue[ByteBuffer] = CassandraValue.fromAnyRef
  implicit final val uuidAllowed: IsCassandraValue[UUID] = CassandraValue.fromAnyRef
  implicit final val stringAllowed: IsCassandraValue[String] = CassandraValue.fromAnyRef
  implicit final val dateAllowed: IsCassandraValue[Date] = CassandraValue.fromAnyRef
  implicit final val cassandraLocalDateAllowed: IsCassandraValue[LocalDate] = CassandraValue.fromAnyRef
  implicit final val inetAddressAllowed: IsCassandraValue[InetAddress] = CassandraValue.fromAnyRef
  implicit final val udtValueAllowed: IsCassandraValue[UDTValue] = CassandraValue.fromAnyRef
  implicit final val tupleValueAllowed: IsCassandraValue[TupleValue] = CassandraValue.fromAnyRef
  implicit final def javaListsAllowed[T: IsCassandraValue](list: java.util.List[T]): CassandraValue = CassandraValue.fromAnyRef {
    list.asScala.map(implicitly[IsCassandraValue[T]].apply(_).unbox).asJava
  }
  implicit final def javaSetsAllowed[T: IsCassandraValue](set: java.util.Set[T]): CassandraValue = CassandraValue.fromAnyRef {
    set.asScala.map(implicitly[IsCassandraValue[T]].apply(_).unbox).asJava
  }
  implicit final def javaMapsAllowed[K: IsCassandraValue, V: IsCassandraValue](map: java.util.Map[K, V]): CassandraValue = CassandraValue.fromAnyRef {
    map.asScala.map {
      case (k, v) =>
        implicitly[IsCassandraValue[K]].apply(k).unbox -> implicitly[IsCassandraValue[V]].apply(v).unbox
    }.asJava
  }

  implicit final def scalaListsAllowed[T: IsCassandraValue](list: List[T]): CassandraValue = javaListsAllowed(list.asJava)
  implicit final def scalaSetsAllowed[T: IsCassandraValue](set: Set[T]): CassandraValue = javaSetsAllowed(set.asJava)
  implicit final def scalaMapsAllowed[K: IsCassandraValue, V: IsCassandraValue](map: Map[K, V]): CassandraValue = javaMapsAllowed(map.asJava)
  implicit final val zonedDateTimeAllowed: IsCassandraValue[ZonedDateTime] = z => dateAllowed(Date.from(z.toInstant))
  implicit final def scalaOptionAllowed[T: IsCassandraValue](option: Option[T]): CassandraValue = option match {
    case Some(z) =>
      implicitly[IsCassandraValue[T]].apply(z)
    case None =>
      CassandraValue.fromAnyRef(null)
  }

  implicit final class SafeOps(query: CPreparedStatement) {
    def execute(args: CassandraValue*): ResultSet =
      facade.execute(this.bind(args: _*))

    def bind(args: CassandraValue*): CBoundStatement =
      facade.bind(query, args.map(_.unbox): _*)
  }

  def cassandraValue[T: IsCassandraValue](value: T): CassandraValue = value
}

object WithSafeCassandraFacade {
  final class CassandraValue private (val unbox: AnyRef) extends AnyVal

  private object CassandraValue {
    def fromAnyRef[T <: AnyRef](t: T): CassandraValue = new CassandraValue(t)
  }
}
