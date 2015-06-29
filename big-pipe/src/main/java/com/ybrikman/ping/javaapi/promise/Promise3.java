package com.ybrikman.ping.javaapi.promise;

import play.libs.F;

public class Promise3<A, B, C> {

  private final F.Promise<A> a;
  private final F.Promise<B> b;
  private final F.Promise<C> c;

  public Promise3(F.Promise<A> a, F.Promise<B> b, F.Promise<C> c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public <R> F.Promise<R> map(Function3<A, B, C, R> function) {
    return a.flatMap(a -> b.flatMap(b -> c.map(c -> function.apply(a, b, c))));
  }

  public <R> F.Promise<R> flatMap(Function3<A, B, C, F.Promise<R>> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> function.apply(a, b, c))));
  }
}
