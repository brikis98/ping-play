package com.ybrikman.ping.javaapi.bigpipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.ybrikman.ping.scalaapi.bigpipe.JavaAdapter;
import play.libs.F;
import scala.concurrent.ExecutionContext;

/**
 * A Pagelet that contains JSON. The general usage pattern is to send this JSON to the browser and render it using a
 * client-side templating language, such as Mustache.js. Therefore, this Pagelet only supports client-side rendering
 * and will throw an exception if you try to render it server-side.
 */
public class JsonPagelet implements Pagelet {
  private final String id;
  private final F.Promise<JsonNode> content;

  public JsonPagelet(String id, F.Promise<JsonNode> content) {
    this.id = id;
    this.content = content;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public com.ybrikman.ping.scalaapi.bigpipe.Pagelet wrapped(ExecutionContext ec) {
    return new com.ybrikman.ping.scalaapi.bigpipe.JsonPagelet(id, content.map(JavaAdapter::jsonNodeToJsValue, ec).wrapped());
  }
}
