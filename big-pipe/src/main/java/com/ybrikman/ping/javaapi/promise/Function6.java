package com.ybrikman.ping.javaapi.promise;

@FunctionalInterface
public interface Function6<A, B, C, D, E, F, R> {
  public R apply(A a, B b, C c, D d, E e, F f);
}
