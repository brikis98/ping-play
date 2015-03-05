# Ping-Play

This is a sample app for the
[Composable and streamable Play apps](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps) talk at
[Ping Conference 2014](http://www.ping-conf.com/). A recording of the talk, which includes live coding demos with this sample app, is available on [ustream](http://www.ustream.tv/recorded/42801129).

The code shows some examples of how to:

1. Compose standalone endpoints (controllers) to build a more complex page from simpler parts
2. A prototype of [Facebook's BigPipe](https://www.facebook.com/note.php?note_id=389414033919) for Play: that is,
tools to break a page into "pagelets" and stream each pagelet to the browser as soon as the data is available.

This is just prototype code, intended for demonstration and education. There are no tests, so I wouldn't recommend
tossing it directly into production :)

# How to run the app

This is a standard [Play Framework](http://www.playframework.com/) app, built on Play 2.3.3. To run it,
[install Play](http://www.playframework.com/download) and do `activator run`.

# How to browse the code

The main example code to look at:

1. `app/controllers/Wvyp` and `app/controllers/Wvyu` are examples of simple, standalone modules.
2. `app/controllers/Aggregator` shows how these modules can be combined to build a more complicated module.
3. `app/controllers/WvypStream` shows how to stream the contents of a module using a BigPipe approach.
4. `app/ui` a reusable library for composing modules and streaming HTML.

# De-duping service calls

To be able to use standalone endpoints, you need to de-dupe remote service calls, or you'll end up dramatically increasing the load on downstream dependencies. A bunch of people have asked, so here is an outline of how to de-dupe remote service calls in Play: https://gist.github.com/brikis98/761e4fa7404f6b9803bb

# Safely injecting "pagelets"

This method of injecting pagelets in `pagelet.scala.html` is **for demonstration purposes only**. I used it to keep the talk simple and to minimize dependencies in this code, but it's **not** a good idea to shove HTML directly into a script tag:

```html
<!-- Excerpt from pagelet.scala.html -->
@(contents: Html, id: String)

<script type="text/html-stream" id="@id-contents">
  @contents
</script>
```
If `contents` contains a closing `script` tag, your page will break. And if you're not careful about how you render `contents`, you may be opening yourself up for an injection attack.

Instead of injecting server-side rendered HTML, a better approach is to inject JSON and to use a JavaScript templating technology (e.g. mustache, handlebars, dust, react, etc) to turn it into HTML in the browser. 

To inject JSON safely, put it into a `code` tag wrapped in an HTML comment:

```html
<code id="my-data">
<!--{"name": "Jim"}-->
</code>
```

Note that you'll need to escape/unescape the double dash in the JSON so it can't break out of the comment block. You'll probably want an `escapeForEmbedding` function in Scala and an `unescapeForEmbedding` function in JavaScript, but I've written both in  JavaScript just to keep things simple:

```javascript
var ESC_FLAGS = "gi";
var HTML_ENTITY = {
  dsh: { escaped: '\\u002d\\u002d', unescaped: '--', escaped_re: '\\\\u002d\\\\u002d' }
};

function escapeForEmbedding(str) {
  return str.replace(new RegExp(HTML_ENTITY.dsh.unescaped, ESC_FLAGS), HTML_ENTITY.dsh.escaped);
}

function unescapeForEmbedding(str) {
  return str.replace(new RegExp(HTML_ENTITY.dsh.escaped_re, ESC_FLAGS), HTML_ENTITY.dsh.unescaped);
}
```  

You can then use JavaScript to read the contents of the `code` block, strip the comments, and use `JSON.parse` to safely parse the JSON without exposing yourself to any sort of injection attack:

```javascript
function parseEmbeddedJson(domId) {
  var contentElem = document.getElementById(domId);
  var innerContent = contentElem.firstChild.nodeValue;
  contentElem.parentNode.removeChild(contentElem);
  return JSON.parse(unescapeForEmbedding(innerContent));
}
```

You can pass this JSON to your favorite templating technology, render it, and inject it into the DOM. Here's an example using [Handlebars.js](http://handlebarsjs.com/):

```javascript
// You probably want to store your template in an external file
var template = "Hello {{name}}"; 
var json = parseEmbeddedJson("my-data");
var html = Mustache.render(template, json);
document.getElementById("some-dom-node").innerHTML = html;
```

Putting this all together, a better version of `pagelet.scala.html` would look something like this:

```html
@(data: Json, id: String)

<code id="@id-data">
  <!--@escapeForEmbedding(Json.stringify(data))-->
</script>

<script type="text/javascript">
  // You probably want to store your template in an external file
  var template = "Hello {{name}}"; 
  var json = parseEmbeddedJson("@id-data");
  var html = Mustache.render(template, json);
  document.getElementById("@id").innerHTML = html;
</script>
```

# License

This code is available under the MIT license. See the LICENSE file for more info.
