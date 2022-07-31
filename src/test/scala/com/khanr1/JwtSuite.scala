package com.khanr1

import weaver.* 

import cats.*
import cats.effect._
import cats.syntax.all.*
import com.khanr1.auth.Jwt.*
import com.khanr1.auth.JwtAuthMiddleware
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalacheck.Gen
import pdi.jwt.*
import pdi.jwt.JwtAlgorithm
import scala.util.*
import weaver.*
import weaver.scalacheck.*
import cats.effect.unsafe.implicits.global

// Suites must be "objects" for them to be icked by the framework
object JwtSuite extends SimpleIOSuite with Checkers {
  def extractId(content: String): Int =
    Try(content.split(",").toList.head.drop(6).toInt).toOption.getOrElse(0)
  end extractId

  val authenticate: JwtToken => JwtClaim => IO[Option[User]] = _ =>
    claim =>
      if (extractId(claim.content) == 123) User(123, "admin").some.pure[IO]
      else none[User].pure[IO]

  
  val jwtAuth    = JwtAuth.hmac("53cr3t", JwtAlgorithm.HS256)
  val middleware = JwtAuthMiddleware[IO, User](jwtAuth, authenticate)
  
  
  
  
  case class User(id:Int,name:String)
  object User{
    given decoder:Decoder[User]=Decoder.forProduct2("id","name")(User.apply)
    given encoder:Encoder[User]=Encoder.forProduct2("id","name")(u=> (u.id,u.name))
    given show:Show[User]=Show.fromToString
  }

  val genUser:Gen[User]=for{
    id<-Gen.posNum[Int]
    name <- Gen.alphaStr
  } yield User(id,name)

  val genAdminUser:Gen[User]=for{
    name <- Gen.alphaStr
  } yield User(123,name)

  val openRoute: HttpRoutes[IO] = HttpRoutes.of{
    case GET -> Root => Ok("good")
  }
  val adminRoute:AuthedRoutes[User,IO]=AuthedRoutes.of{
    case GET -> Root / "admin" as user => Ok("admin")
  }

  val adminToken = jwtEncode(JwtClaim(User(123,"admin").asJson.noSpaces),jwtAuth.secret,jwtAuth.algo.head)
  val adminTokenString=adminToken.unsafeRunSync() //TODO how to only use 
  val userToken = jwtEncode(JwtClaim(User(666,"user").asJson.noSpaces),jwtAuth.secret,jwtAuth.algo.head)
  val rootReq: Request[IO]         = Request[IO](Method.GET, Uri.unsafeFromString("/"))
  val adminReqNoToken: Request[IO] = Request[IO](Method.GET, Uri.unsafeFromString("/admin"))
  val goodAdminReq    = adminReqNoToken.withHeaders(Header.ToRaw.keyValuesToRaw("Authorization" -> s"Bearer $adminTokenString"))

  test("encoding and decoding should return the initial toker -> decoding (encoding(token))==token"){
    forall(genUser){
      u => {
        val jwtClaim= JwtClaim(u.asJson.noSpaces)
        val token = jwtEncode[IO](jwtClaim,jwtAuth.secret,jwtAuth.algo.head)
        val decode = token.flatMap(x =>jwtDecode[IO](x,jwtAuth))
        decode.map(x=> expect(jwtClaim==x))
      } 
    }
  }

  test("Open routes works when combined with secured route"){
    forall(genUser){ u=>
      openRoute.run(rootReq).value.flatMap{ 
        case Some(resp) => IO.pure(resp).map(resp=>expect.same(Status.Ok,resp.status))
        case None => IO.pure(failure("route not found"))
      }
    }
 
  }

  test("Admin route gives 403 when there's no token"){
    forall(genUser){ u=>
      middleware(adminRoute).run(adminReqNoToken).value.flatMap{
        case Some(rest)=> IO.pure(rest).map(resp=>expect.same(Status.Forbidden,resp.status))
        case None => IO.pure(failure("route not found"))
      }
    }
  }

  test("Admin route gives 200 when there's a valid token"){
    forall(genUser){ u=>
      middleware(adminRoute).run(goodAdminReq).value.flatMap{
        case Some(rest)=> IO.pure(rest).map(resp=>expect.same(Status.Ok,resp.status))
        case None => IO.pure(failure("route not found"))
      }
    }
  }
  
  

  




}
