package cruisR.geo_stream_rdf

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.entity.ContentType

trait HTTPpostclient {

	val dataReceivingServerUrl: String

  def send(content: String) = {

    if (dataReceivingServerUrl != "" &&
        content != "") {

      val post = new HttpPost(dataReceivingServerUrl)
      post.addHeader("Content-Type","application/ld+json")      
      //    post.addHeader("appid","YahooDemo")
      //    post.addHeader("query","umbrella")
      //    post.addHeader("results","10")

      val client = new DefaultHttpClient
      val params = client.getParams
      params.setParameter("graph", "data:/points/")

      //    val nameValuePairs = new ArrayList[NameValuePair](1)
      //    nameValuePairs.add(new BasicNameValuePair("registrationid", "123456789"));
      //    nameValuePairs.add(new BasicNameValuePair("accountType", "GOOGLE"));
      //    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      val entity = new ByteArrayEntity(
          content.getBytes("UTF-8"),
            ContentType.create( "application/ld+json", "utf-8" )
      )
      post.setEntity(entity)

      // send the post request
      val response = client.execute(post)
      println("--- response HEADERS ---")
      response.getAllHeaders.foreach(arg => println(arg))
    }
  }
}