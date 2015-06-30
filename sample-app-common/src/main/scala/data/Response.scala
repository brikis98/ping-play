package data

import play.api.libs.json.Json

/**
 * Simple class used to represent a response from a remote service
 *
 * @param id
 * @param delay
 */
case class Response(id: String, delay: Long)

object Response {
  implicit val responseWrites = Json.writes[Response]
}