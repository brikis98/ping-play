package com.ybrikman.ping.javaapi.bigpipe;

import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import play.libs.HttpExecution;
import scala.concurrent.ExecutionContext;

/**
 * The base interface for "pagelets", which represent small, self-contained pieces of a page that can be rendered
 * independently.
 */
public interface Pagelet extends com.ybrikman.ping.scalaapi.bigpipe.Pagelet {

  default public HtmlStream renderPlaceholder() {
    return renderPlaceholder(HttpExecution.defaultContext());
  }

  default public HtmlStream renderServerSide() {
    return renderServerSide(HttpExecution.defaultContext());
  }

  default public HtmlStream renderClientSide() {
    return renderClientSide(HttpExecution.defaultContext());
  }

  @Override
  default public HtmlStream renderPlaceholder(ExecutionContext ec) {
    return wrapped(ec).renderPlaceholder(ec);
  }

  @Override
  default public HtmlStream renderServerSide(ExecutionContext ec) {
    return wrapped(ec).renderServerSide(ec);
  }

  @Override
  default public HtmlStream renderClientSide(ExecutionContext ec) {
    return wrapped(ec).renderClientSide(ec);
  }

  com.ybrikman.ping.scalaapi.bigpipe.Pagelet wrapped(ExecutionContext ec);
}
