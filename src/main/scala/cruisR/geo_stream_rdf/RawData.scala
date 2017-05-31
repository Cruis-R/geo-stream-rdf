package cruisR.geo_stream_rdf

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
    val chargingStatus: String
    ) {

  override def toString() =
    s"IMEI found $imei with timetracked: $timetracked, coords $longitude $latitude, batteryStatus $batteryStatus, speed $speedNauticalMiles"

  /**
   * En JSON-LD avec un @context hébergé sur le Web:
   {
     "@context": "https://deductions.github.io/drivers.context.jsonld",
     "@id": "urn:user/u1",
     "lat": "48.862415",
     "long": "2.342431"
   }
   */
  def toJSON_LD(): String = {
    /* 114715.000 signifie l'heure du tracking 11H47:15
     * 
     *  */
    s"""{
     "@context": "https://deductions.github.io/drivers.context.jsonld",
     "@id": "imei:$imei",
     "lat": "$latitudeDecimalDegree",
     "long": "$longitudeDecimalDegree",
     "date": "$date"
   }"""
  }
  
  def latitudeDecimalDegree = {
    val degree = latitude.slice(0, 1) . toInt
    val minutes = latitude.slice(2,100) . toFloat
    degree + minutes / 60
  }

  def longitudeDecimalDegree = {
    val degree = latitude.slice(0, 2) . toInt
    val minutes = latitude.slice(4,100) . toFloat
    degree + minutes / 60
  }

  def date = {
    val time = s"${timetracked.slice(0, 1)}:${timetracked.slice(2, 3)}:${timetracked.slice(4, 5)}"
    val day   = datetracked.slice(0, 1)
    val month = datetracked.slice(2, 3)
    val year  = datetracked.slice(3, 4)
    val date = s"20${year}-${month}-${day}"
    s"${date}T${time}"
  }
}