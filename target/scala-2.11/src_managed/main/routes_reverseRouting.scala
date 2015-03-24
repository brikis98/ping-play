// @SOURCE:D:/work/java/play/ping-play/conf/routes
// @HASH:0dbd9369bd16d19b99c86a1295a06d22e46c3620
// @DATE:Mon Mar 23 21:04:23 PDT 2015

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset

import Router.queryString


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:7
// @LINE:5
package controllers {

// @LINE:13
class ReverseAssets {


// @LINE:13
def at(file:String): Call = {
   implicit val _rrc = new ReverseRouteContext(Map(("path", "/public")))
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                        

}
                          

// @LINE:10
class ReverseUpdateViewsStreamController {


// @LINE:10
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "updates")
}
                        

}
                          

// @LINE:9
class ReverseProfileViewsStreamController {


// @LINE:9
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "profile")
}
                        

}
                          

// @LINE:5
class ReverseMock {


// @LINE:5
def mock(serviceName:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "mock/" + implicitly[PathBindable[String]].unbind("serviceName", dynamicString(serviceName)))
}
                        

}
                          

// @LINE:7
class ReverseCompositeStreamController {


// @LINE:7
def index(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "stream")
}
                        

}
                          
}
                  


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:7
// @LINE:5
package controllers.javascript {
import ReverseRouteContext.empty

// @LINE:13
class ReverseAssets {


// @LINE:13
def at : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Assets.at",
   """
      function(file) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
      }
   """
)
                        

}
              

// @LINE:10
class ReverseUpdateViewsStreamController {


// @LINE:10
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.UpdateViewsStreamController.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "updates"})
      }
   """
)
                        

}
              

// @LINE:9
class ReverseProfileViewsStreamController {


// @LINE:9
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.ProfileViewsStreamController.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "profile"})
      }
   """
)
                        

}
              

// @LINE:5
class ReverseMock {


// @LINE:5
def mock : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Mock.mock",
   """
      function(serviceName) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "mock/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("serviceName", encodeURIComponent(serviceName))})
      }
   """
)
                        

}
              

// @LINE:7
class ReverseCompositeStreamController {


// @LINE:7
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.CompositeStreamController.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "stream"})
      }
   """
)
                        

}
              
}
        


// @LINE:13
// @LINE:10
// @LINE:9
// @LINE:7
// @LINE:5
package controllers.ref {


// @LINE:13
class ReverseAssets {


// @LINE:13
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this.getClass.getClassLoader, "", "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """assets/$file<.+>""")
)
                      

}
                          

// @LINE:10
class ReverseUpdateViewsStreamController {


// @LINE:10
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.UpdateViewsStreamController.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.UpdateViewsStreamController", "index", Seq(), "GET", """""", _prefix + """updates""")
)
                      

}
                          

// @LINE:9
class ReverseProfileViewsStreamController {


// @LINE:9
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.ProfileViewsStreamController.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.ProfileViewsStreamController", "index", Seq(), "GET", """""", _prefix + """profile""")
)
                      

}
                          

// @LINE:5
class ReverseMock {


// @LINE:5
def mock(serviceName:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Mock.mock(serviceName), HandlerDef(this.getClass.getClassLoader, "", "controllers.Mock", "mock", Seq(classOf[String]), "GET", """""", _prefix + """mock/$serviceName<[^/]+>""")
)
                      

}
                          

// @LINE:7
class ReverseCompositeStreamController {


// @LINE:7
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.CompositeStreamController.index(), HandlerDef(this.getClass.getClassLoader, "", "controllers.CompositeStreamController", "index", Seq(), "GET", """""", _prefix + """stream""")
)
                      

}
                          
}
        
    