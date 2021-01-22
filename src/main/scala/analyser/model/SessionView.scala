package analyser.model

import analyser.Predicates
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import kantan.csv.{CellEncoder, HeaderEncoder}

import java.time.{Instant, LocalDate, ZoneId}

case class SessionView(
                        sessionId: String,
                        lineCount: Int,
                        start: Option[Instant],
                        startMessage: Option[String],
                        end: Option[Instant],
                        endMessage: Option[String],
                        unexpectedJourney: Boolean,
                        unexpectedJourneyAndNoSuccess: Boolean,
                        zonks: Int,
                        files: List[String]
                      )  {

  def day: Option[LocalDate] = start.map(i => i.atZone(ZoneId.systemDefault()).toLocalDate)
}

object SessionView {

  def fromLogs(lines: List[LogLine]): List[SessionView] = {
    lines.groupBy(_.xSessionId).map {
      case (maybeXSID, lines) =>
        val sessionId = maybeXSID.getOrElse("undefined")

        println(s"X-Session-Id: $sessionId")
        //        println()
        //        println(lines.mkString("\n"))
        SessionView.fromLogs(
          sessionId,
          lines
        )
    }.toList
  }

  def fromLogs(xSessionId: String, lines: List[LogLine]): SessionView = {
    val files = lines.flatMap(_.sourceFile).distinct

    val start = lines.map(_.timestamp).collect {
      case Some(value) => value
    }.minOption

    val end = lines.map(_.timestamp).collect {
      case Some(value) => value
    }.maxOption

    val span = SpanView.fromLines(lines)

    val expectedEligible = lines.exists(Predicates.expectedJourney)
    val completedJourney = lines.exists(Predicates.submissionSucceeded)

    SessionView(
      xSessionId,
      lines.size,
      span.start,
      span.startMessage,
      span.end,
      span.endMessage,
      expectedEligible,
      expectedEligible && !completedJourney,
      lines.count(Predicates.zonk),
      files
    )
  }

  implicit val codec: Codec[SessionView] = deriveCodec

  //  implicit val headerEncoder: HeaderEncoder[SessionView] = HeaderEncoder.defaultHeaderEncoder[SessionView]
  implicit val sessionEncoder: HeaderEncoder[SessionView] = {
    import kantan.csv.java8._
    import kantan.csv.generic._

    implicit val filesEnc: CellEncoder[List[String]] = CellEncoder.from[List[String]](_.mkString(","))

    //    RowEncoder
    //      .encoder(0,1,2,3,4,5,6)((sv: SessionView) => SessionView.unapply(sv).get)
    HeaderEncoder.caseEncoder(
      "x-session-id",
      "Line count",
      "min(@timestamp)",
      "min.message",
      "max(@timestamp)",
      "max.message",
      "expected-eligible-journey",
      "expected-eligible-journey AND !completed",
      "Zonks",
      "Files")(SessionView.unapply)
  }
}
