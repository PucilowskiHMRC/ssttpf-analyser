package analyser

import analyser.Main.load
import analyser.model.{DayView, LogLine, RequestView, SessionView}
import kantan.csv.ops._
import kantan.csv.{HeaderEncoder, rfc}

import java.nio.file.{Files, Path, Paths}
import java.time.{LocalDate, ZoneId}

case class Analyser(
                     config: Config
                   ) {

  def apply() = {
    val lines = config.in.flatMap(f => load(f).getOrElse(throw new Exception("Expected at least 1 input file")))

    val sessions = SessionView.fromLogs(lines)

    writeCsv("sessions.csv")(sessions)

    writeCsv("sessions-succeeded-then-ineligible.csv") {
      sessions.filter(s => s.unexpectedJourneyAndNoSuccess)
    }

    val requests = RequestView.fromLogs(lines)
    writeCsv("requests.csv")(requests)

    val sessionsPath = mkSessionsFolder()

    requests.groupBy(_.sessionId).foreach {
      case (sessionId, requests) =>
        writeCsv(s"sessions/${sessionId}-requests.csv")(requests)
    }



    val dailies = sessions.groupBy(_.day).map {
      case (maybeDate, sessions) =>
        val unexpectedJourneys = sessions.count(_.unexpectedJourney)
        val unexpectedJourneysAndNoComplete = sessions.count(s => s.unexpectedJourneyAndNoSuccess)
        val unexpectedJourneysAndNoCompleteAndZonk = sessions.count(s => s.unexpectedJourneyAndNoSuccess && s.zonks > 0)
        val unexpectedJourneysAndNoCompleteAndNoZonk = sessions.count(s => s.unexpectedJourneyAndNoSuccess && s.zonks == 0)

        DayView(
          maybeDate,
          unexpectedJourneys,
          unexpectedJourneysAndNoComplete,
          unexpectedJourneysAndNoCompleteAndZonk,
          unexpectedJourneysAndNoCompleteAndNoZonk,
//          unexpectedJourneysAndNoComplete.toDouble / unexpectedJourneys
        )
    }.toList.sortBy(_.date.getOrElse(LocalDate.MIN))

    writeCsv("dailies.csv")(dailies)

    println(s"Sessions: ${sessions.size}")
    println(s"Lines: ${lines.size}")
//    println(s"Succeededs: ${sessions.count(_.succeededs > 0)}")
    println(s"Bad journey: ${sessions.count(_.unexpectedJourney)}")
    println(s"Bad journey & incomplete application: ${sessions.count(s => s.unexpectedJourneyAndNoSuccess)}")
  }

  def outFile(filename: String): Path = config.out.toPath.resolve(filename)

  def writeCsv[A: HeaderEncoder](filename: String)(as: IterableOnce[A]): Unit = {
    val out = outFile(filename)
    println(s"Writing ${as.iterator.size} records to ${out}")

    out.asCsvWriter[A](rfc.withHeader).write(as).close()
  }

  def mkSessionsFolder(): Path = {
    val sessionsPath = outFile("sessions")

    if(!Files.exists(sessionsPath)) Files.createDirectory(sessionsPath)

    sessionsPath
  }
}
