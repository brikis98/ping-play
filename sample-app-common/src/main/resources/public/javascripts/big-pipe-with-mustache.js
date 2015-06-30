(function(window, mustache, BigPipe) {
  "use strict";

  var document = window.document;
  var console = window.console;

  // In a real app, you'd probably want to store the template in an external file and not inline it like this.
  var template =
      '<div class="module">' +
        '<h3 class="id">{{ id }}</h3>' +
        '<h6>took</h6>' +
        '<h2 class="highlight">{{ delay }} ms</h2>' +
        '<h6>to respond</h6>' +
       '</div>';

  // Override the original BigPipe.renderPagelet method with one that uses mustache.js for client-side rendering
  BigPipe.renderPagelet = function(id, json) {
    var domElement = document.getElementById(id);
    if (domElement) {
      domElement.innerHTML = Mustache.render(template, json);
    } else {
      console.log("ERROR: cannot render pagelet because DOM node with id " + id + " does not exist");
    }
  };

})(window, Mustache, BigPipe);