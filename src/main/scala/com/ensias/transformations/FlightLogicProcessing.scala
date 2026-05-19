package com.ensias.transformations

import com.ensias.models._
import org.apache.spark.sql.{Dataset, SparkSession}

import java.time.Instant

object FlightLogicProcessing {

  def process(
    input: Dataset[FlightInputDataModel]
  ): Dataset[FlightOutputDataModel] = {

    import input.sparkSession.implicits._

    input.map { flight =>

      // ---------------------------------------------------------------------
      // Derived metrics
      // ---------------------------------------------------------------------

      val durationHours =
        math.abs(
          java.time.Duration
            .between(
              java.time.LocalDateTime.parse(flight.departureTime),
              java.time.LocalDateTime.parse(flight.arrivalTime)
            )
            .toMinutes
        ) / 60.0

      val speed =
        if (durationHours > 0) flight.distanceKm / durationHours else 0.0

      val isDelayed =
        flight.delayMinutes.exists(_ > 15)

      val severity =
        flight.delayMinutes match {
          case Some(d) if d > 120 => "CRITICAL"
          case Some(d) if d > 60  => "HIGH"
          case Some(d) if d > 15  => "MEDIUM"
          case Some(_)            => "LOW"
          case None               => "ON_TIME"
        }

      // ---------------------------------------------------------------------
      // Output model
      // ---------------------------------------------------------------------

      FlightOutputDataModel(
        flightId = flight.flightId,
        carrierName = flight.carrier.name,

        route = FlightRoute(
          origin = s"${flight.origin.city} (${flight.origin.code})",
          destination = s"${flight.destination.city} (${flight.destination.code})"
        ),

        metrics = FlightMetrics(
          speedKmh = speed,
          durationHours = durationHours
        ),

        status = FlightStatus(
          isDelayed = isDelayed,
          delaySeverity = severity
        ),

        processedAt = Instant.now().toString
      )
    }
  }
}