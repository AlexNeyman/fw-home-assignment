package gpt

import org.scalameter.{Bench, Executor}
import org.scalameter.api._

import java.time.LocalDate
import scala.io.Source
import scala.util.Random

object FlightDBBenchmark extends Bench.Group {
  // This file is generated by the 'generate.flights.sc' script
  // private val csvPath = "flights.generated.csv"

  private val csvPath = "flights.csv"

  class MemoryBenchmark extends Bench.LocalTime {
    override def measurer = new Executor.Measurer.MemoryFootprint

    performance of "Memory footprint of Map[flight, schedule]" in {
      using(Gen.unit("nothing")) config(
        exec.benchRuns := 1,
        exec.independentSamples := 1,
        exec.minWarmupRuns := 1,
        exec.maxWarmupRuns := 1,
      ) in { _ =>
        newPopulatedDB(flights)
      }
    }
  }

  class TimeBenchmark extends Bench.LocalTime {
    private val lookupCount = 1_000_000

    performance of "Time of Map[flight, schedule]" in {
      using(Gen.unit("nothing")) config(
        exec.benchRuns := 1,
        exec.independentSamples := 1,
        exec.minWarmupRuns := 1,
        exec.maxWarmupRuns := 1,
      ) in { _ =>
        val db = newPopulatedDB(flights)
        val flightsToLookup = flights.take(1000).toArray

        (1 to lookupCount).foreach { _ =>
          val randomFlight = flightsToLookup(Random.nextInt(flightsToLookup.length))
          db.flightExists(randomFlight)
        }
      }
    }
  }

  include(new MemoryBenchmark)
  include(new TimeBenchmark)

  private def newPopulatedDB(flights: Iterator[Flight]): FlightDB = {
    val db = new FlightDB()
    flights.zipWithIndex.foreach {
      case (flight, i) =>
        if ((i + 1) % 1000000 == 0) {
          println(s"Adding flight ${i + 1}")
        }
        db.addFlight(flight)
    }
    db
  }

  private def flights: Iterator[Flight] = {
    Source.fromFile(csvPath).getLines().map { line =>
      val cols = line.split(",").map(_.trim)
      if (cols.length == 4) {
        Some(Flight(cols(0), cols(1), LocalDate.parse(cols(2)), cols(3)))
      } else {
        None
      }
    }.filter(_.isDefined).map(_.get)
  }
}
