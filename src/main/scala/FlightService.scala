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
          dbRef.get.flatMap(db => Ok(db.flightExists(flight).asJson))
        }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // FIXME: Use args
    val filePath = "flights.csv"
    val host = "0.0.0.0"
    val port = 8082

    val dbService = new FlightDBService(filePath, new FlightDB())

    val service = for {
      _ <- dbService.start
      _ <- BlazeServerBuilder[IO]
        .bindHttp(port, host)
        .withHttpApp(routes(dbService.dbRef).orNotFound)
        .resource
    } yield ()

    service.useForever.as(ExitCode.Success)
  }
}
