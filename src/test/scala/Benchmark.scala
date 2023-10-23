package gpt

import org.scalameter.api._

import java.time.LocalDate
import scala.util.Random

object Benchmark extends Bench.OfflineReport {
  override def measurer = new Executor.Measurer.MemoryFootprint

  private val today: LocalDate = LocalDate.now()

  private val flightCount = 1_000_000
  private val checks = 100_000

  performance of "Set" in {
    measure method "contains" in {
      using(Gen.unit("test")) in {
        _ =>
          val flights: Array[String] = (1 to flightCount).map(_ => randomFlight()).toArray
          val flightSet: Set[String] = flights.toSet

          (1 to checks).foreach(_ => flightSet.contains(flights(Random.nextInt(flightCount))))
      }
    }
  }

  private def randomFlight(): String = {
    val from = randomAirport()
    val to = randomAirport()
    val date = randomDate().toString
    val flightNum = randomFlightNumber()
    s"$from-$to-$date-$flightNum"
  }

  private def randomAirport(): String = {
    val chars = ('A' to 'Z').toList
    Random.shuffle(chars).take(3).mkString
  }

  private def randomDate(): LocalDate = today.plusDays(Random.nextInt(365))

  private def randomFlightNumber(): String = Random.nextInt(100_000).toString
}