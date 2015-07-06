package com.ybrikman.ping.javaapi.bigpipe;

/**
 * Specify the type of rendering you wish to use with BigPipe: either client-side, out-of-order rendering, which
 * gives the minimal load time for your page, but requires JavaScript, or server-side, in-order rendering, which has
 * a higher load time (albeit still faster than not using BigPipe at all), but does not rely on JavaScript.
 */
public enum PageletRenderOptions {
  ClientSide,
  ServerSide
}
