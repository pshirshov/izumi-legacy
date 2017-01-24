package org.bitbucket.pshirshov.izumitk.akka.http

import java.security.{PrivateKey, PublicKey}

import org.bitbucket.pshirshov.izumitk.akka.http.modules.SecurityKeys
import org.bitbucket.pshirshov.izumitk.test.IzumiTestBase

import scala.language.postfixOps

class JwtSupportTest extends IzumiTestBase {
  "JWT Module" must {
    "support public key transformations" in {
      td =>
        val privateKey = SecurityKeys.readPemKey(
          s"""
             |-----BEGIN RSA PRIVATE KEY-----
             |MIIEpAIBAAKCAQEAyLEeIW/1pFaZFDf2QbLpzA2bo7mIdT2hCVJzd6gbTjOeBIq2
             |OQPNcRn4b00Ut9gqXkI8q2uxDtnltBj6djxSxnnUwKu0/lTPvtDIUMaPvwFjbYPU
             |fHT7njJy1lIaQbdmsLmSeVgkfs26TFxBGdletKNAiSpnKB387TBSgFo4tr0JkZu0
             |t/t5p6XutX8vonkLZFblAQ39LHQ3LsIwV4h7c32XkbPRVWX61MA1ILh0l8gCLsUA
             |ui+wXJ0MMkhFe4k5+y8NP9CUGwuS00YnmYQKZcuRIIH9Q7Vv7c054Vq+U7Oa1i10
             |Z+IQHhwFx58NqEmXMQkhXVm1CydBUzDEMevpVQIDAQABAoIBAEff3umcvj2X4gTy
             |sdf/qmmw6e+t76gFZVoAVGgjJXxRUiNQRkiPCxGZenbqBSR+X1YYpL/DtsqZ0QE5
             |/OFBY2e/lClYlV6Mo0Y0YblEE5EVfzVmhD/6aW7TpHsLNmoYs8dQ/ptErPVY2oKk
             |e5A1YAyTdvZo4D7m1oFfz/w105/l20OOCnoQ63w2omT5VDJBAc98g/8NpGvtwVav
             |OjfjXnDj/FbSas5aUArjU8HhQ1m22KKiNapHRZ3rS1UncIz5manZfEmNH5mPpTRb
             |eRg1mY5GdkhzxtHn3gZ5yshfpJdp/oYWvcS92+sItNj58/Q9V98pI0SiS7Q4dTS4
             |HPIht8ECgYEA6yDJMj/NROweDag6aSYoHfMQKAhYPwTctXUQI7C3sWNatGtkSSnn
             |zV5+DUtxXkRL7x73H7VHip6SVWvXel9ntWIcBDK46IvAyCmxUDUcNWFosetP3tNz
             |MBsZntFXd+bt8TLh7QHNbUH35SDv3xleNXzcJ7F3MHmq6st40tcmnRECgYEA2oHH
             |jDVTohBJ0nZiF3mnD1utP58GZTvjUaRmEmBnbGEbE0jguyFZFtamsKNpCdZ3MfeX
             |miPAsprsCl99vkof2mYlyHsYxSazluLx+546Mw8vHJrLZ2vVfBS2lJ1aW4pVto0K
             |rU51vt7W2imBho3yC28qoCYMgaO7QZvDIxldWAUCgYEA4ELJI7BO7Kn4kDYgYCIQ
             |qWAnzMcIxDKzAC/DUQdAbWTCIss7P71EZlD2EWAfpTWm50+1jPyuW7ya/fZ58zIE
             |DLCi4C4wxzxkh4WcpU/Cs/dQkEJMT8+GUh/G8//fWXFdCWzqp8/MS6Vb96LOpw/f
             |F00b/o/5irbBGmkvuImVPsECgYEApIBN9Z10EC+JFDxxztCUV/ih7qLPUsmKcr18
             |5trTIgHdO2CDZ/5MLuh858CuZGykoeaPqMi/2vbNO2X2qZrubiK20T7zQAHJ9I94
             |tADEOgp1YmibN4o0V8zURABOtVno+50la6IE/SSQxf+3dtBIaBlSEu1yPutAH72f
             |ZD5FSu0CgYBRX6qR63z/FLQnex2b829cU0uAOToDtKKSbtlBHMYgozFFxWdvJU8A
             |WoXPkUv5ih8HvQ/y+Ia4QWe0saIqPLzEc7s2XR5gx+yehBsNMSjiqhj2Y0JcRyjw
             |X1jmrDMVoNlVCAGoR0gzc0+kG6p2hCbYZJdBUz/d4YAwY1Aq7OMkIw==
             |-----END RSA PRIVATE KEY-----
                   """.stripMargin)
        assert(privateKey.isInstanceOf[PrivateKey])

        val publickKeyString =
          s"""
             |-----BEGIN PUBLIC KEY-----
             |MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyLEeIW/1pFaZFDf2QbLp
             |zA2bo7mIdT2hCVJzd6gbTjOeBIq2OQPNcRn4b00Ut9gqXkI8q2uxDtnltBj6djxS
             |xnnUwKu0/lTPvtDIUMaPvwFjbYPUfHT7njJy1lIaQbdmsLmSeVgkfs26TFxBGdle
             |tKNAiSpnKB387TBSgFo4tr0JkZu0t/t5p6XutX8vonkLZFblAQ39LHQ3LsIwV4h7
             |c32XkbPRVWX61MA1ILh0l8gCLsUAui+wXJ0MMkhFe4k5+y8NP9CUGwuS00YnmYQK
             |ZcuRIIH9Q7Vv7c054Vq+U7Oa1i10Z+IQHhwFx58NqEmXMQkhXVm1CydBUzDEMevp
             |VQIDAQAB
             |-----END PUBLIC KEY-----
             |
           """.stripMargin
        val publicKey = SecurityKeys.readPemKey(
          publickKeyString
        )

        assert(publicKey.isInstanceOf[PublicKey])

        assert(SecurityKeys.publicKeyFingerprint(publicKey) == "MD5:e1:e9:f4:9c:24:26:33:ed:ab:4a:57:59:5c:20:5b:39")
        assert(SecurityKeys.publicKeyFingerprint(privateKey) == SecurityKeys.publicKeyFingerprint(publicKey))
        assert(SecurityKeys.keyInfo(privateKey) == "RSA:JCERSAPrivateCrtKey")
        assert(SecurityKeys.keyInfo(publicKey) == "RSA:JCERSAPublicKey")
        assert(SecurityKeys.writePublicPemKey(publicKey).trim == publickKeyString.trim)
        assert(SecurityKeys.writePublicPemKey(privateKey).trim == SecurityKeys.writePublicPemKey(publicKey).trim )
    }
  }

}
