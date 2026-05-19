package com.ensias.models

case class FlightStatus(
  isDelayed: Boolean,
  delaySeverity: String
)

case class FlightMetrics(
  speedKmh: Double,
  durationHours: Double
)

case class FlightRoute(
  origin: String,
  destination: String
)

case class FlightOutputDataModel(
  flightId: String,
  carrierName: String,
  route: FlightRoute,
  metrics: FlightMetrics,
  status: FlightStatus,
  processedAt: String
)