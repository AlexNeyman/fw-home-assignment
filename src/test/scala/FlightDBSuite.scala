package gpt

import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate
import scala.reflect.ClassTag
import scala.util.Try

class FlightDBSuite extends AnyFlatSpec {
  it should "allow adding valid flights" in {
    val flightDB = new FlightDB()

    assertSuccess(flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assertSuccess(flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 2, 1), "1")))
  }

  it should "not allow adding flights outside of its schedule" in {
    val flightDB = new FlightDB()

    assertSuccess(flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assertFailure[IllegalArgumentException](flightDB.addFlight(Flight("A", "B", LocalDate.of(2020, 1, 1), "1")))
  }

  it should "allow checking for existing flights" in {
    val flightDB = new FlightDB()
    val flight1 = Flight("A", "B", LocalDate.of(2023, 1, 1), "1")
    val flight2 = Flight("A", "B", LocalDate.of(2023, 2, 1), "1")

    assertSuccess(flightDB.addFlight(flight1))
    assertSuccess(flightDB.addFlight(flight2))

    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 2, 1), "1")))
  }

  it should "distinguish between airports and flight numbers" in {
    val flightDB = new FlightDB()

    assertSuccess(flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))

    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("B", "A", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "2")))
  }

  it should "allow checking for flights even outside of schedule" in {
    val flightDB = new FlightDB()

    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assertSuccess(flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2020, 1, 1), "1")))
  }

  "clear()" should "clear the database" in {
    val flightDB = new FlightDB()
    val flight = Flight("A", "B", LocalDate.of(2023, 1, 1), "1")

    assertSuccess(flightDB.addFlight(flight))
    assert(flightDB.flightExists(flight))

    flightDB.clear()
    assert(!flightDB.flightExists(flight))

    assertSuccess(flightDB.addFlight(flight))
    assert(flightDB.flightExists(flight))
  }

  private def assertSuccess(result: Try[Unit]): Unit = {
    assert(result.isSuccess)
  }

  private def assertFailure[T <: AnyRef : ClassTag](result: Try[Unit]): Unit = {
    assert(result.isFailure)
    assertThrows[T] {
      // You have to be cunning when you don't know Scala
      throw result.failed.get
    }
  }
}
