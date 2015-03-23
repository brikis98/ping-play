// @SOURCE:D:/work/java/play/ping-play/conf/routes
// @HASH:d088b37d562b5aafbe6435f96d3fe575ebea7d06
// @DATE:Sun Mar 22 23:22:52 PDT 2015


import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset

import Router.queryString

object Routes extends Router.Routes {

import ReverseRouteContext.empty

private var _prefix = "/"

def setPrefix(prefix: String) {
  _prefix = prefix
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:5
private[this] lazy val controllers_Mock_mock0_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("mock/"),DynamicPart("serviceName", """[^/]+""",true))))
private[this] lazy val controllers_Mock_mock0_invoker = createInvoker(
controllers.Mock.mock(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Mock", "mock", Seq(classOf[String]),"GET", """""", Routes.prefix + """mock/$serviceName<[^/]+>"""))
        

// @LINE:7
private[this] lazy val controllers_CompositeStreamController_index1_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("stream"))))
private[this] lazy val controllers_CompositeStreamController_index1_invoker = createInvoker(
controllers.CompositeStreamController.index,
HandlerDef(this.getClass.getClassLoader, "", "controllers.CompositeStreamController", "index", Nil,"GET", """""", Routes.prefix + """stream"""))
        

// @LINE:10
private[this] lazy val controllers_Assets_at2_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("assets/"),DynamicPart("file", """.+""",false))))
private[this] lazy val controllers_Assets_at2_invoker = createInvoker(
controllers.Assets.at(fakeValue[String], fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Assets", "at", Seq(classOf[String], classOf[String]),"GET", """ Map static resources from the /public folder to the /assets URL path""", Routes.prefix + """assets/$file<.+>"""))
        
def documentation = List(("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """mock/$serviceName<[^/]+>""","""controllers.Mock.mock(serviceName:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """stream""","""controllers.CompositeStreamController.index"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""","""controllers.Assets.at(path:String = "/public", file:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]]
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:5
case controllers_Mock_mock0_route(params) => {
   call(params.fromPath[String]("serviceName", None)) { (serviceName) =>
        controllers_Mock_mock0_invoker.call(controllers.Mock.mock(serviceName))
   }
}
        

// @LINE:7
case controllers_CompositeStreamController_index1_route(params) => {
   call { 
        controllers_CompositeStreamController_index1_invoker.call(controllers.CompositeStreamController.index)
   }
}
        

// @LINE:10
case controllers_Assets_at2_route(params) => {
   call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at2_invoker.call(controllers.Assets.at(path, file))
   }
}
        
}

}
     