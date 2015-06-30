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
libraryDependencies += "com.ybrikman.ping" %% "big-pipe" % "0.0.9"
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
3. This HTML is wrapped in a `Pagelet` that knows the name of the placeholder `div` where that HTML should be inserted 
   when it arrives in the browser.
4. It composes all the pagelets into a single `HtmlStream` that can stream down the `Pagelets` in whatever order they
   complete.
5. It uses the `bigPipeExample.scala.stream` template from above to render all of this.

To see this example in action, take a look at sample-app-scala or sample-app-java (yes, BigPipe streaming works with 
both Scala and Java) as well as sample-app-common (which has some code shared by both sample apps, including all the
templates). For example, here is how to run the Scala sample app (assuming you have 
[Typesafe Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) installed already):

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
rendered using BigPipe, where the header and much of the markup is sent back instantly, so time to first byte is 4 
milliseconds instead of 4 seconds, static content (i.e. CSS, JS, images) can start loading right away, and then as 
each backend service responds, the corresponding part of the page (i.e. the pagelet) are sent to the browser and
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
   [slides](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps)). 
2. [BigPipe: Pipelining web pages for high performance](https://www.facebook.com/note.php?note_id=389414033919): the
   original blog post by Facebook that introduces BigPipe on PHP.
3. [New technologies for the new LinkedIn home page](http://engineering.linkedin.com/frontend/new-technologies-new-linkedin-home-page):
   the new LinkedIn homepage is using BigPipe style streaming with Play. This ping-play project is loosely based off of 
   the work done originally at LinkedIn. 

# Documentation

## Scala vs Java

BigPipe streaming is supported for both Scala and Java developers.

Scala developers should primarily be using classes in the `com.ybrikman.ping.scalaapi` package. In particular, use the
`com.ybrikman.ping.scalaapi.Pagelet` class to wrap your `Html` and `Future[Html]` as `Pagelet` objects, and use the
`com.ybrikman.ping.scalaapi.HtmlStream` class to combine `Pagelet` objects into an `HtmlStream`. See 
`sample-app-scala` for examples.

Java developers should primarily be using classes in the `com.ybrikman.ping.javaapi` package. In particular, use the
`com.ybrikman.ping.javaapi.Pagelet` class to wrap your `Html` and `Promise<Html>` as `Pagelet` objects and use the
`com.ybrikman.ping.javaapi.HtmlStreamHelper` class to combine `Pagelet` objects into an `HtmlStream`. See 
`sample-app-java` for examples.  

## HtmlStream and .scala.stream templates

Play's built-in `.scala.html` templates are compiled into functions that append together and return `Html`, which is 
just a wrapper for a `StringBuilder`, and cannot be streamed. This is why this project introduces a new `.scala.stream` 
template that appends together and returns `HtmlStream` objects, which are a wrapper for an `Enumerator[Html]`, which 
can be streamed. Note that this new template type still uses Play's [Twirl](https://github.com/playframework/twirl) 
template compiler and its syntax. The only things that are different are:
 
1. The extension is `.scala.stream` instead of `.scala.html`.
2. When you are using the template in a controller, the package name will be `views.stream.XXX` instead of 
   `views.html.XXX`.
3. To include raw, unescaped HTML, instead of wrapping the content in an `Html` object (e.g. 
   `Html(someStringWithMarkup)`), wrap it in an `HtmlStream` object (e.g. `HtmlStream.fromString(someStringWithMarkup)`).
4. You can include an `HtmlStream` object anywhere in the markup of a `.scala.stream` template and Play will stream the 
   content down from the `HtmlStream`'s `Enumerator` whenever the content is available.

The last point is how you get BigPipe style streaming. The `HtmlStream` class has many helper methods to create an
`HtmlStream`, including `fromHtml` and `fromHtmlFuture`, and to compose several streams into one, such as `interleave`.  

## Pagelet

Although you can use the `HtmlStream` class directly, this project also comes with a `Pagelet` class that not only 
helps you stream data back to the browser, but stream that data in any order, and still have it render correctly 
client-side. The idea is to break your page down into small "pagelets" that know how to fetch their own data 
independently and render themselves. For example, you might have one pagelet that fetches data from a profile service 
and knows how to render a user's profile, another pagelet that fetches data from an ads service and knows how to render
an ad unit, and so on. For each pagelet, you make your backend calls, get back some `Future` (Scala) or `Promise` (Java) 
objects, render them into a `Future[Html]` or `Promise<Html>`, and then use `Pagelet.fromHtmlFuture` or 
`Pagelet.fromHtmlPromise` to wrap them in a `Pagelet` class. You can then compose `Pagelet` instances together using 
`HtmlStream.fromInterleavedPagelets`.

To support out-of-order rendering, the `Pagelet` class wraps your content in markup that is invisible when it first 
arrives in the browser, plus some JavaScript that knows how to extract the content and inject it into the right place
in the DOM. The markup sent back by each `Pagelet` will look roughly like this:

```html
<code id="pagelet1"><!--Your content--></code>
<script>BigPipe.onPagelet("pagelet1");</script>
```

The `BigPipe.onPagelet` method is part of `big-pipe.js`, so make sure to include that script on every page.
 
## big-pipe.js

The `BigPipe.onPagelet` method will extract the content from the `code` tag and call `BigPipe.renderPagelet` to render
it client-side into the DOM node with the specified id (e.g. `pagelet1` in the example above). The default 
`BigPipe.renderPagelet` just inserts the content into the DOM using the `innerHTML` method. If you wish to use a more 
sophisticated method for client-side rendering, simply override the `BigPipe.renderPagelet` with your own:

```javascript
BigPipe.renderPagelet = function(id, content) {
  // Provide a custom way to insert the specified content into the DOM node with the given id
}
```

The `id` parameter will be the id of the DOM node and `content` will be your content. Note that if your content was 
JSON instead of HTML, `big-pipe.js` will automatically call `JSON.parse` on it before passing it to you. This can be
convenient if you use client-side templating.

## Client-side templating

You can use a client-side templating technology, such as mustache.js or handlebars.js to render most of your page 
in the browser. To do that, all you need to do is create a `Pagelet` that contains JSON (a `JsValue` for Scala 
developers or `JsonNode` for Java developers) instead of HTML:

```scala
def index = Action {
  // Make several fake service calls in parallel and get back JSON (a Future[JsValue]) from each one 
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

## Composing independent pagelets

TODO: write documentation

## De-duping remote service calls

If your page is built out of composable, independent pagelets, then each pagelet will know how to fetch all the data it
needs from backend services. If each pagelet is truly independent, that means you may have duplicated service calls.
For example, several pagelets may make the exact same backend call to fetch the current user's profile. This is 
inefficient and increases the load on downstream services.

This project comes with a `DedupingCache` library that makes it easy to *de-dupe* service calls. You can use it to 
ensure that if several pagelets request the exact same data, you only make one call to a backend service, and all the 
other calls get the same cached response. This class has a single method called `get` that takes a key and a way to
generate the value for that key if it isn't already in the cache. 

For example, if you are using Play's `WSClient` to make remote calls, you could wrap any calls to it with this `get` 
method to ensure that any duplicate calls for a given URL get back a cached value:

```scala
class ServiceClient {
  val cache = new DedupingCache[String, Future[WSResponse]]
  
  def makeRequest(url: String): Future[WSResponse] = {
    cache.get(url, wsClient.url(url).get())
  }
}
```

See the `Deduping` controllers in `sample-app-scala` and `sample-app-java` for a complete example of how to setup and
use the `DedupingCache`. You will also have to add the `CacheFilter` to your filter chain, as shown in the
`PingApplicationLoader` class in `sample-app-scala` and the `Filters` class in `sample-app-java`. 

# Caveats and drawbacks to BigPipe

## HTTP headers and error handling 

With BigPipe streaming, you typically start sending the response back to the browser before your backend calls are 
finished. The first part of that response is the HTTP headers and once you've sent them back to the browser, it's too
late to change your mind. If one of those backend calls fails, you've already sent your 200 OK, so you can no longer 
just send the browser a 500 error or a redirect! 

Instead, you must handle errors by injecting JavaScript code into your stream that displays the message when it arrives
in the browser or redirects the user as necessary.

## Caching

Because of the the way headers and error handling work, be extra careful using BigPipe if you cache entire 
pages, especially at the CDN level. Otherwise, you may stream out a 200 OK to the CDN, hit an error with a backend call,
and accidentally end up caching a page with an error on it. 

If your pages are mostly static and can be cached for a long time (e.g. blogs), BigPipe is probably not for you. If 
your pages are mostly dynamic and cannot be cached (e.g. the news feeds at Facebook, LinkedIn, Twitter), then BigPipe
can help.

## Pop-in

Pagelets can be sent down to the browser and rendered client-side in any order. Therefore, you have to be careful to 
avoid too much "pop-in", where rendering each pagelet causes random parts of the page to pop in and move around, which
makes the page hard to use.

To avoid annoying your users, use CSS to size the placeholder elements appropriately so they don't resize or move much
as the actual content pops in. Alternatively, use JavaScript to ensure that the elements on a page render from top to
bottom, even if they show up in a different order (e.g. set `display: none` until all the pagelets above the current 
one have been filled in).

## SEO and noscript

You may not want to use BigPipe for clients that don't have JavaScript enabled, or don't handle JavaScript well, such 
as a search engine crawler. The simplest solution is to run a headless browser in your data center (e.g. 
[zombie](http://zombie.js.org/)) that fetches each page, lets the BigPipe JavaScript execute to render the whole page,
and then sends the rendered HTML down to the client.

# TODO

1. Publish artifacts to Maven central. Currently waiting for this project to be added to Sonatype.
2. Finish the "Composable pagelets" implementation (it is currently unfinished and untested).
3. Finish the "Composable pagelets" and "De-duping remote service calls" documentation.
4. Add support for in-order, pure server-side rendering of pagelets for use cases that don't support JavaScript (e.g. 
   SEO).
5. Add examples to the sample apps of using client-side templates (e.g. Mustache.js) to render pagelets.
6. Add examples of error handling while doing BigPipe streaming.
7. More integration tests of the streaming to actually check timings and ensure JavaScript code is working
8. Add support for pagelet priorities
9. Add support for only rendering content that's visible

# Release process

This project is published to Sonatype as described in the 
[SBT Deploying to Sonatype](http://www.scala-sbt.org/release/docs/Using-Sonatype.html) documentation. To do that, this
project uses the [sbt-sonatype](https://github.com/xerial/sbt-sonatype), [sbt-pgp](http://www.scala-sbt.org/sbt-pgp), 
and [sbt-release](https://github.com/sbt/sbt-release) plugins.

To release a new version, you must have your PGP keys already setup 
([docs here](http://www.scala-sbt.org/release/docs/Using-Sonatype.html#First+-+PGP+Signatures)) and then you can run:

```
activator shell
set credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "<username>", "<password>")
release
```

# License

This code is available under the MIT license. See the LICENSE file for more info.
