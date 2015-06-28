/**
 * JavaScript helper functions for BigPipe-style streaming. This code has no external dependencies.
 */
(function() {
  "use strict";

  // Establish the root object, `window` in the browser, or `exports` on the server.
  var root = this;

  var ESC_FLAGS = "gi";
  var HTML_ENTITY = {
    dsh: { escaped: '\\u002d\\u002d', unescaped: '--', escaped_re: '\\\\u002d\\\\u002d' }
  };

  var JSON_CONTENT_TYPE = "json";
  var HTML_CONTENT_TYPE = "html";
  var TEXT_CONTENT_TYPE = "text";

  var log = function(msg) {
    if (root.console && root.console.log) {
      root.console.log(msg);
    }
  };

  /**
   * Convert the given String into an HTML String that can be safely embedded into a webpage in a way that the browser
   * completely ignores it.
   *
   * @param str
   * @returns string
   */
  var escapeForEmbedding = function(str) {
    return str.replace(new RegExp(HTML_ENTITY.dsh.unescaped, ESC_FLAGS), HTML_ENTITY.dsh.escaped);
  };

  /**
   * Unescape the given String
   *
   * @param str
   * @returns string
   */
  var unescapeForEmbedding = function(str) {
    return str.replace(new RegExp(HTML_ENTITY.dsh.escaped_re, ESC_FLAGS), HTML_ENTITY.dsh.unescaped);
  };

  var readEmbeddedContentFromDom = function(domId) {
    var contentElem = document.getElementById(domId);
    if (contentElem) {
      return contentElem.firstChild.nodeValue;
    } else {
      log("ERROR: Unable to read content from DOM node with id " + domId + " so return an empty String.");
      return "";
    }
  };

  var parseEmbeddedJsonFromDom = function(domId) {
    var content = readEmbeddedContentFromDom(domId);
    return JSON.parse(unescapeForEmbedding(content));
  };

  var renderPagelet = function(id, content) {
    var domElement = document.getElementById(id);
    if (domElement) {
      domElement.innerHTML = content;
    } else {
      log("ERROR: cannot insert pagelet content because DOM node with id " + id + " does not exist");
    }
  };

  var onPagelet = function(id, contentId, contentType) {
    if (contentType === JSON_CONTENT_TYPE) {
      var json = parseEmbeddedJsonFromDom(contentId);
      renderPagelet(id, json);
    } else if (contentType === HTML_CONTENT_TYPE || contentType === TEXT_CONTENT_TYPE) {
      var content = readEmbeddedContentFromDom(contentId);
      renderPagelet(id, content);
    } else {
      log("ERROR: unsupported contentType " + contentType + " for pagelet with id " + id);
    }
  };

  root.BigPipe = {
    escapeForEmbedding: escapeForEmbedding,
    unescapeForEmbedding: unescapeForEmbedding,
    readEmbeddedContentFromDom: readEmbeddedContentFromDom,
    parseEmbeddedJsonFromDom: parseEmbeddedJsonFromDom,
    onPagelet: onPagelet,
    renderPagelet: renderPagelet
  };
}.call(this));