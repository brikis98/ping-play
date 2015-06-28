/**
 * JavaScript helper functions for BigPipe-style streaming. This code has no external dependencies.
 */
(function() {
  "use strict";

  var JSON_CONTENT_TYPE = "json";
  var HTML_CONTENT_TYPE = "html";
  var TEXT_CONTENT_TYPE = "text";

  var root = this;
  var BigPipe = root.BigPipe = {};
  var document = root.document;
  var console = root.console;

  var log = function(msg) {
    if (console && console.log) {
      console.log(msg);
    }
  };

  BigPipe.unescapeForEmbedding = function(str) {
    if (str) {
      return str.replace(new RegExp('\\\\u002d\\\\u002d', "gi"), '--');
    } else {
      return str;
    }
  };

  BigPipe.readEmbeddedContentFromDom = function(domId) {
    var contentElem = document.getElementById(domId);
    if (contentElem) {
      return BigPipe.unescapeForEmbedding(contentElem.firstChild.nodeValue);
    } else {
      log("ERROR: Unable to read content from DOM node with id " + domId + " so return an empty String.");
      return "";
    }
  };

  BigPipe.parseEmbeddedJsonFromDom = function(domId) {
    var content = BigPipe.readEmbeddedContentFromDom(domId);
    return JSON.parse(content);
  };

  BigPipe.renderPagelet = function(id, content) {
    var domElement = document.getElementById(id);
    if (domElement) {
      domElement.innerHTML = content;
    } else {
      log("ERROR: cannot insert pagelet content because DOM node with id " + id + " does not exist");
    }
  };

  BigPipe.onPagelet = function(id, contentId, contentType) {
    if (contentType === JSON_CONTENT_TYPE) {
      var json = BigPipe.parseEmbeddedJsonFromDom(contentId);
      BigPipe.renderPagelet(id, json);
    } else if (contentType === HTML_CONTENT_TYPE || contentType === TEXT_CONTENT_TYPE) {
      var content = BigPipe.readEmbeddedContentFromDom(contentId);
      BigPipe.renderPagelet(id, content);
    } else {
      log("ERROR: unsupported contentType " + contentType + " for pagelet with id " + id);
    }
  };
}.call(this));