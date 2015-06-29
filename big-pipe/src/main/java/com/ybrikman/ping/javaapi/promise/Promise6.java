package com.ybrikman.ping.javaapi.promise;

import play.libs.F;

public class Promise6<A, B, C, D, E, F> {

  private final play.libs.F.Promise<A> a;
  private final play.libs.F.Promise<B> b;
  private final play.libs.F.Promise<C> c;
  private final play.libs.F.Promise<D> d;
  private final play.libs.F.Promise<E> e;
  private final play.libs.F.Promise<F> f;

  public Promise6(play.libs.F.Promise<A> a, play.libs.F.Promise<B> b, play.libs.F.Promise<C> c, play.libs.F.Promise<D> d, play.libs.F.Promise<E> e, play.libs.F.Promise<F> f) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
    this.e = e;
    this.f = f;
  }

  public <R> play.libs.F.Promise<R> map(Function6<A, B, C, D, E, F, R> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.flatMap(d -> e.flatMap(e -> f.map(f -> function.apply(a, b, c, d, e, f)))))));
  }

  public <R> play.libs.F.Promise<R> flatMap(Function6<A, B, C, D, E, F, play.libs.F.Promise<R>> function) {
    return a.flatMap(a -> b.flatMap(b -> c.flatMap(c -> d.flatMap(d -> e.flatMap(e -> f.flatMap(f -> function.apply(a, b, c, d, e, f)))))));
  }
}
