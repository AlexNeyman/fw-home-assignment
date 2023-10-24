package gpt

import java.time.LocalDate

class FlightDB(val scheduleStartsFrom: LocalDate, val scheduleEndsAt: LocalDate) {
  private type FlightKey = String
  private type Schedule = Array[Boolean]

  if (scheduleStartsFrom.isAfter(scheduleEndsAt)) {
    throw new IllegalArgumentException("scheduleStartsFrom cannot be after scheduleEndsAt")
  }

  private val scheduleSize = scheduleEndsAt.toEpochDay - scheduleStartsFrom.toEpochDay + 1
  // Such defensive programming is not always required
  if (scheduleSize > Int.MaxValue) {
    throw new IllegalArgumentException("Schedule is too long")
  }

  private var flights = Map.empty[FlightKey, Schedule]

  def addFlight(flight: Flight): Unit = {
    if (flight.date.isBefore(scheduleStartsFrom) || flight.date.isAfter(scheduleEndsAt)) {
      throw new IllegalArgumentException("Flight date is outside of schedule")
    }

    val flightSchedule: Schedule =
      flights.getOrElse(flight.id,
        Array.fill(scheduleSize.toInt)(false))

    flightSchedule(flightDateToScheduleIndex(flight.date)) = true

    flights += (flight.id -> flightSchedule)
  }

  def flightExists(flight: Flight): Boolean = {
    flights.get(flight.id) match {
      case Some(schedule) =>
        schedule(flightDateToScheduleIndex(flight.date))
      case None =>
        false
    }
  }

  private def flightDateToScheduleIndex(date: LocalDate): Int = {
    val i = date.toEpochDay - scheduleStartsFrom.toEpochDay
    if (i > Int.MaxValue) {
      throw new IllegalArgumentException("Flight is too far in the future")
    }
    i.toInt
  }
}
