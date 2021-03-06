package cruisR.geo_stream_rdf

import java.io._
import java.net.{
  ServerSocket,
  Socket,
  SocketException }
import scala.collection._ // mutable._


/**
 * Simple client/server application using Java sockets.
 * http://www.scala-lang.org/old/node/55
 */
object TCPDump {

  def main(args: Array[String]): Unit = {

    val receivingServerUrl = args match {
      case Array(url) => url
      case _ => "http://SPARQLGraphStore.org/load"
    }

    try {
      val listener = new ServerSocket(9999)
      while (true) {
        println("TCPDump: Starting top level loop")
        val runner = new ServerThread(listener.accept()) {
          override val dataReceivingServerUrl = receivingServerUrl
        }
        runner.start()
      }

      listener.close()
    } catch {
      case e: IOException =>
        System.err.println("Could not listen on port: 9999.");
        System.exit(-1)
    }
  }
}

abstract case class ServerThread(socket: Socket)
    extends Thread("ServerThread")
    with HTTPpostclient {

  val dataReceivingServerUrl = ""
  val precedingPoints = mutable.Map[String, RawData]()
  
  val logger = System.out
  // append and autoflush
  val logger2 = new PrintStream(new FileOutputStream("geo.csv", true), true)

  override def run(): Unit = {
		val in = 
      new DataInputStream(socket.getInputStream())
    try {
      //      val out = new DataOutputStream(socket.getOutputStream());
      while (true) {
        val line = in.readLine() // TODO use BufferedReader
        regex_on_tcpdump(line) match {
          case Some(data) =>
            // send the RDF (JSON-LD) to RDF REST server)
            send(data.toJSON_LD)
            println(s"data sent at ${new java.util.Date()}")
          case _ =>
            if (isLineAdmissible(line) )
              println(s"HTTPpostclient: regex not matching or line not expected: '$line'")
        }
        Thread.sleep(100)
      }
      //      out.close();
    } catch {
      case e: SocketException =>
        println("SocketException " + e.getLocalizedMessage)
        // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    } finally {
            //      out.close();
      in.close()
      socket.close()
      logger2.close()
    }
  }

  val DECIMAL = """(\d+\.\d+)"""
  val INTEGER = """(\d+)"""
  val PLUSINTEGER = """(\+\d+)"""
  val LETTER = """(\w)"""
  val DOT="""\."""
  val BATTERY = """(\w:\d+\.\d+V)""" // “F:4.11V” full battery, “L:3.65V” low battery

  /** specific messages */
  val WORD_OR_NOT =
    """(|battery|SHAKE|shake|move|ACC on|ACC OFF|speed|stockade|low batt|Low batt|help|ACCStart|ACCStop)"""

  /**
   * regex getting relevant information from the tcpdump
   * typical input line:
   * 170524170838,+33689162952,GPRMC,160838.000,A,4850.2382,N,00220.0353,E,000.0,000.0,240517,,,A*68,L,, imei:863977030715952,06,76.8,F:4.14V,1,139,4305,208,01,0300,4679
   */
  val regex =
    s"""$INTEGER,$PLUSINTEGER,GPRMC,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$DECIMAL,$INTEGER,,,....,$LETTER,$WORD_OR_NOT, imei:$INTEGER,$INTEGER,$DECIMAL,$BATTERY,$INTEGER,(.*)""" r

  var beginning = true

  /**
   * Parsing tcpdump and building ImeiTracking objects.
   * time must match utctime of the elapsed minute ?
   */
  def regex_on_tcpdump(line: String): Option[RawData] = {
    try {
      if ( isLineAdmissible(line) ) {
        println(s"""'$line'""")
        val regex(
          timestamp, phoneNumber, time, xx,
          latitudeString, northSouth,
          longitudeString, eastWest,
          speedNauticalMiles, angle, date,
          validGPSsignal, eventType, imei,
          satelliteCount, altitude, batteryStatus, chargingStatus,
          endOfLine // gpsLen, crc16, mcc, mnc, lac, cellID
          ) = line

        val rawData = RawData(
          msisdn = timestamp,
          timetracked = time,
          latitude = latitudeString,
          longitude = longitudeString,
          datetracked = date,
          imei = imei,
          speedNauticalMiles = speedNauticalMiles,
          angle = angle,
          satelliteCount, altitude, batteryStatus,
          chargingStatus, eventType)
         
        val preceding = precedingPoints.getOrElse(imei, rawData)
        precedingPoints(imei) = rawData

        // TODO compare on all fields but not timetracked
        if (preceding != rawData || beginning ) {
            val csv = new ProcessedData(rawData).toCSV()
            logger.println(s"csv $csv")
            //      logger.println(s"rawData : $rawData")
            // logger.println(s"${rawData.toJSON_LD()}")
            logger2.println(csv)
            Some(rawData.copy(preceding=Some(preceding)))
        } else {
            logger.println(s"For imei $imei NO CHANGE")
            None
        }
      } else
        None

    } catch {
      case t: Throwable =>
        println(s"""|EEE
        |Error: ${t.getLocalizedMessage}
        |line: '$line'
        |EEE""".stripMargin )
        None
    } finally beginning = false
  }

  private def isLineAdmissible(line: String) = line != "" && line != "null" && line != null

}

object ParseTest extends ServerThread(null) with App {
  val line = args(0)
  println(s"csv '$line'")
  val dataOption = regex_on_tcpdump(line)
  println(s"dataOption $dataOption")
  println(s"ProcessedData ${new ProcessedData(dataOption.get)}")
  dataOption match {
    case Some(rawData) => println( rawData.toJSON_LD() )
    case _ =>
  }
}