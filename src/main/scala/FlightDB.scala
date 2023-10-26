package gpt

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

class FlightDB {
  private case class FlightSchedule(
                                     // Used as an offset to calculate the index of a flight in the schedule
                                     firstFlightDate: LocalDate,
                                     // Each boolean represents whether a flight exists on that day.
                                     // Can be replaced with a bitset for better memory efficiency.
                                     schedule: Array[Boolean] = Array.empty
                                   ) {
    def addFlight(flight: Flight): Try[FlightSchedule] = {
      flightDateToScheduleIndex(flight.date) match {
        case Success(i) if i >= schedule.length =>
          // This approach may be a problem on a large scale because of lots of copying.
          // Though there is a simple solution: we need to know the min and max dates in the schedule beforehand.
          // Then we can allocate an array of the right size in advance.
          //
          // We also can use vectors, which is a persistent data structure,
          // so it doesn't copy the whole data on appends.
          // The downside of vectors is that they use more memory than arrays,
          // plus they can be slightly slower in lookups.
          val newSchedule = schedule ++ Array.fill(i - schedule.length + 1)(false)
          Success(this.copy(schedule = newSchedule.updated(i, true)))

        case Success(i) =>
          Success(this.copy(schedule = schedule.updated(i, true)))

        case Failure(exception) =>
          Failure(exception)
      }
    }

    def flightExists(flight: Flight): Boolean = {
      if (flight.date.isBefore(firstFlightDate) ||
        flight.date.isAfter(firstFlightDate.plusDays(schedule.length))) {
        false
      } else {
        flightDateToScheduleIndex(flight.date) match {
          case Success(i) => schedule(i)
          case Failure(_) => false
        }
      }
    }

    private def flightDateToScheduleIndex(flightDate: LocalDate): Try[Int] = {
      flightDate.toEpochDay - firstFlightDate.toEpochDay match {
        case i if i > Int.MaxValue =>
          Failure(new IllegalArgumentException("Flight is too far in the future"))
        case i if i < 0 =>
          Failure(new IllegalArgumentException("Flight is before the first flight in the schedule"))
        case i =>
          Success(i.toInt)
      }
    }
  }

  private var flights = Map.empty[String, FlightSchedule]

  def addFlight(flight: Flight): Try[Unit] = {
    val flightSchedule = flights.getOrElse(flight.id, FlightSchedule(flight.date))

    flightSchedule.addFlight(flight) match {
      case Success(newFlightSchedule) =>
        flights += (flight.id -> newFlightSchedule)
        Success(())

      case Failure(exception) =>
        Failure(exception)
    }
  }

  def flightExists(flight: Flight): Boolean = {
    flights.get(flight.id) match {
      case Some(schedule) =>
        schedule.flightExists(flight)
      case None =>
        false
    }
  }
}
