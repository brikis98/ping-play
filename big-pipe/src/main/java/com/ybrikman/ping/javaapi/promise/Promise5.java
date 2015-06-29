package com.ybrikman.ping.javaapi.promise;

import play.libs.F;

public class Promise5<A, B, C, D, E> {

  private final F.Promise<A> a;
  private final F.Promise<B> b;
  private final F.Promise<C> c;
  private final F.Promise<D> d;
  private final F.Promise<E> e;

  public Promise5(F.Promise<A> a, F.Promise<B> b, F.Promise<C> c, F.Promise<D> d, F.Promise<E> e) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
    this.e = e;
  }

  public <R> F.Promise<R> map(Function5<A, B, C, D, E, R> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.flatMap(d -> e.map(e -> function.apply(a, b, c, d, e))))));
  }

  public <R> F.Promise<R> flatMap(Function5<A, B, C, D, E, F.Promise<R>> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.flatMap(d -> e.flatMap(e -> function.apply(a, b, c, d, e))))));
  }
}
