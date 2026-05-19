package com.ensias.models

case class Airport(
  code: String,
  name: String,
  city: String,
  country: String
)

case class Carrier(
  code: String,
  name: String
)

case class FlightInputDataModel(
  flightId: String,
  carrier: Carrier,
  origin: Airport,
  destination: Airport,
  departureTime: String,
  arrivalTime: String,
  distanceKm: Double,
  delayMinutes: Option[BigInt]
)