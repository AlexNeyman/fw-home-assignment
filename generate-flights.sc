import scala.util.{Random, Using}
import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDate

val csvPath = "./flights.generated.csv"

// Real world annual pre-COVID stats
val airportCount = 10_000
val routeCount = 68_000
val flightCount = 40_000_000

val scheduleLengthYears = 1
val scheduleLengthDays = scheduleLengthYears * 365
val today = LocalDate.now()

val airportChars = 'A' to 'Z'
val airports = (1 to airportCount).map(_ =>
  (1 to 3).map(_ =>
    ('A' to 'Z')(Random.nextInt(airportChars.length))
  ).mkString
).toArray

val routes = (1 to routeCount).map(i => {
  val from = airports(Random.nextInt(airports.length))
  val to = airports(Random.nextInt(airports.length))
  (from, to, 1000 + i)
}).toArray

val flights = (1 to flightCount).to(LazyList).map(i => {
  val route = routes(Random.nextInt(routes.length))
  val date = today.plusDays(i * scheduleLengthDays / flightCount)
  (route._1, route._2, date, route._3)
})

Using(new BufferedWriter(new FileWriter(csvPath))) { file =>
  flights.foreach(f => file.write(s"${f._1},${f._2},${f._3},${f._4}\n"))
}
