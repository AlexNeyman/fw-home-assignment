package gpt

import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class FlightDBSuite extends AnyFlatSpec {
  it should "allow adding valid flights" in {
    val flightDB = new FlightDB()
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 2, 1), "1"))
  }

  it should "not allow adding flights outside of its schedule" in {
    val flightDB = new FlightDB()
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    assertThrows[IllegalArgumentException] {
      flightDB.addFlight(Flight("A", "B", LocalDate.of(2022, 1, 1), "1"))
    }
  }

  it should "allow checking for existing flights" in {
    val flightDB = new FlightDB()
    val flight1 = Flight("A", "B", LocalDate.of(2023, 1, 1), "1")
    val flight2 = Flight("A", "B", LocalDate.of(2023, 2, 1), "1")

    flightDB.addFlight(flight1)
    flightDB.addFlight(flight2)

    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 2, 1), "1")))
  }

  it should "distinguish between airports and flight numbers" in {
    val flightDB = new FlightDB()

    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("B", "A", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "2")))
  }

  it should "allow checking for flights even outside of schedule" in {
    val flightDB = new FlightDB()
    
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2020, 1, 1), "1")))
  }

  it should "clear the database" in {
    val flightDB = new FlightDB()
    val flight = Flight("A", "B", LocalDate.of(2023, 1, 1), "1")

    flightDB.addFlight(flight)
    assert(flightDB.flightExists(flight))

    flightDB.clear()
    assert(!flightDB.flightExists(flight))

    flightDB.addFlight(flight)
    assert(flightDB.flightExists(flight))
  }
}
