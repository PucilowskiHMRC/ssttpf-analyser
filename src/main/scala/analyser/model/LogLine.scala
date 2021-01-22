package analyser.model

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.{AutoDerivation, Configuration}

import java.time.Instant

case class LogLine(
                    timestamp: Option[Instant],

                    level: Option[String],
                    xSessionId: Option[String] = None,
                    xRequestId: Option[String] = None,
                    message: Option[String],
                    appHome: Option[String],
                    sourceFile: Option[String] = None
                  )

object LogLine extends AutoDerivation {

  implicit val cfg: Configuration = Configuration.default
    .withDefaults
    .withKebabCaseMemberNames

  implicit val decoder: Decoder[LogLine] = (cursor) => {
    val stockDecoder: Decoder[LogLine] = deriveConfiguredDecoder

    val _source = cursor.downField("_source")
    for {
      line <- _source.as[LogLine](stockDecoder)
      timestamp <- _source.downField("@timestamp").as[Option[Instant]]
      appHome <- _source.downField("application.home").as[Option[String]]
    } yield line.copy(timestamp = timestamp, appHome = appHome)
  }

}