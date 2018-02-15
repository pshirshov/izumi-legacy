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

  implicit final val intAllowed: IsCassandraValue[Int] = CassandraValue.apply(_.underlying())
  implicit final val longAllowed: IsCassandraValue[Long] = CassandraValue.apply(_.underlying())
  implicit final val byteAllowed: IsCassandraValue[Byte] = CassandraValue.apply(_.underlying())
  implicit final val shortAllowed: IsCassandraValue[Short] = CassandraValue.apply(_.underlying())
  implicit final val floatAllowed: IsCassandraValue[Float] = CassandraValue.apply(_.underlying())
  implicit final val doubleAllowed: IsCassandraValue[Double] = CassandraValue.apply(_.underlying())
  implicit final val bigIntAllowed: IsCassandraValue[BigInt] = CassandraValue.apply(_.underlying())
  implicit final val bigDecimalAllowed: IsCassandraValue[BigDecimal] = CassandraValue.apply(_.underlying())
  implicit final val booleanAllowed: IsCassandraValue[Boolean] = CassandraValue.apply(Boolean.box)
  implicit final val byteBufferAllowed: IsCassandraValue[ByteBuffer] = CassandraValue.make
  implicit final val uuidAllowed: IsCassandraValue[UUID] = CassandraValue.make
  implicit final val stringAllowed: IsCassandraValue[String] = CassandraValue.make
  implicit final val dateAllowed: IsCassandraValue[Date] = CassandraValue.make
  implicit final val cassandraLocalDateAllowed: IsCassandraValue[LocalDate] = CassandraValue.make
  implicit final val inetAddressAllowed: IsCassandraValue[InetAddress] = CassandraValue.make
  implicit final def javaListsAllowed[T: IsCassandraValue](list: java.util.List[T]): CassandraValue = CassandraValue.make(list)
  implicit final def javaSetsAllowed[T: IsCassandraValue](set: java.util.Set[T]): CassandraValue = CassandraValue.make(set)
  implicit final def javaMapsAllowed[K: IsCassandraValue, V: IsCassandraValue](map: java.util.Map[K, V]): CassandraValue = CassandraValue.make(map)
  implicit final val udtValueAllowed: IsCassandraValue[UDTValue] = CassandraValue.make
  implicit final val tupleValueAllowed: IsCassandraValue[TupleValue] = CassandraValue.make
  implicit final val zonedDateTimeAllowed: IsCassandraValue[ZonedDateTime] = CassandraValue.apply(z => Date.from(z.toInstant))
  implicit final def scalaListsAllowed[T: IsCassandraValue](list: List[T]): CassandraValue = CassandraValue.make(list.asJava)
  implicit final def scalaSetsAllowed[T: IsCassandraValue](set: Set[T]): CassandraValue = CassandraValue.make(set.asJava)
  implicit final def scalaMapsAllowed[K: IsCassandraValue, V: IsCassandraValue](map: Map[K, V]): CassandraValue = CassandraValue.make(map.asJava)

  implicit final class SafeOps(query: CPreparedStatement) {
    def execute(args: CassandraValue*): ResultSet =
      facade.execute(bind(args: _*))

    def bind(args: CassandraValue*): CBoundStatement =
      facade.bind(query, args.map(_.value): _*)
  }

  def cassandraValue[T: IsCassandraValue](value: T): CassandraValue = value
}

object WithSafeCassandraFacade {
  final class CassandraValue private (val value: AnyRef) extends AnyVal

  private object CassandraValue {
    def make[T <: AnyRef](t: T): CassandraValue = new CassandraValue(t)

    def apply[T, R <: AnyRef](converter: T => R)(t: T): CassandraValue = new CassandraValue(converter(t))
  }
}
