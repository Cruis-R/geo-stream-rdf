package cruisR.geo_stream_rdf

import java.io._
import java.net.{ InetAddress, ServerSocket, Socket, SocketException }

/**
 * Simple client/server application using Java sockets.
 * http://www.scala-lang.org/old/node/55
 *
 * The server simply generates random integer values and
 * the clients provide a filter function to the server
 * to get only values they interested in (eg. even or
 * odd values, and so on).
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

  /**
   * regex getting relevant information from the tcpdump
   * typical line:
   *  2017-05-16 14:33:02,032 - traffic_parser - INFO - IMEI tracked : IMEI: 863977030761766, msisdn: +33607357711, timetracked: 123250.000, datetracked: 160517, latitude: 4850.2417, longitude: 00220.0349
   */
  val regex =
    """(\+\d+)(,)(GPRMC,)(\d+\.\d+),\w,(\d+\.\d+),\w,(\d+\.\d+)(.*)(\d{6})(.*)(imei:)(\d+)""" r
  val logger = System.out

  val dataReceivingServerUrl = "http://semantic-forms.cc:9000/position"
  
  override def run(): Unit = {
    try {
      //      val out = new DataOutputStream(socket.getOutputStream());
      val in = 
//        new ObjectInputStream(
        new DataInputStream(socket.getInputStream())

      while (true) {
        val line = in.readLine() // TODO use BufferedReader
        println(line)
        regex_on_tcpdump(line) match {
          case Some(data) =>

            // send the RDF (JSON-LD) to RDF REST server)
            send(data.toJSON_LD)
            
          case _ =>
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

  /**
   * Parsing tcpdump and building ImeiTracking objects.
   * time must match utctime of the elapsed minute ?
   */
  def regex_on_tcpdump(line: String): Option[RawData] = {
    try {
      val bim = regex.split(line)
      if (bim.size > 8) {
        val rawData = RawData(
          msisdn = bim(1),
          timetracked = bim(4),
          latitude = bim(5),
          longitude = bim(6),
          datetracked = bim(8),
          imei = bim(11))
        logger.println(rawData)
        Some(rawData)
      } else None
    }
  }
}