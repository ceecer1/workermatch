package io.kodeasync.matches.util

import java.text.SimpleDateFormat

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats, Serialization}

/**
  * Created by shishir on 3/5/17.
  */
trait JsonSerialization extends Json4sSupport {

  val customDateFormat = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  }

  implicit val formats: Formats = customDateFormat ++ JodaTimeSerializers.all
  implicit val serialization: Serialization = json4s.native.Serialization

}
