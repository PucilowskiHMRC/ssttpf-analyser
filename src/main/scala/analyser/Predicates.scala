package analyser

import analyser.model.LogLine

object Predicates {
  def expectedJourney(line: LogLine) = {
    val fp = "java.lang.RuntimeException: Expected eligible journey in progress"
    line.message.exists(_.contains(fp))
  }

  def submissionSucceeded(line: LogLine) = {
    val fp = "Arrangement submission Succeeded"
    line.message.exists(_.contains(fp))
  }

  def zonk(line: LogLine) = {
    val fp = "ZONK ERROR!"
    line.message.exists(_.contains(fp))
  }
}
