package com.ybrikman.ping.javaapi.bigpipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import play.libs.F.Promise;
import play.libs.HttpExecution;
import play.libs.Json;
import play.twirl.api.BufferedContent;
import play.twirl.api.Html;
import scala.concurrent.ExecutionContext;

/**
 * Create a "pagelet" that can be sent down the browser as soon as the given content is available and rendered
 * client-side into the correct spot on the page, as identified by the given DOM id. This is done by:
 *
 * 1. Wrapping the given content in HTML markup that will not be visible when the browser first processes it
 * 2. Adding JavaScript that will extract the hidden content and insert it into the proper location in the DOM
 *
 * Use the Pagelet.fromXXX methods to create Pagelets from a variety of types. Use the Pagelet.asHtmlXXX methods to
 * get the HTML/JS code that you should insert into your page.
 */
public class Pagelet {

  private final Promise<String> content;
  private final String id;
  private final PageletContentType contentType;
  private final ExecutionContext ec;

  public Pagelet(Promise<String> content, String id, PageletContentType contentType, ExecutionContext ec) {
    this.content = content;
    this.id = id;
    this.contentType = contentType;
    this.ec = ec;
  }

  public Promise<String> getContent() {
    return content;
  }

  public String getId() {
    return id;
  }

  public PageletContentType getContentType() {
    return contentType;
  }

  public ExecutionContext getEc() {
    return ec;
  }

  public Promise<Html> asHtmlPromise() {
    return Promise.wrap(asScalaPagelet().asHtmlFuture());
  }

  public HtmlStream asHtmlStream() {
    return HtmlStreamHelper.fromHtmlPromise(asHtmlPromise());
  }

  public static Pagelet fromStringPromise(Promise<String> content, String id, ExecutionContext ec) {
    return new Pagelet(content, id, PageletContentType.html, ec);
  }

  public static Pagelet fromStringPromise(Promise<String> content, String id) {
    return fromStringPromise(content, id, HttpExecution.defaultContext());
  }

  public static Pagelet fromHtmlPromise(Promise<Html> content, String id, ExecutionContext ec) {
    return new Pagelet(content.map(BufferedContent::body, ec), id, PageletContentType.html, ec);
  }

  public static Pagelet fromHtmlPromise(Promise<Html> content, String id) {
    return fromHtmlPromise(content, id, HttpExecution.defaultContext());
  }

  public static Pagelet fromJsonPromise(Promise<JsonNode> content, String id, ExecutionContext ec) {
    return new Pagelet(content.map(Json::stringify, ec), id, PageletContentType.json, ec);
  }

  public static Pagelet fromJsonPromise(Promise<JsonNode> content, String id) {
    return fromJsonPromise(content, id, HttpExecution.defaultContext());
  }

  public static Pagelet fromHtml(Html content, String id, ExecutionContext ec) {
    return fromHtmlPromise(Promise.pure(content), id, ec);
  }

  public static Pagelet fromHtml(Html content, String id) {
    return fromHtml(content, id, HttpExecution.defaultContext());
  }

  public static Pagelet fromString(String content, String id, ExecutionContext ec) {
    return fromStringPromise(Promise.pure(content), id, ec);
  }

  public static Pagelet fromString(String content, String id) {
    return fromString(content, id, HttpExecution.defaultContext());
  }

  public static Pagelet fromJson(JsonNode content, String id, ExecutionContext ec) {
    return fromJsonPromise(Promise.pure(content), id, ec);
  }

  public static Pagelet fromJson(JsonNode content, String id) {
    return fromJson(content, id, HttpExecution.defaultContext());
  }

  private com.ybrikman.ping.scalaapi.bigpipe.Pagelet asScalaPagelet() {
    return new com.ybrikman.ping.scalaapi.bigpipe.Pagelet(content.wrapped(), id, contentType, ec);
  }
}
