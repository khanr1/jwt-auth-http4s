import sbt.*

object Dependencies {

  object Version{
    val cats = "2.8.0"
    val catsEffect = "3.3.14"
    val jwt = "9.0.5"
    val munit = "1.0.0-M1"
    val http4s = "0.23.14"
    val circe = "0.14.2"
    val weaver = "0.7.13"
  }

  object Library{
    val jwtCirce         = "com.github.jwt-scala" %% "jwt-circe"   % Version.jwt
    val cats             = "org.typelevel"        %% "cats-core"   % Version.cats
    val catsEffect       = "org.typelevel"        %% "cats-effect" % Version.catsEffect
    val munit            = "org.scalameta"        %% "munit"       % Version.munit
    val circe            ="io.circe"              %% "circe-core"  % Version.circe
    val weaverCats       ="com.disneystreaming"   %% "weaver-cats" % Version.weaver
    val weaverScalaCheck ="com.disneystreaming"   %% "weaver-scalacheck" % Version.weaver
    val weaverDiscipline ="com.disneystreaming"   %% "weaver-discipline" % Version.weaver
    val http4s           = "org.http4s"           %% "http4s-core"       % Version.http4s
    val http4sServer     = "org.http4s"           %% "http4s-server"     % Version.http4s
    val http4sCLient     = "org.http4s"           %% "http4s-client"     % Version.http4s
    val http4sDsl        = "org.http4s"           %% "http4s-dsl"     % Version.http4s
    val http4sCirce      = "org.http4s"           %% "http4s-circe"     % Version.http4s



     

  }
  
}
