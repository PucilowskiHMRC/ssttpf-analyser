package analyser.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import kantan.csv.{CellEncoder, HeaderEncoder}

import java.time.{Instant, LocalDate, ZoneId}

case class SpanView(
                     start: Option[Instant],
                     startMessage: Option[String],
                     end: Option[Instant],
                     endMessage: Option[String]
                   ) {

  def day: Option[LocalDate] = start.map(i => i.atZone(ZoneId.systemDefault()).toLocalDate)

//  def withSpan(
//                start: Option[Instant],
//                startMessage: Option[String],
//                end: Option[Instant],
//                endMessage: Option[String]
//              ): E
}

object SpanView {
  def fromLines(lines: List[LogLine]): SpanView = {
    val start = lines.minBy(_.timestamp.getOrElse(Instant.MIN))
    val end = lines.maxBy(_.timestamp.getOrElse(Instant.MIN))

    SpanView(
      start = start.timestamp,
      startMessage = start.message,
      end = end.timestamp,
      endMessage = end.message
    )
  }

  //
  //  implicit val encoder: HeaderEncoder[SpanView] = {
  //    import kantan.csv.java8._
  //    import kantan.csv.generic._
  //
  //    implicit val filesEnc: CellEncoder[List[String]] = CellEncoder.from[List[String]](_.mkString(","))
  //
  //    //    RowEncoder
  //    //      .encoder(0,1,2,3,4,5,6)((sv: SessionView) => SessionView.unapply(sv).get)
  //    HeaderEncoder.caseEncoder(
  //      "Started",
  //      "Start message",
  //      "Ended",
  //      "End message")(SpanView.unapply)
  //  }
  //
  //  implicit val codec: Codec[SpanView] = deriveCodec
}
