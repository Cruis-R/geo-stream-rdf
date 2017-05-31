package cruisR.geo_stream_rdf

import java.io._
import java.net.{ InetAddress, ServerSocket, Socket, SocketException }

/**
 * Simple client/server application using Java sockets.
 * http://www.scala-lang.org/old/node/55
 */
object TCPDump {

  def main(args: Array[String]): Unit = {
    try {
      val listener = new ServerSocket(9999);
      while (true)
        new ServerThread(listener.accept()).start();
      listener.close()
    } catch {
      case e: IOException =>
        System.err.println("Could not listen on port: 9999.");
        System.exit(-1)
    }
  }
}

case class ServerThread(socket: Socket)
extends Thread("ServerThread")
with HTTPpostclient {

  val regexold =
    """(\+\d+)(,)(GPRMC,)(\d+\.\d+),\w,(\d+\.\d+),\w,(\d+\.\d+)(.*)(\d{6})(.*)(imei:)(\d+)""" r
  val logger = System.out
  
  val dataReceivingServerUrl = "" // http://semantic-forms.cc:9000/position"
  
  override def run(): Unit = {
    try {
      //      val out = new DataOutputStream(socket.getOutputStream());
      val in = 
        new DataInputStream(socket.getInputStream())

      while (true) {
        val line = in.readLine() // TODO use BufferedReader
        println(line)
        regex_on_tcpdump(line) match {
          case Some(data) =>
            // send the RDF (JSON-LD) to RDF REST server)
            send(data.toJSON_LD)          
          case _ => println("regex not matching")
        }
        Thread.sleep(100)
      }
      //      out.close();
      in.close();
      socket.close()
    } catch {
      case e: SocketException =>
        () // avoid stack trace when stopping a client with Ctrl-C
      case e: IOException =>
        e.printStackTrace();
    }
  }

  val DECIMAL = """(\d+\.\d+)"""
  val INTEGER = """(\d+)"""
  val PLUSINTEGER = """(\+\d+)"""
  val LETTER = """(\w)"""
  val DOT="""\."""
  val BATTERY = """(\w:\d+\.\d+V)""" // “F:4.11V” full battery, “L:3.65V” low battery
  /**
   * regex getting relevant information from the tcpdump
   * typical input line:
   * 170524170838,+33689162952,GPRMC,160838.000,A,4850.2382,N,00220.0353,E,000.0,000.0,240517,,,A*68,L,, imei:863977030715952,06,76.8,F:4.14V,1,139,4305,208,01,0300,4679
   */
//    s"""$INTEGER,$PLUSINTEGER,GPRMC,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$DECIMAL,$INTEGER,.*""" r
  val regex =
    s"""$INTEGER,$PLUSINTEGER,GPRMC,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$LETTER,$DECIMAL,$DECIMAL,$INTEGER,,,....,$LETTER,, imei:$INTEGER,$INTEGER,$DECIMAL,$BATTERY,$INTEGER,$INTEGER,(.*)""" r

  /**
   * Parsing tcpdump and building ImeiTracking objects.
   * time must match utctime of the elapsed minute ?
   */
  def regex_on_tcpdump(line: String): Option[RawData] = {
    try {
      val regex(
          timestamp,phoneNumber,time,xx,
          longitudeString,eastWest,
          latitudeString,northSouth,
          speedNauticalMiles,angle,date,
          validGPSsignal, imei,
          satelliteCount, altitude, batteryStatus, chargingStatus,
          gpsLen, endOfLine // crc16, mcc, mnc, lac, cellID
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
          satelliteCount, altitude, batteryStatus, chargingStatus
        )
        logger.println(rawData)
        Some(rawData)
    } catch {
    case t: Throwable =>
      println(s"t.getLocalizedMessage // $line")
      None
    }
  }
}