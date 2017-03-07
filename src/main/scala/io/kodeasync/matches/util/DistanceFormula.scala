package io.kodeasync.matches.util

/**
  * Created by shishir on 3/5/17.
  */
object DistanceFormula {

  /**
    * Logic Copied from http://stackoverflow.com/questions/3694380/calculating-
    * distance-between-two-points-using-latitude-longitude-what-am-i-doi
    *
    * Calculate distance between two points in latitude and longitude
    * Uses Haversine method as its base.
    *
    * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
    * el2 End altitude in meters
    *
    * @return Distance in Kms
    */
  def getDistance(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double = {
    val R = 6371 // Radius of the earth
    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    var distance = R * c
    distance = Math.pow(distance, 2)
    Math.sqrt(distance)
  }

}
