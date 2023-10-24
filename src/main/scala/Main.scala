package gpt

import cats.effect.unsafe.implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    FlightService.run(args.toList).unsafeRunSync()
  }
}
