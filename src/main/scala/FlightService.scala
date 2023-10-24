package gpt

import java.nio.file.{Path, Paths}
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.blaze.server.BlazeServerBuilder
import io.circe.syntax._
import org.http4s.implicits._
import org.http4s.EntityDecoder
import org.http4s.circe.CirceInstances

import java.time.LocalDate


object FlightService extends IOApp with CirceInstances {
  private def readFlightsFile(filePath: Path): IO[Seq[Flight]] = {
    Stream
      .eval(IO(scala.io.Source.fromFile(filePath.toFile)))
      .flatMap(source => Stream.fromIterator[IO](source.getLines(), 64))
      .map(line => line.split(",").map(_.trim))
      .filter(cols => cols.length == 4)
      // FIXME: Handle parsing exceptions
      .map(cols => Flight(cols(0), cols(1), LocalDate.parse(cols(2)), cols(3)))
      .compile
      .toList
  }

  private def flightExists(flight: Flight, flights: IO[Seq[Flight]]): IO[Boolean] =
    flights.map(_.contains(flight))

  // Entity decoder for Flight
  implicit val flightEntityDecoder: EntityDecoder[IO, Flight] =
    jsonOf[IO, Flight]

  private def app(flights: IO[Seq[Flight]]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req@POST -> Root / "fareplace" / "flightExists" =>
        req.decode[Flight] { flight =>
          flightExists(flight, flights).flatMap { exists =>
            Ok(exists.asJson)
          }
        }
    }

  override def run(args: List[String]): IO[ExitCode] = {
    val flightsFilePath = Paths.get("flights.csv")
    val flights = readFlightsFile(flightsFilePath)
    BlazeServerBuilder[IO]
      .bindHttp(8082, "0.0.0.0")
      .withHttpApp(app(flights).orNotFound)
      .resource
      .useForever
      .as(ExitCode.Success)
  }
}
