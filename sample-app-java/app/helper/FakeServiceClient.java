package helper;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import data.FutureUtil;
import data.Response;
import play.api.libs.json.JsValue;
import play.libs.F;
import play.libs.Json;

import javax.inject.Inject;

public class FakeServiceClient {
  private final data.FakeServiceClient delegate;

  @Inject
  public FakeServiceClient(ActorSystem actorSystem) {
    delegate = new data.FakeServiceClient(new FutureUtil(actorSystem));
  }

  public F.Promise<Response> fakeRemoteCallFast(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallFast(id));
  }

  public F.Promise<Response> fakeRemoteCallMedium(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallMedium(id));
  }

  public F.Promise<Response> fakeRemoteCallSlow(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallSlow(id));
  }

  public F.Promise<Response> fakeRemoteCall(String id, long delayInMillis) {
    return F.Promise.wrap(delegate.fakeRemoteCall(id, delayInMillis));
  }

  public F.Promise<JsonNode> fakeRemoteCallJsonFast(String id) {
    return toJsonNode(F.Promise.wrap(delegate.fakeRemoteCallJsonFast(id)));
  }

  public F.Promise<JsonNode> fakeRemoteCallJsonMedium(String id) {
    return toJsonNode(F.Promise.wrap(delegate.fakeRemoteCallJsonMedium(id)));
  }

  public F.Promise<JsonNode> fakeRemoteCallJsonSlow(String id) {
    return toJsonNode(F.Promise.wrap(delegate.fakeRemoteCallJsonSlow(id)));
  }

  public F.Promise<JsonNode> fakeRemoteCallJson(String id, long delayInMillis) {
    return toJsonNode(F.Promise.wrap(delegate.fakeRemoteCallJson(id, delayInMillis)));
  }

  private F.Promise<JsonNode> toJsonNode(F.Promise<JsValue> jsValuePromise) {
    return jsValuePromise.map(jsValue -> Json.parse(jsValue.toString()));
  }

  public F.Promise<Response> fakeRemoteCallErrorFast(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallErrorFast(id));
  }

  public F.Promise<Response> fakeRemoteCallErrorMedium(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallErrorMedium(id));
  }

  public F.Promise<Response> fakeRemoteCallErrorSlow(String id) {
    return F.Promise.wrap(delegate.fakeRemoteCallErrorSlow(id));
  }

  public F.Promise<Response> fakeRemoteCallError(String id, long delayInMillis) {
    return F.Promise.wrap(delegate.fakeRemoteCallError(id, delayInMillis));
  }
}
