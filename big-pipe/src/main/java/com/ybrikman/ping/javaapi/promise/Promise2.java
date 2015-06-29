package com.ybrikman.ping.javaapi.promise;

import play.libs.F;

public class Promise2<A, B> {

  private final F.Promise<A> a;
  private final F.Promise<B> b;

  public Promise2(F.Promise<A> a, F.Promise<B> b) {
    this.a = a;
    this.b = b;
  }

  public <R> F.Promise<R> map(Function2<A, B, R> function) {
    return a.flatMap(a -> b.map(b -> function.apply(a, b)));
  }

  public <R> F.Promise<R> flatMap(Function2<A, B, F.Promise<R>> function) {
    return a.flatMap(a -> b.flatMap(b -> function.apply(a, b)));
  }
}
