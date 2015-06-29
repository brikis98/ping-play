package com.ybrikman.ping.javaapi.promise;

@FunctionalInterface
public interface Function5<A, B, C, D, E, R> {
  public R apply(A a, B b, C c, D d, E e);
}