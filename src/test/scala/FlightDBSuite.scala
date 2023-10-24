package gpt

import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate

class FlightDBSuite extends AnyFlatSpec {
  it should "validate its args" in {
    assertThrows[IllegalArgumentException] {
      new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2022, 1, 1))
    }
  }

  it should "allow adding valid flights" in {
    val flightDB = new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 2, 1), "1"))
  }

  it should "not allow adding flights outside of its schedule" in {
    val flightDB = new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 2))
    assertThrows[IllegalArgumentException] {
      flightDB.addFlight(Flight("A", "B", LocalDate.of(2022, 1, 1), "1"))
    }
    assertThrows[IllegalArgumentException] {
      flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 3), "1"))
    }
  }

  it should "allow checking for existing flights" in {
    val flightDB = new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 2, 1), "1"))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 2, 1), "1")))
  }

  it should "distinguish between airports and flight numbers" in {
    val flightDB = new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))
    flightDB.addFlight(Flight("A", "B", LocalDate.of(2023, 1, 1), "1"))
    assert(flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("B", "A", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "2")))
  }

  it should "allow checking for nonexistent flights even outside of schedule" in {
    val flightDB = new FlightDB(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2023, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2020, 1, 1), "1")))
    assert(!flightDB.flightExists(Flight("A", "B", LocalDate.of(2025, 1, 1), "1")))
  }
}
