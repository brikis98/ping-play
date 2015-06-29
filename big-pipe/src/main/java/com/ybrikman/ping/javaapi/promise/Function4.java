package com.ybrikman.ping.javaapi.promise;

@FunctionalInterface
public interface Function4<A, B, C, D, R> {
  public R apply(A a, B b, C c, D d);
}
