package gpt

import java.time.LocalDate

class FlightDB {
  private case class FlightSchedule(
                                     // Used as an offset to calculate the index of a flight in the schedule
                                     firstFlightDate: LocalDate,
                                     // Each boolean represents whether a flight exists on that day
                                     schedule: Vector[Boolean] = Vector.empty
                                   ) {
    def addFlight(flight: Flight): FlightSchedule = {
      flightDateToScheduleIndex(flight.date) match {
        case i if i >= schedule.length =>
          val newSchedule = schedule ++ Vector.fill(i - schedule.length + 1)(false)
          FlightSchedule(firstFlightDate, newSchedule.updated(i, true))
        case i =>
          FlightSchedule(firstFlightDate, schedule.updated(i, true))
      }
    }

    def flightExists(flight: Flight): Boolean = {
      if (flight.date.isBefore(firstFlightDate) ||
        flight.date.isAfter(firstFlightDate.plusDays(schedule.length))) {
        false
      } else {
        schedule(flightDateToScheduleIndex(flight.date))
      }
    }

    private def flightDateToScheduleIndex(flightDate: LocalDate): Int = {
      flightDate.toEpochDay - firstFlightDate.toEpochDay match {
        case i if i > Int.MaxValue =>
          // FIXME: Exceptions are not the best way to handle this
          throw new IllegalArgumentException("Flight is too far in the future")
        case i if i < 0 =>
          // FIXME: Exceptions are not the best way to handle this
          throw new IllegalArgumentException("Flight is before the first flight in the schedule")
        case i =>
          i.toInt
      }
    }
  }

  private var flights = Map.empty[String, FlightSchedule]

  def addFlight(flight: Flight): Unit = {
    val flightSchedule = flights.getOrElse(flight.id, FlightSchedule(flight.date))

    flights += (flight.id -> flightSchedule.addFlight(flight))
  }

  def clear(): Unit = {
    flights = Map.empty
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
