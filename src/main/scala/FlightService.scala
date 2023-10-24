package gpt

import cats.effect.{ExitCode, IO, IOApp, Ref}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.blaze.server.BlazeServerBuilder
import io.circe.syntax._
import org.http4s.implicits._
import org.http4s.EntityDecoder
import org.http4s.circe.CirceInstances

import java.time.LocalDate


object FlightService extends IOApp with CirceInstances {
  implicit val flightEntityDecoder: EntityDecoder[IO, Flight] =
    jsonOf[IO, Flight]

  private def routes(dbRef: Ref[IO, FlightDB]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@POST -> Root / "fareplace" / "flightExists" =>
        req.decode[Flight] { flight =>
          dbRef
            .get
            .map(db => db.flightExists(flight))
            .flatMap(exists => Ok(exists.asJson))
        }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // FIXME: Use args
    val filePath = "flights.csv"
    val host = "0.0.0.0"
    val port = 8082

    // FIXME: Dates should be parsed from the file
    val db = new FlightDB(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 26))
    val dbService = new FlightDBService(filePath, db)

    val service = for {
      _ <- dbService.start
      exitCode <- BlazeServerBuilder[IO]
        .bindHttp(port, host)
        .withHttpApp(routes(dbService.dbRef).orNotFound)
        .resource
    } yield exitCode

    service.useForever.as(ExitCode.Success)
  }
}
