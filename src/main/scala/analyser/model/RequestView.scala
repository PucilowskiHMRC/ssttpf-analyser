package analyser.model

import kantan.csv.{CellEncoder, HeaderEncoder, rfc}

import java.time.Instant
import scala.util.Try

case class RequestView(
                        sessionId: String,
                        requestId: String,
                        method: Option[String],
                        uri: Option[String],
                        lineCount: Int,
                        start: Option[Instant],
                        startMessage: Option[String],
                        end: Option[Instant],
                        endMessage: Option[String]
                      )

object RequestView {

  val verbs = List("GET", "POST", "PUT", "DELETE", "PATCH")

  def fromLogs(lines: List[LogLine]): List[RequestView] = {
    lines.groupBy(_.xRequestId).map {
      case (maybeReqId, lines) =>
        val sessionId = {
          val ids = lines.map(_.xSessionId).distinct
          assert(ids.size == 1, s"number of unique session ids should be 1 not ${ids.size}")
          ids.headOption.flatten.getOrElse("unknown")
        }

        RequestView.fromLogs(sessionId, maybeReqId.getOrElse("unknown"), lines)
    }.toList
  }

  def fromLogs(sessionId: String, requestId: String, lines: List[LogLine]): RequestView = {
    assert(lines.forall(_.xRequestId.forall(_ == requestId)))

    val requestLine = lines.find { line =>
      verbs.exists { verb =>
        line.message.forall(_.startsWith(verb))
      }
    }

    val maybeR = requestLine.flatMap(_.message).map { line =>
      //      Try {
      line.split("\\s+")
      //      }.toOption
    }

    val span = SpanView.fromLines(lines)

    RequestView(
      sessionId,
      requestId,
      maybeR.flatMap(_.headOption),
      maybeR.flatMap(_.drop(1).headOption),
      lines.size,
      span.start,
      span.startMessage,
      span.end,
      span.endMessage,
    )
  }

  implicit val encoder: HeaderEncoder[RequestView] = {
    import kantan.csv.java8._
    import kantan.csv.generic._

    implicit val filesEnc: CellEncoder[List[String]] = CellEncoder.from[List[String]](_.mkString(","))

    HeaderEncoder.caseEncoder(
      "X-Session-Id",
      "X-Request-Id",
      "Method",
      "URI",
      "Line count",
      "Started",
      "Start message",
      "Ended",
      "End message"
    )(RequestView.unapply)
  }
}
