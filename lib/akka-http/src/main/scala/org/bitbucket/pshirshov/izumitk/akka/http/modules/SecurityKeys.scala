package org.bitbucket.pshirshov.izumitk.akka.http.modules

import java.io.{ByteArrayOutputStream, StringReader}
import java.math.BigInteger
import java.security.interfaces.RSAPublicKey
import java.security.{Key, KeyPair, PublicKey, Security}

import com.google.common.hash.{HashCode, Hashing}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader
import org.bouncycastle.util.encoders.Base64


object SecurityKeys {
  def initBouncyCastle(): Unit = {
    if (Option(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)).isEmpty) {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
    }
  }

  def readPemKey(keystr: String): Key = {
    SecurityKeys.initBouncyCastle()
    val reader = new PEMReader(new StringReader(keystr))
    reader.readObject() match {
      case k: Key =>
        k
      case k: KeyPair =>
        k.getPrivate
      case k =>
        throw new IllegalArgumentException(s"Unsupported key: $k")
    }

  }

  def publicKeyFingerprint(key: Key): String = {
    key match {
      case k: RSAPublicKey =>
        fingerprint(k)
      case k =>
        throw new IllegalArgumentException(s"Unsupported key: $k")
    }
  }

  def keyInfo(key: Key): String = {
    s"${key.getAlgorithm}@${key.getClass}"
  }

  def fingerprint(key: RSAPublicKey): String = {
    fingerprint(key.getPublicExponent, key.getModulus)
  }

  def publicKey(key: PublicKey): String = {
    key match {
      case k: RSAPublicKey =>
        new String(Base64.encode(k.getEncoded))

      case k =>
        throw new IllegalArgumentException(s"Unsupported key: $k")
    }
  }

  // ssh-keygen -E md5 -lf /dev/stdin <<< $( ssh-keygen -f private_key.pem -y )
  def fingerprint(publicExponent: BigInteger, modulus: BigInteger): String = {
    val blob = keyBlob(publicExponent, modulus)
    s"MD5:${hexColonDelimited(Hashing.md5().hashBytes(blob))}"
  }

  private def keyBlob(publicExponent: BigInteger, modulus: BigInteger): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    writeLengthFirst("ssh-rsa".getBytes, out)
    writeLengthFirst(publicExponent.toByteArray, out)
    writeLengthFirst(modulus.toByteArray, out)
    out.toByteArray
  }

  private def writeLengthFirst(array: Array[Byte], out: ByteArrayOutputStream): Unit = {
    out.write((array.length >>> 24) & 0xFF)
    out.write((array.length >>> 16) & 0xFF)
    out.write((array.length >>> 8) & 0xFF)
    out.write((array.length >>> 0) & 0xFF)
    if (array.length == 1 && array(0) == 0x00.toByte) out.write(Array.ofDim[Byte](0)) else out.write(array)
  }

  private def hexColonDelimited(hc: HashCode): String = {
    import com.google.common.base.Splitter.fixedLength
    import com.google.common.io.BaseEncoding.base16

    import scala.collection.JavaConverters._

    fixedLength(2).split(base16().lowerCase().encode(hc.asBytes())).asScala.mkString(":")
  }

}
