import sbt._

object Dependencies {

  object cats {
    val core = "org.typelevel" %% "cats-core" % "2.3.1"
  }

  object circe {
    val org = "io.circe"
    val v = "0.13.0"
  }

  object kantan {
    val v = "0.6.1"
    val core = "com.nrinaudo" %% "kantan.csv" % v
    val java8 = "com.nrinaudo" %% "kantan.csv-java8" % v
    val generic = "com.nrinaudo" %% "kantan.csv-generic" % v
  }

  val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.6"
}
