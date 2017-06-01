package cruisR.geo_stream_rdf

case class ProcessedData(
    val imei: String,
    val date: String,
    val latitudeDecimalDegree: Float,
    val longitudeDecimalDegree: Float,
    val batteryStatus: String,
    val chargingStatus: String,
    val speedNauticalMiles: String,
    val angle: String,
    val altitude: String,
    val eventType: String) {

  def this(rawData: RawData) = 
    this(
      rawData.imei,
      rawData.date,
      rawData.latitudeDecimalDegree,
      rawData.longitudeDecimalDegree,
      rawData.batteryStatus,
      rawData.chargingStatus,
      rawData.speedNauticalMiles,
      rawData.angle,
      rawData.altitude,
      rawData.eventType)

  def toCSV() = productIterator.map {
    case Some(value) => value
    case None        => ""
    case rest        => rest
  }.mkString(",")
}