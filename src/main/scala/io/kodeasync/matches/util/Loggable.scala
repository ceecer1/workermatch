package io.kodeasync.matches.util

import org.slf4j.LoggerFactory

/**
  * Created by shishir on 3/5/17.
  */
trait Loggable {

  val logger = LoggerFactory.getLogger(getClass)

}
