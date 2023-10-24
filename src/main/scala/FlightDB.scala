package gpt

import java.time.LocalDate


class FlightDB() {
  private type FlightKey = String
  private type Schedule = Array[Boolean]

  private val flights = Map.empty[FlightKey, Schedule]
  private val scheduleCoverage = (LocalDate.now(), LocalDate.now())
}
