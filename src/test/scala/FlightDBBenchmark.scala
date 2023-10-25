package gpt

import org.scalameter.{Bench, Executor}
import org.scalameter.api.Gen

import java.security.MessageDigest
import java.time.LocalDate
import scala.io.Source

object FlightDBBenchmark extends Bench.Group {
  private val flights = parseFlightsFile().toArray

  class MemoryBenchmark extends Bench.LocalTime {
    override def measurer = new Executor.Measurer.MemoryFootprint

    performance of "Memory footprint of Set[flight]" in {
      using(Gen.unit("nothing")) in { _ =>
        flights.map(flightToSetItem).toSet
      }
    }

    performance of "Memory footprint of Map[flight, schedule]" in {
      using(Gen.unit("nothing")) in { _ =>
        val db = new FlightDB()
        flights.foreach(db.addFlight)
        db
      }
    }
  }

  class TimeBenchmark extends Bench.LocalTime {
    private val lookupCount = 1_000_000

    performance of "Time of Set[flight]" in {
      using(Gen.unit("nothing")) in { _ =>
        val flightSet = flights.map(flightToSetItem).toSet
        val flight = flightToSetItem(randomFlight())
        (1 to lookupCount).foreach { _ =>
          flightSet.contains(flight)
        }
      }
    }

    performance of "Time of Map[flight, schedule]" in {
      using(Gen.unit("nothing")) in { _ =>
        val db = new FlightDB()
        flights.foreach(db.addFlight)
        (1 to lookupCount).foreach { _ =>
          db.flightExists(randomFlight())
        }
      }
    }
  }

  include(new MemoryBenchmark)
  include(new TimeBenchmark)

  private val sha1 = MessageDigest.getInstance("SHA-1")

  private def flightToSetItem(f: Flight): String = {
    sha1.digest("%s-%s".format(f.id, f.date).getBytes).map("%02x".format(_)).mkString("").take(8)
  }

  private def randomFlight(): Flight = flights(scala.util.Random.nextInt(flights.length))

  // Forgive me for not being functional
  private def parseFlightsFile(): Seq[Flight] = {
    var flights = Vector.empty[Flight]

    val source = Source.fromFile("flights.csv")
    for (line <- source.getLines()) {
      val cols = line.split(",").map(_.trim)
      if (cols.length == 4) {
        flights :+= Flight(cols(0), cols(1), LocalDate.parse(cols(2)), cols(3))
      }
    }
    source.close()

    flights
  }
}
