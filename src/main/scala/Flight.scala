package gpt

import io.circe.{Decoder, Encoder, HCursor, Json}

import java.time.LocalDate

case class Flight(
                   origArp: String,
                   destArp: String,
                   date: LocalDate,
                   flightNum: String
                 ) {
  def id: String = s"$origArp-$destArp-$flightNum"
}

object Flight {
  implicit val flightEncoder: Encoder[Flight] = (flight: Flight) =>
    Json.obj(
      ("origArp", Json.fromString(flight.origArp)),
      ("destArp", Json.fromString(flight.destArp)),
      ("date", Json.fromString(flight.date.toString)),
      ("flightNum", Json.fromString(flight.flightNum))
    )

  implicit val flightDecoder: Decoder[Flight] = (c: HCursor) =>
    for {
      origArp <- c.downField("origArp").as[String]
      destArp <- c.downField("destArp").as[String]
      date <- c.downField("date").as[String]
      flightNum <- c.downField("flightNum").as[String]
    } yield Flight(origArp, destArp, LocalDate.parse(date), flightNum) // FIXME: Handle parsing exceptions
}