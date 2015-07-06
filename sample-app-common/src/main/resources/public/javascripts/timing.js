// Quick, hacky code used to display page load timing using the Navigation Timing API
(function(window) {
  "use strict";

  var document = window.document;

  if (window.performance && window.performance.timing) {
    var timing = window.performance.timing;

    var timeToFirstByte = timing.responseStart - timing.requestStart;
    var timeToDomLoading = timing.domLoading - timing.requestStart;

    document.getElementById("time-to-first-byte").innerHTML = timeToFirstByte + "ms";
    document.getElementById("time-to-dom-loading").innerHTML = timeToDomLoading + "ms";
  } else {
    document.getElementById("time-to-first-byte").innerHTML = "Navigation Timing API not supported in this browser"
    document.getElementById("time-to-dom-loading").innerHTML = "Navigation Timing API not supported in this browser"
  }
})(window);