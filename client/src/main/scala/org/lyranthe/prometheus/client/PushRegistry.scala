package org.lyranthe.prometheus.client

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import org.lyranthe.prometheus.client.registry._

class PushRegistry(host: String,
                   port: Int,
                   job: String,
                   additionalLabels: Seq[(String, String)])
    extends DefaultRegistry {
  final private val url = {
    val extra =
      if (additionalLabels.isEmpty) ""
      else "/" + additionalLabels.flatMap(labels => Vector(labels._1, labels._2)).mkString("/")
    new URL("http", host, port, s"/metrics/job/$job$extra")
  }

  def unsafePush(): Boolean = {
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    try {
      conn.setRequestMethod("PUT")
      conn.setRequestProperty("Content-Type", "text/plain; version=0.0.4")
      conn.setConnectTimeout(10 * 1000) // 10s
      conn.setReadTimeout(10 * 1000)    // 10s
      conn.setDoOutput(true)
      conn.connect()
      val output = new DataOutputStream(conn.getOutputStream)
      output.write(TextFormat.output(collect()))
      output.flush()
      val responseCode = conn.getResponseCode
      output.close()

      responseCode == 202
    } finally {
      conn.disconnect()
    }
  }
}

object PushRegistry {
  def apply(host: String, port: Int = 9091)(
      job: String,
      instance: Option[String],
      additionalLabels: (String, String)*): PushRegistry =
    new PushRegistry(host,
                     port,
                     job,
                     instance.map("instance" -> _).toSeq ++ additionalLabels)
}
