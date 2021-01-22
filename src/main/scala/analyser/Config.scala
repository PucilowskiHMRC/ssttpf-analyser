package analyser

import java.io.File

case class Config(
                   in: List[File] = List.empty,
                   out: File = new File(".")
                 )

object Config {

  import scopt.OParser

  val builder = OParser.builder[Config]

  val parser = {
    import builder._
    OParser.sequence(
      programName("ssttp-analyser"),
      head("ssttp-analyser"),
      // option -f, --foo
      arg[File]("input-file")
        .unbounded()
        .action((x, c) => c.copy(in = c.in :+ x))
        .text("ES query response json"),
      opt[File]('o', "out-path")
        .action((x, c) => c.copy(out = x))
        .text("Output dir")
    )
  }
}