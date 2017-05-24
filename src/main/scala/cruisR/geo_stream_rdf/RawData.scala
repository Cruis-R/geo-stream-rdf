package cruisR.geo_stream_rdf

case class RawData(
    val msisdn: String,
    val timetracked: String,
    val latitude: String,
    val longitude: String,
    val datetracked: String,
    val imei: String) {

  override def toString() =
    s"IMEI found in tcpdump {$imei with timetracked: $timetracked, $longitude $latitude"

  /**
   * typical input line:
   * 2017-05-16 14:33:02,032 - traffic_parser - INFO - IMEI tracked : IMEI: 863977030761766, msisdn: +33607357711, timetracked: 123250.000, datetracked: 160517, latitude: 4850.2417, longitude: 00220.0349
   * 
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
     * msisdn: +33607357711, timetracked: 123250.000, datetracked: 160517,
     * latitude: 4850.2417, longitude: 00220.0349 */
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