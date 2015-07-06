package com.ybrikman.ping.javaapi.bigpipe;

import play.libs.HttpExecution;
import scala.collection.JavaConverters;
import scala.concurrent.ExecutionContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class composes the given Pagelets together and prepares them for either out-of-order client-side rendering (if
 * renderOptions is set to ClientSide) or in-order server-side rendering (if renderOptions is set to ServerSide). Use
 * the render method in this class in your templates to actually render the Pagelets. It provides you a Map from
 * Pagelet id to the HtmlStream for that Pagelet. Insert the HtmlStream in this Map for each Pagelet into the
 * appropriate part of your markup.
 */
public class BigPipe extends com.ybrikman.ping.scalaapi.bigpipe.BigPipe {

  public BigPipe(PageletRenderOptions renderOptions, List<Pagelet> pagelets, ExecutionContext ec) {
    super(renderOptions, toScalaPagelets(pagelets, ec), ec);
  }

  public BigPipe(PageletRenderOptions renderOptions, List<Pagelet> pagelets) {
    this(renderOptions, pagelets, HttpExecution.defaultContext());
  }

  public BigPipe(PageletRenderOptions renderOptions, Pagelet ... pagelets) {
    this(renderOptions, Arrays.asList(pagelets));
  }

  private static scala.collection.immutable.List<com.ybrikman.ping.scalaapi.bigpipe.Pagelet> toScalaPagelets(List<Pagelet> pagelets, ExecutionContext ec) {
    List<com.ybrikman.ping.scalaapi.bigpipe.Pagelet> scalaPagelets =
        pagelets
          .stream()
          .map(pagelet -> pagelet.wrapped(ec))
          .collect(Collectors.toList());

    return JavaConverters.asScalaBufferConverter(scalaPagelets).asScala().toList();
  }
}
