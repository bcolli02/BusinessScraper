lazy val root = (project in file(".")).
  settings(
    name := "BusinessScraper",
    version := "1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.1",           // useful tools
      "info.folone" %% "poi-scala" % "0.15",                    // writing excel
      "org.scalaz" %% "scalaz-core" % "7.1.3",                  // essentials
      "org.http4s" %% "http4s-dsl" % "0.6.5",                    // to use the core dsl
      "org.http4s" %% "http4s-blazeclient" % "0.6.5",           // to use the blaze client
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0" // output logging
    ),
    resolvers ++= Seq(
      "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
    )
  )
