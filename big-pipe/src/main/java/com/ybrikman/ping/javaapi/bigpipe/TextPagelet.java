package com.ybrikman.ping.javaapi.bigpipe;

import play.libs.F;
import scala.concurrent.ExecutionContext;

/**
 * A Pagelet that contains plain text. Both server-side and client-side rendering are supported.
 */
public class TextPagelet implements Pagelet {
  private final String id;
  private final F.Promise<String> content;

  public TextPagelet(String id, F.Promise<String> content) {
    this.id = id;
    this.content = content;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public com.ybrikman.ping.scalaapi.bigpipe.Pagelet wrapped(ExecutionContext ec) {
    return new com.ybrikman.ping.scalaapi.bigpipe.TextPagelet(id, content.wrapped());
  }
}
