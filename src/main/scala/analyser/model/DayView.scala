package analyser.model

import kantan.csv.HeaderEncoder
import kantan.csv.java8._
import kantan.csv.generic._

import java.time.LocalDate

case class DayView(
                    date: Option[LocalDate],
                    unexpectedJourney: Int,
                    unexpectedJourneyAndNoSuccess: Int,
                    unexpectedJourneyAndZonk: Int,
                    unexpectedJourneyNoZonk: Int
                  )

object DayView {

  implicit val headerEncoder: HeaderEncoder[DayView] = HeaderEncoder
    .caseEncoder[Option[LocalDate], Int, Int, Int, Int, DayView](
      "date",
      "expected-eligible-journey",
      "expected-eligible-journey AND !completed",
      "expected-eligible-journey AND !completed AND zonk",
      "expected-eligible-journey AND !completed AND !zonk"
    )(DayView.unapply)

//  implicit val personCodec: RowEncoder[DayView] = RowEncoder
//    .encoder[DayView, Option[LocalDate], Int, Int, Double](0, 1, 2, 3)((dv: DayView) => DayView.unapply(dv).get)

}