package org.bitbucket.pshirshov.izumitk.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.ByteBuffer

/**
  * Created by pshir on 9/8/2016.
  */
object SerializationUtils {
  def toByteBuffer(o: Serializable): ByteBuffer = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(o)
    ByteBuffer.wrap(byteArrayOutputStream.toByteArray)
  }

  def toByteArray(bb: ByteBuffer): Array[Byte] = {
    val b = new Array[Byte](bb.remaining())
    bb.get(b)
    b
  }

  def readObject[T](bytes: Array[Byte]): T = {
    val byteArrayInputStream = new ByteArrayInputStream(bytes)
    val objectInputStream = new ObjectInputStream(byteArrayInputStream)
    objectInputStream.readObject().asInstanceOf[T]
  }

  def readObject[T](bytes: ByteBuffer): T = {
    readObject[T](toByteArray(bytes))
  }
}
