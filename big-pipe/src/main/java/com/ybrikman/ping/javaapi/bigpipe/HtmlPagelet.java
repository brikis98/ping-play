package com.ybrikman.ping.javaapi.bigpipe;

import play.libs.F;
import play.twirl.api.Html;
import scala.concurrent.ExecutionContext;

/**
 * A Pagelet that contains HTML. Both server-side and client-side rendering are supported.
 */
public class HtmlPagelet implements Pagelet {

  private final String id;
  private final F.Promise<Html> content;

  public HtmlPagelet(String id, F.Promise<Html> content) {
    this.id = id;
    this.content = content;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public com.ybrikman.ping.scalaapi.bigpipe.Pagelet wrapped(ExecutionContext ec) {
    return new com.ybrikman.ping.scalaapi.bigpipe.HtmlPagelet(id, content.wrapped());
  }
}
