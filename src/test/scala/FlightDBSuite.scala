package gpt

import org.scalatest.flatspec.AnyFlatSpec

import java.time.LocalDate
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

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

  private def assertSuccess(result: Try[Any]): Unit = {
    assert(result.isSuccess)
  }

  private def assertFailure[A <: Throwable : ClassTag](result: Try[Any]): Unit = {
    val clazz = implicitly[ClassTag[A]].runtimeClass

    result match {
      case Failure(exception) =>
        assert(clazz.isInstance(exception),
          s"Expected exception of type $clazz, but got ${exception.getClass}")
      case Success(_) =>
        fail(new AssertionError(s"Expected exception of type $clazz, but got success"))
    }
  }
}
