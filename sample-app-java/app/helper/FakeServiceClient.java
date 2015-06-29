package helper;

import akka.actor.ActorSystem;
import data.FutureUtil;
import data.Response;
import play.libs.F;

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
}
