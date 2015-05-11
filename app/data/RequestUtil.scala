package data

import play.api.mvc.RequestHeader
import play.mvc.Http.{RequestHeader => RequestHeaderJ}

object RequestUtil {
  
  private val HOST_PORT_REGEX = ".+?:(\\d+)".r
  private val DEFAULT_PORT = 9000

  /**
   * Quick hack to get the current port number from the incoming request. I'd
   * prefer to read it from Play's configs, but the http.port config is not set
   * in all environments (e.g. it doesn't always have a value in dev, prod, and
   * test).
   */
  def port(request: RequestHeader): Int = {
    port(request.host)
  }

  /**
   * Quick hack to get the current port number from the incoming request. I'd
   * prefer to read it from Play's configs, but the http.port config is not set
   * in all environments (e.g. it doesn't always have a value in dev, prod, and
   * test).
   */
  def port(request: RequestHeaderJ): Int = {
    port(request.host())
  }

  private def port(host: String): Int = {
    host match {
      case HOST_PORT_REGEX(port) => port.toInt
      case _ => 9000
    }    
  }
}