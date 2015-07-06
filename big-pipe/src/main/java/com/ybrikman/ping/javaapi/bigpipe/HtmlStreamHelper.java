package com.ybrikman.ping.javaapi.bigpipe;

import com.ybrikman.ping.scalaapi.bigpipe.HtmlStream;
import com.ybrikman.ping.scalaapi.bigpipe.JavaAdapter;
import play.api.http.ContentTypeOf;
import play.api.http.Writeable;
import play.api.libs.iteratee.Enumerator;
import play.api.mvc.Codec;
import play.libs.F.Promise;
import play.libs.HttpExecution;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import scala.collection.JavaConverters;
import scala.concurrent.ExecutionContext;

import java.util.Arrays;
import java.util.List;

public class HtmlStreamHelper {

  public static HtmlStream empty() {
    return HtmlStream.empty();
  }

  public static HtmlStream fromString(String str) {
    return HtmlStream.fromString(str);
  }

  public static HtmlStream fromHtml(Html html) {
    return HtmlStream.fromHtml(html);
  }

  public static HtmlStream fromHtmlEnumerator(Enumerator<Html> enumerator) {
    return HtmlStream.fromHtmlEnumerator(enumerator);
  }

  public static HtmlStream fromHtmlPromise(Promise<Html> html) {
    return fromHtmlPromise(html, HttpExecution.defaultContext());
  }

  public static HtmlStream fromHtmlPromise(Promise<Html> html, ExecutionContext ec) {
    return HtmlStream.fromHtmlFuture(html.wrapped(), ec);
  }

  public static HtmlStream fromResult(Result result) {
    return fromResult(result, HttpExecution.defaultContext(), Codec.utf_8());
  }

  public static HtmlStream fromResult(Result result, ExecutionContext ec, Codec codec) {
    return HtmlStream.fromResult(result.toScala(), ec, codec);
  }

  public static HtmlStream fromResultPromise(Promise<Result> result) {
    return fromResultPromise(result, HttpExecution.defaultContext());
  }

  public static HtmlStream fromResultPromise(Promise<Result> result, ExecutionContext ec) {
    return HtmlStream.fromResultFuture(result.map(Result::toScala, ec).wrapped(), ec);
  }

  public static HtmlStream flatten(Promise<HtmlStream> stream) {
    return flatten(stream, HttpExecution.defaultContext());
  }

  public static HtmlStream flatten(Promise<HtmlStream> stream, ExecutionContext ec) {
    return HtmlStream.flatten(stream.wrapped(), ec);
  }

  public static HtmlStream interleave(HtmlStream ... streams) {
    return interleave(Arrays.asList(streams));
  }

  public static HtmlStream interleave(List<HtmlStream> streams) {
    return HtmlStream.interleave(JavaConverters.asScalaBufferConverter(streams).asScala());
  }

  public static Results.Chunks<Html> toChunks(HtmlStream stream) {
    return toChunks(stream, Codec.utf_8(), HttpExecution.defaultContext());
  }

  public static Results.Chunks<Html> toChunks(HtmlStream stream, Codec codec, ExecutionContext executionContext) {
    return new Results.Chunks<Html>(Writeable.writeableOf_Content(codec, ContentTypeOf.contentTypeOf_Html(codec))) {
      @Override
      public void onReady(Out<Html> out) {
        JavaAdapter.writeEnumeratorToOut(stream.enumerator(), out, executionContext);
      }
    };
  }
}
