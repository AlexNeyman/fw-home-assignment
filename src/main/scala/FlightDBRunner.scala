package gpt

import cats.effect.{IO, Ref}
import cats.effect.kernel.Resource
import fs2.io.file.{Files, Path, Watcher}

import java.nio.file.Paths
import java.time.LocalDate
import scala.util.{Try, Using}

class FlightDBRunner(path: String) {
  val dbRef: Ref[IO, FlightDB] = Ref.unsafe(new FlightDB())

  private def updateDB(): IO[Unit] = {
    val db = new FlightDB()
    for {
      _ <- IO.fromTry(populateDB(db))
      _ <- dbRef.set(db)
    } yield ()
  }

  private def populateDB(db: FlightDB): Try[Unit] = {
    Using(scala.io.Source.fromFile(path)) { source =>
      source.getLines().foreach { line =>
        val cols = line.split(",").map(_.trim)
        if (cols.length == 4) {
          // FIXME: Handle date parsing exceptions
          val flight = Flight(cols(0), cols(1), LocalDate.parse(cols(2)), cols(3))

          // FIXME: Handle addFlight exceptions
          db.addFlight(flight)
        }
      }
    }
  }

  private def watchForFileChanges(): Resource[IO, Unit] = {
    val fs2Path = Path.fromNioPath(Paths.get(path).toAbsolutePath)
    Files[IO].watch(fs2Path).evalMap {
      case _: Watcher.Event.Modified =>
        updateDB()
      case _ =>
        IO.unit
    }.compile.resource.lastOrError
  }

  def start: Resource[IO, Unit] = {
    for {
      _ <- Resource.eval(updateDB())
      _ <- watchForFileChanges().start
    } yield ()
  }
}
