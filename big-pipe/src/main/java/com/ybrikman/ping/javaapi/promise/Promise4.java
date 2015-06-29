package com.ybrikman.ping.javaapi.promise;

import play.libs.F;

public class Promise4<A, B, C, D> {

  private final F.Promise<A> a;
  private final F.Promise<B> b;
  private final F.Promise<C> c;
  private final F.Promise<D> d;

  public Promise4(F.Promise<A> a, F.Promise<B> b, F.Promise<C> c, F.Promise<D> d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public <R> F.Promise<R> map(Function4<A, B, C, D, R> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.map(d -> function.apply(a, b, c, d)))));
  }

  public <R> F.Promise<R> flatMap(Function4<A, B, C, D, F.Promise<R>> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.flatMap(d -> function.apply(a, b, c, d)))));
  }
}
