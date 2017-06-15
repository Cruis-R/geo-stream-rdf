package cruisR.geo_stream_rdf

import scala.collection.mutable

trait MobileManager {
  val imeiSet = mutable.Set[String]()
  
  /** Type declaration for JSON-LD */
  def manageRawDataAddType(rawData: RawData): String = {
    val imei = rawData.imei
    if( imeiSet.add( imei ) ) {
      println(s"NEW IMEI added: $imei")
      s""",
      {
        "@id": "imei:$imei",
        "@type": "geoloc:Mobile"
      }"""
    } else ""
  }
}