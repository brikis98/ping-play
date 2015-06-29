package com.ybrikman.ping.javaapi.promise;

import play.libs.F.Promise;

public class PromiseHelper {

  public static <A, B> Promise2<A, B> sequence(Promise<A> a, Promise<B> b) {
    return new Promise2<>(a, b);
  }

  public static <A, B, C> Promise3<A, B, C> sequence(Promise<A> a, Promise<B> b, Promise<C> c) {
    return new Promise3<>(a, b, c);
  }

  public static <A, B, C, D> Promise4<A, B, C, D> sequence(Promise<A> a, Promise<B> b, Promise<C> c, Promise<D> d) {
    return new Promise4<>(a, b, c, d);
  }

  public static <A, B, C, D, E> Promise5<A, B, C, D, E> sequence(Promise<A> a, Promise<B> b, Promise<C> c, Promise<D> d, Promise<E> e) {
    return new Promise5<>(a, b, c, d, e);
  }

  public static <A, B, C, D, E, F> Promise6<A, B, C, D, E, F> sequence(Promise<A> a, Promise<B> b, Promise<C> c, Promise<D> d, Promise<E> e, Promise<F> f) {
    return new Promise6<>(a, b, c, d, e, f);
  }
}










