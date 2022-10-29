scalaVersion := "2.12.8"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "pbft1",
    libraryDependencies ++= {

      lazy val versions = new {
        val akka = "2.4.18"
        val akkaHttp = "10.0.7"
      }

      val appDeps = Seq(
        "com.typesafe.akka" %% "akka-http" % versions.akkaHttp,
        "com.typesafe.akka" %% "akka-remote" % versions.akka,
        "com.typesafe.akka" %% "akka-slf4j" % versions.akka,
        "de.heikoseeberger" %% "akka-http-json4s" % "1.16.1"
      )

      val testDeps = Seq(
        "org.scalacheck" %% "scalacheck" % "1.13.4",
        "org.scalatest" %% "scalatest" % "3.0.1",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0",
        "com.typesafe.akka" %% "akka-http-testkit" % versions.akkaHttp,
        "com.typesafe.akka" %% "akka-testkit" % versions.akka
      )

      appDeps ++ testDeps
    }
  )
