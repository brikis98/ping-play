# Ping-Play

The ping-play project brings [BigPipe](https://www.facebook.com/note.php?note_id=389414033919) style streaming to 
[Play Framework](http://playframework.com/) applications. It includes tools for splitting your pages up into small
"pagelets" and support for streaming those pagelets down to the browser as soon as they are ready, which can 
significantly reduce page load time.

# Quick start

*Pre-requisite: this project requires Play 2.4, Scala 2.11.6, and Java 8.*

**Note: these artifacts are not yet published!**

To add BigPipe streaming to your own pages, first, add the big-pipe dependency to your build:

```scala
libraryDependencies += "com.ybrikman.ping" %% "big-pipe" % "0.0.1"`
```

Next, add support for the `.scala.stream` template type and some imports for it to your build:

```scala
TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat"),
TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream")
```

Now you can create streaming templates. These templates can mix normal HTML markup, which will be streamed to the 
browser immediately, with the `HtmlStream` class, which is a wrapper for an `Enumerator[Html]` that will be streamed
to the browser whenever the `Enumerator` has data. 

For example, here is a streaming template called `app/views/bigPipeExample.scala.stream`:

```html
@(body: HtmlStream)

<html>
  <head>
    <script src="@routes.Assets.at("com/ybrikman/bigpipe/big-pipe.js")"></script>
  </head>
  <body>
    <div id="profile-placeholder"></div>
    <div id="graph-placeholder"></div>
    <div id="feed-placeholder"></div>

    @body
  </body>
</html>
```

The key things to notice in this template are:

1. It includes several placeholder `div` elements, each of which will be filled in incrementally by pagelets as they
   arrive from the server.
2. It includes the `big-pipe.js` JavaScript file, which knows how to render pagelets client-side as they arrive.
3. It includes an `HtmlStream` called `body` at the bottom of the page. This is where all the pagelets will go as they
   fill in.

Now, let's look at the controller we use with this template, called `app/controllers/BigPipeExample.scala`:

```scala
class BigPipeExample extends Controller {
  def index = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallSlow("feed")

    // Convert each Future into a Pagelet which will be rendered as HTML as soon as the data is available
    val profile = Pagelet.fromHtmlFuture(profileFuture.map(views.html.profile.apply), "profile-placeholder")
    val graph = Pagelet.fromHtmlFuture(graphFuture.map(views.html.graph.apply), "graph-placeholder")
    val feed = Pagelet.fromHtmlFuture(feedFuture.map(views.html.feed.apply), "feed-placeholder")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed)

    // Render the streaming template immediately
    Ok.chunked(views.stream.bigPipeExample(body))
  }
}
```

The key things to notice in this controller are:

1. It makes 3 fake service calls in parallel to represent calls to your backend services to fetch data.
2. When each piece of data redeems, it will be rendered as HTML using its own template (e.g. `views.html.profile`).
3. This HTML wrapped in a `Pagelet` that knows the name of the placeholder `div` where that HTML should be inserted 
   when it arrives in the browser.
4. It composes all the pagelets into a single `HtmlStream` that can stream down the `Pagelets` in whatever order they
   complete.
5. It uses the `bigPipeExample.scala.stream` template from above to render all of this.

To see this example in action, take a look at the sample Java or Scala apps (yes, BigPipe streaming works with both!).
For example, to see the Scala sample app, you would:

1. `git clone` this repo.
2. `activator shell`
3. `project sampleAppScala`
4. `run`
5. Open `http://localhost:9000/withoutBigPipe` to see how long the page takes to load without BigPipe streaming.
6. Open `http://localhost:9000/withBigPipe` to see how much faster the page loads with BigPipe streaming.

Check out [Background](#Background) to get a better understanding of how BigPipe works and 
[Documentation](#Documentation) to see what libraries and APIs are available. 

# Background

## Why BigPipe?

To fetch the data for a page, modern apps often have to make requests to multiple remote backend services (e.g. RESTful
HTTP calls to a profile service, a search service, an ads service, etc). You then have to wait for *all* of these 
remote calls to come back before you can send *any* data back to the browser. For example, the following screenshot 
shows a page that makes 6 remote service calls, most of which complete in few hundred milliseconds, but one takes 
nearly 4 seconds. As a result, the time to first byte is nearly 4 seconds (during which the user sees a completely 
blank page) and then the page finally starts rendering:

![Page loading without BigPipe](/images/without-big-pipe.png)

With BigPipe style streaming, you can start streaming data back to the browser without waiting for the backends at all, 
and fill in the page on the fly as each backend responds. For example, the following screenshot shows the same page 
rendered using BigPipe, where the header and much of the markup was sent back instantly, so time to first byte was 4 
milliseconds instead of 4 seconds, static content (i.e. CSS, JS, images) could start loading right away, and then as 
each backend service responded, the corresponding part of the page (i.e. the pagelet) was sent to the browser and
rendered on the screen incrementally:

![Page loading with BigPipe](/images/with-big-pipe.png)

## Why not AJAX or iframes?

You could try to accomplish something similar to BigPipe by sending back a page that's empty and makes lots of AJAX 
calls or uses iframes to fill in each pagelet. This approach is much slower than BigPipe for a number of reasons: 

1. Each AJAX call requires an extra roundtrip to your server, which adds a lot of latency. This latency is especially
   bad on mobile or slower connections.
2. Each extra roundtrip also increases the load on your server. Instead of 1 QPS to load a page, you now have 6 QPS to
   load a page with 6 pagelets.
3. Older browsers severly limit how many AJAX calls you can do and most browsers give AJAX calls a low priority during
   the initial page load.
4. You have to download, parse, and execute a bunch of JavaScript code before you can even make the AJAX calls. 

BigPipe gives you all the benefits of an AJAX portal, but without the downsides, by using a single connection&mdash;that
is, the original connection used to request the page&mdash;and streaming down each pagelet using 
[HTTP Chunked Encoding](https://en.wikipedia.org/wiki/Chunked_transfer_encoding), which works in almost all browsers.

## Further reading

1. [Composable and Streamable Play Apps](https://engineering.linkedin.com/play/composable-and-streamable-play-apps): 
   a talk that introduces how BigPipe streaming works on top of Play (see the 
   [video](https://www.youtube.com/watch?v=4b1XLka0UIw) and 
   [slides](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps). 
2. [BigPipe: Pipelining web pages for high performance](https://www.facebook.com/note.php?note_id=389414033919): the
   original blog post by Facebook that introduces BigPipe on PHP.
3. [New technologies for the new LinkedIn home page](http://engineering.linkedin.com/frontend/new-technologies-new-linkedin-home-page):
   the new LinkedIn homepage is using BigPipe style streaming. This ping-play project is loosely based off of the work 
   done originally at LinkedIn. 

# Documentation

## Scala vs Java

BigPipe streaming is supported for both Scala and Java developers.

Scala developers should primarily be using classes in the `com.ybrikman.ping.scalaapi` package. In particular, use the
`com.ybrikman.ping.scalaapi.Pagelet` class to wrap your `Html` and `Future[Html]` as `Pagelet` objects, and use the
`com.ybrikman.ping.scalaapi.HtmlStream` class to combine `Pagelet` objects into an `HtmlStream`.

Java developers should primarily be using classes in the `com.ybrikman.ping.javaapi` package. In particular, use the
`com.ybrikman.ping.javaapi.Pagelet` class to wrap your `Html` and `Promise<Html>` as `Pagelet` objects and use the
`com.ybrikman.ping.javaapi.HtmlStreamHelper` class to combine `Pagelet` objects into an `HtmlStream`.  
 
## Controlling client-side rendering

Each pagelet consists of the content, wrapped in `code` tag and an HTML comment so that it is ignored by the browser,
and some JavaScript code telling the `big-pipe.js` library to process it. It looks roughly like this:

```html
<code id="pagelet1"><!--<p>Some content</p>--></code>
<script>BigPipe.onPagelet("pagelet1");</script>
```

The `BigPipe.onPagelet` method will extract the content from the `code` tag and call `BigPipe.renderPagelet` to render
it client-side into a placeholder `div`. The default `BigPipe.renderPagelet` just inserts the content into the `div`
using the `innerHTML` method. If you wish to use a more sophisticated method for client-side rendering, simply override
the `BigPipe.renderPagelet` with your own:

```javascript
BigPipe.renderPagelet = function(id, content) {
  // Provide a custom way to insert the specified content into the DOM node with the given id
}
```

For example, you could send down JSON as your content, and then use a client-side rendering technology, such as 
mustache.js or handlebars.js to render it in the browser. To do that, all you need to do is create a `Pagelet` that
contains a `JsValue` (for Scala developers) or `JsonNode` (for Java developers):

```scala
def index = Action {
  // Make several fake service calls in parallel and get back a Future[JsValue] from each one 
  val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
  val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
  val feedFuture = serviceClient.fakeRemoteCallSlow("feed")

  // Convert each Future[JsValue] into a Pagelet
  val profile = Pagelet.fromJsonFuture(profileFuture, "profile-placeholder")
  val graph = Pagelet.fromJsonFuture(graphFuture, "graph-placeholder")
  val feed = Pagelet.fromJsonFuture(feedFuture, "feed-placeholder")

  // Compose all the pagelets into an HtmlStream
  val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed)

  // Render the streaming template immediately
  Ok.chunked(views.stream.bigPipeExample(body))
}
```

Next, create your custom `BigPipe.renderPagelet` method:

```javascript
BigPipe.renderPagelet = function(id, content) {
  var myTemplate = "Hello {{ name }}";
  var html = Mustache.render(myTemplate, content);
  document.getElementByid(id, html);
}
```

Each `Pagelet` will stream down the JSON as soon as it's available and will call your `BigPipe.renderPagelet` method
with `content` already parsed as JSON for you.

## Composable pagelets

TODO: write documentation

## De-duping remote service calls

TODO: write documentation

# License

This code is available under the MIT license. See the LICENSE file for more info.
