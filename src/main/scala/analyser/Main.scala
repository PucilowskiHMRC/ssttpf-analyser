package analyser

import io.circe.Json
import analyser.model.{DayView, LogLine, RequestView, SessionView}
import cats.implicits.catsSyntaxOptionId
import com.github.tototoshi.csv.CSVWriter
import scopt.OParser
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._

import java.io.File
import java.nio.file.Files
import java.time.{LocalDate, ZoneId}
import scala.jdk.CollectionConverters.ListHasAsScala

object Main extends App {

  OParser.parse(Config.parser, args, Config()) match {
    case Some(config) =>
      val a = Analyser(config)

      a()

      // do something
    case _ =>
      // arguments are bad, error message will have been displayed
  }

  def load(file: File) = {
    val input = Files.readAllLines(file.toPath).asScala.mkString("\n")

    io.circe.parser.parse(input).flatMap { json =>
      val c = json.hcursor

      def extractResponses(payload: Json): List[Json] = {
        val responsesCursor = payload.hcursor.downField("responses")

        if (responsesCursor.succeeded) {
          responsesCursor.as[List[Json]].getOrElse(throw new Exception("Expected responses"))
        } else {
          List(payload)
        }
      }

      def extractHits(response: Json) = {
        //        val lines = response.hcursor.downField("hits").downField("hits").as[List[Json]]

        response.hcursor.downField("hits").downField("hits").as[List[LogLine]].getOrElse(throw new Exception("Expected log lines"))
          .map(_.copy(sourceFile = file.toString.some))
      }

      val responses = extractResponses(json)

      Right(responses.flatMap(extractHits))
    }
  }



}
