package com.khanr1.auth

import cats.*
import cats.kernel.Eq
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.Encoder
import pdi.jwt.*
import pdi.jwt.algorithms.JwtHmacAlgorithm


object Jwt {

  opaque type JwtToken = String
  object JwtToken{
    def apply(str:String):JwtToken=str
    extension (token:JwtToken){
        def value:String=token
    }
    given decoder:Decoder[JwtToken]=Decoder.decodeString.map(apply(_))
    given encoder:Encoder[JwtToken]=Encoder.encodeString.contramap(_.value)
    given show:Show[JwtToken]= Show.show(_.value)
    given eq:Eq[JwtToken]= Eq.fromUniversalEquals

  }

  opaque type JwtSecret = String
  object JwtSecret{
    def apply(str:String):JwtSecret=str
    extension (secret:JwtSecret){
        def value:String=secret
    }
    given decoder:Decoder[JwtSecret]=Decoder.decodeString.map(apply(_))
    given encoder:Encoder[JwtSecret]=Encoder.encodeString.contramap(_.value)
    given show:Show[JwtSecret]= Show.show(_.value)
    given eq:Eq[JwtSecret]= Eq.fromUniversalEquals

  }

  enum JwtAuth:
    case JwtNoValidation extends JwtAuth
    case JwtSymmetric(secret:JwtSecret,algo:Seq[JwtHmacAlgorithm]) extends JwtAuth

  object JwtAuth:
    def noValidation=JwtNoValidation
    def hmac(secret:String,algo:JwtHmacAlgorithm):JwtSymmetric= JwtSymmetric(JwtToken(secret),Seq(algo))
    def hmac(secret:String,algo:Seq[JwtHmacAlgorithm]):JwtSymmetric = JwtSymmetric(JwtToken(secret),algo)  
  end JwtAuth

  def jwtDecode[F[_]:ApplicativeThrow](token:JwtToken,auth:JwtAuth):F[JwtClaim]=
    (auth match {
        case JwtAuth.JwtNoValidation => JwtCirce.decode(token,JwtOptions.DEFAULT.copy(signature = false))
        case JwtAuth.JwtSymmetric(secret,algo) => JwtCirce.decode(token.show,secret.show,algo)
    }).liftTo[F]

  def jwtEncode[F[_]:Applicative](claim:JwtClaim,secret:JwtSecret,algo:JwtHmacAlgorithm):F[JwtToken]=
    JwtToken(JwtCirce.encode(claim,secret.show,algo)).pure[F]

}
