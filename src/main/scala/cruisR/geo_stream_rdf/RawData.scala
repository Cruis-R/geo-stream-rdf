package cruisR.geo_stream_rdf

/** 114715.000 signifie l'heure du tracking 11H47:15 */
case class RawData(
    val msisdn: String,
    val timetracked: String,
    val latitude: String,
    val longitude: String,
    val datetracked: String,
    val imei: String,
    val speedNauticalMiles: String,
    val angle: String,
    val satelliteCount: String,
    val altitude: String,
    val batteryStatus: String,
    val chargingStatus: String,
    val eventType: String,
    val preceding: Option[RawData] = None
    ) {

  def toString2() =
    s"IMEI found $imei with timetracked: $timetracked, coords $longitude $latitude, batteryStatus $batteryStatus, speed $speedNauticalMiles"

  /**
   * En JSON-LD avec un @context hébergé sur le Web:
   * {
   * "@context": "https://deductions.github.io/drivers.context.jsonld",
   * "@id": "point:/imei:1234/2017-05-31T12:43:00.000",
   * "mobile": "imei:863977030715952",
   * "lat": "48.83763",
   * "long": "2.3348699",
   * "date": "2017-05-31T12:43:00.000",
   * "precedingPoint": "point:/imei:1234/2017-05-31T12:44:44.444"
   * }
   */
  def toJSON_LD(): String = {
    s"""{
     "@context": "https://deductions.github.io/drivers.context.jsonld",
     "@id": $makeURI,
     "mobile": "imei:$imei",
     "lat": "$latitudeDecimalDegree",
     "long": "$longitudeDecimalDegree",
     "date": "$date"
     ${
      preceding match {
        case Some(rawData) =>
          s"""", precedingPoint": ${rawData.makeURI}"""
        case _ => ""
      }
    }
   }"""
  }

  def makeURI() = s"point:/imei:$imei/$date"

  def latitudeDecimalDegree = {
//    println( s"latitude $latitude")
    val degree = latitude.slice(0, 2) . toInt
    val minutes = latitude.slice(2,100) . toFloat
//    println( s"degree $degree, minutes $minutes")
    degree + minutes / 60
  }

  def longitudeDecimalDegree = {
//    println( s"longitude $longitude")
    val degree = longitude.slice(0, 3) . toInt
    val minutes = longitude.slice(3,100) . toFloat
//    println( s"degree $degree, minutes $minutes")
    degree + minutes / 60
  }

  /** compute data in XML Schema format */
  def date = {
//    println( s"timetracked $timetracked, datetracked $datetracked")
    val time = s"${timetracked.slice(0, 2)}:${timetracked.slice(2, 4)}:${timetracked.slice(4, 10)}"
    val day   = datetracked.slice(0, 2)
    val month = datetracked.slice(2, 4)
    val year  = datetracked.slice(4, 6)
//    println( s"day $day, month $month, year $year")
    val date = s"20${year}-${month}-${day}"
    s"${date}T${time}"
  }
}
