# Ping-Play

The ping-play project brings [BigPipe](https://www.facebook.com/note.php?note_id=389414033919) streaming to the 
[Play Framework](http://playframework.com/). It includes tools for a) splitting your pages up into small "pagelets", 
which makes it easier to maintain large websites, and b) streaming those pagelets down to the browser as soon as they 
are ready, which can significantly reduce page load time.

To fetch the data for a page, modern apps often have to make requests to multiple remote backend services (e.g. RESTful
HTTP calls to a profile service, a search service, an ads service, etc). You then have to wait for *all* of these 
remote calls to come back before you can send *any* data back to the browser. For example, the following screen capture 
shows a page that makes 6 remote service calls, most of which complete in few hundred milliseconds, but one takes 
over 5 seconds. As a result, the time to first byte is 5 seconds, during which the user sees a completely blank page:

![Page loading without BigPipe](/images/without-big-pipe.gif)

With BigPipe, you can start streaming data back to the browser without waiting for the backends at all, and fill in the 
page incrementally as each backend responds. For example, the following screen capture shows the same page making the 
same 6 remote service calls, but this time rendered using BigPipe. The header and much of the markup is sent back 
instantly, so time to first byte is 10 milliseconds (instead of 5 seconds), static content (i.e. CSS, JS, images) can 
start loading right away, and then, as each backend service responds, the corresponding part of the page (i.e. the 
pagelet) is sent to the browser and rendered on the screen:

![Page loading with BigPipe](/images/with-big-pipe.gif)

# Quick start

To understand how to transform your Play app to use BigPipe, it's helpful to first see an example that does *not* use
BigPipe. Here is the controller code, `controllers/WithoutBigPipe.scala`, for the example mentioned above: 

```scala
class WithoutBigPipe(serviceClient: FakeServiceClient) extends Controller {
  def index = Action.async { implicit request =>
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallFast("search")

    // Wait for all the remote calls to complete
    for {
      profile <- profileFuture
      graph <- graphFuture
      feed <- feedFuture
      inbox <- inboxFuture
      ads <- adsFuture
      search <- searchFuture
    } yield {
      // Render the template once all the data is available
      Ok(views.html.withoutBigPipe(profile, graph, feed, inbox, ads, search))
    }
  }
}
```

This controller makes 6 remote service calls, gets back 6 `Future` objects, and when they have all redeemed, it uses 
them to render the following template, `views/withoutBigPipe.scala.html`:

```html
@(profile: data.Response, graph: data.Response, feed: data.Response, inbox: data.Response, ads: data.Response, search: data.Response)

<html>
  <head>
    <link rel="stylesheet" href="/assets/stylesheets/main.css">
  </head>
  <body>
    <h1>Without Big Pipe</h1>
    <table class="wrapper">
      <tr>
        <td><div id="profile">@views.html.helpers.module(profile)</div></td>
        <td><div id="ads">@views.html.helpers.module(ads)</div></td>
        <td><div id="feed">@views.html.helpers.module(feed)</div></td>
      </tr>
      <tr>
        <td><div id="search">@views.html.helpers.module(search)</div></td>
        <td><div id="inbox">@views.html.helpers.module(inbox)</div></td>
        <td><div id="graph">@views.html.helpers.module(graph)</div></td>
      </tr>
    </table>
  </body>
</html>
```

When you load this page, nothing will show up on the screen until all of the backend calls complete, which will take
about 5 seconds.

To transform this page to use BigPipe, you first add the big-pipe dependency to your build (note, this project requires 
Play 2.4, Scala 2.11.6, SBT 0.13.8, and Java 8):

```scala
libraryDependencies += "com.ybrikman.ping" %% "big-pipe" % "0.0.11"
```

Next, add support for the `.scala.stream` template type and some imports for it to your build:

```scala
TwirlKeys.templateFormats ++= Map("stream" -> "com.ybrikman.ping.scalaapi.bigpipe.HtmlStreamFormat"),
TwirlKeys.templateImports ++= Vector("com.ybrikman.ping.scalaapi.bigpipe.HtmlStream")
```

Now you can create streaming templates. These templates can mix normal HTML markup, which will be streamed to the 
browser immediately, with the `HtmlStream` class, which is a wrapper for an `Enumerator[Html]` that will be streamed
to the browser whenever the `Enumerator` has data. Here is `views/withBigPipe.scala.stream`, which is the streaming
version of the template above:

```html
@(body: HtmlStream)

<html>
  <head>
    <link rel="stylesheet" href="/assets/stylesheets/main.css">
    <!-- You need to include the BigPipe JavaScript at the top of the page -->
    <script src="/assets/com/ybrikman/ping/big-pipe.js"></script>
  </head>
  <body>
    <h1>With Big Pipe</h1>
    <!-- These are placeholders that will be filled in as the data becomes available -->
    <table class="wrapper">
      <tr>
        <td><div id="profile"></div></td>
        <td><div id="ads"></div></td>
        <td><div id="feed"></div></td>
      </tr>
      <tr>
        <td><div id="search"></div></td>
        <td><div id="inbox"></div></td>
        <td><div id="graph"></div></td>
      </tr>
    </table>

    @body
  </body>
</html>
```

The key changes to notice from the original template are:

1. The `div` elements for each piece of data are just empty placeholders. They will be filled in incrementally by 
   pagelets as they arrive from the server.
2. The template takes an `HtmlStream` called `body` as a parameter and inserts it at the bottom of the page. This is 
   where all the pagelets will actually stream in.
3. The `big-pipe.js` JavaScript file is included in the `head`. `big-pipe.js` takes care of rendering each pagelet when 
   it arrives in the browser.

Now, let's look at the controller you can use with this template, called `controllers/WithBigPipe.scala`:

```scala
class WithBigPipe(serviceClient: FakeServiceClient) extends Controller {
  def index = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallFast("search")

    // Convert each Future into a Pagelet which will be rendered as HTML as soon as the data is available
    val profile = Pagelet.fromHtmlFuture(profileFuture.map(views.html.helpers.module.apply), "profile")
    val graph = Pagelet.fromHtmlFuture(graphFuture.map(views.html.helpers.module.apply), "graph")
    val feed = Pagelet.fromHtmlFuture(feedFuture.map(views.html.helpers.module.apply), "feed")
    val inbox = Pagelet.fromHtmlFuture(inboxFuture.map(views.html.helpers.module.apply), "inbox")
    val ads = Pagelet.fromHtmlFuture(adsFuture.map(views.html.helpers.module.apply), "ads")
    val search = Pagelet.fromHtmlFuture(searchFuture.map(views.html.helpers.module.apply), "search")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search)

    // Render the streaming template immediately
    Ok.chunked(views.stream.withBigPipe(body))
  }
}
```

The key changes to notice from the original controller are:

1. Instead of waiting for *all* of the service calls to redeem, you render each one individually into `Html` as soon as 
   the data is available, giving you a `Future[Html]`.
2. Each `Future[Html]`, plus the DOM id of the placeholder where it should be inserted, is wrapped in a `Pagelet` 
   object.  
3. The `Pagelet` objects are interleaved into an `HtmlStream` so that they can be sent down to the browser in whatever 
   order they redeem.
4. This `HtmlStream` is passed as the `body` parameter to the `withBigPipe.scala.stream` template above.   

When you load this page, you will see the outline of the page almost immediately, and each piece of the page will 
fill in this outline as soon as the corresponding remote service responds.  

# More examples

There are several BigPipe examples, including the one described above, in `sample-app-scala` and `sample-app-java` in
this repo (yes, BigPipe streaming works with both Scala and Java). You'll also want to browse `sample-app-common`, 
which has some code shared by both sample apps, including all of their templates. For example, here is how to run the 
Scala sample app (assuming you have [Typesafe Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) 
installed already):

1. `git clone` this repo.
2. `activator shell`
3. `project sampleAppScala`
4. `run`
5. Open `http://localhost:9000/withoutBigPipe` to see how long the page takes to load without BigPipe streaming.
6. Open `http://localhost:9000/withBigPipe` to see how much faster the page loads with BigPipe streaming.

Check out the [Documentation](#Documentation) to see what APIs are available and [FAQ](#FAQ) to learn more about 
BigPipe.

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
class MoreBigPipeExamples(serviceClient: FakeServiceClient) extends Controller {

  /**
   * Instead of rendering each pagelet server-side with Play's templating, you can send back JSON and render each 
   * pagelet with a client-side templating library such as mustache.js
   * 
   * @return
   */
  def clientSideTemplating = Action {
    // Make several fake service calls in parallel to represent fetching data from remote backends. Some of the calls
    // will be fast, some medium, and some slow.
    val profileFuture = serviceClient.fakeRemoteCallJsonMedium("profile")
    val graphFuture = serviceClient.fakeRemoteCallJsonMedium("graph")
    val feedFuture = serviceClient.fakeRemoteCallJsonSlow("feed")
    val inboxFuture = serviceClient.fakeRemoteCallJsonSlow("inbox")
    val adsFuture = serviceClient.fakeRemoteCallJsonFast("ads")
    val searchFuture = serviceClient.fakeRemoteCallJsonFast("search")

    // Convert each Future into a Pagelet which will send the JSON to the browser as soon as it's available
    val profile = Pagelet.fromJsonFuture(profileFuture, "profile")
    val graph = Pagelet.fromJsonFuture(graphFuture, "graph")
    val feed = Pagelet.fromJsonFuture(feedFuture, "feed")
    val inbox = Pagelet.fromJsonFuture(inboxFuture, "inbox")
    val ads = Pagelet.fromJsonFuture(adsFuture, "ads")
    val search = Pagelet.fromJsonFuture(searchFuture, "search")

    // Compose all the pagelets into an HtmlStream
    val body = HtmlStream.fromInterleavedPagelets(profile, graph, feed, inbox, ads, search)

    // Render the streaming template immediately
    Ok.chunked(views.stream.clientSideTemplating(body))
    
  }
}
```

Next, create your custom `BigPipe.renderPagelet` method:

```javascript
// Override the original BigPipe.renderPagelet method with one that uses mustache.js for client-side rendering
BigPipe.renderPagelet = function(id, json) {
  var domElement = document.getElementById(id);
  if (domElement) {
    domElement.innerHTML = Mustache.render(template, json);
  } else {
    console.log("ERROR: cannot render pagelet because DOM node with id " + id + " does not exist");
  }
};
```

See `MoreBigPipeExamples#clientSideTemplating` and `big-pipe-with-mustache.js` for working examples.

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

# FAQ

## What are the caveats and drawbacks to BigPipe?

BigPipe is not for everyone. There are some serious drawbacks and caveats you should be aware of before using it:

### HTTP headers and error handling 

With BigPipe streaming, you typically start sending the response back to the browser before your backend calls are 
finished. The first part of that response is the HTTP headers and once you've sent them back to the browser, it's too
late to change your mind. If one of those backend calls fails, you've already sent your 200 OK, so you can no longer 
just send the browser a 500 error or a redirect! 

Instead, you must handle errors by injecting JavaScript code into your stream that displays the message when it arrives
in the browser or redirects the user as necessary. See `MoreBigPipeExamples#errorHandling` for an example.

### Caching

Because of the the way headers and error handling work, be extra careful using BigPipe if you cache entire 
pages, especially at the CDN level. Otherwise, you may stream out a 200 OK to the CDN, hit an error with a backend call,
and accidentally end up caching a page with an error on it. 

If your pages are mostly static and can be cached for a long time (e.g. blogs), BigPipe is probably not for you. If 
your pages are mostly dynamic and cannot be cached (e.g. the news feeds at Facebook, LinkedIn, Twitter), then BigPipe
can help.

### Pop-in

Pagelets can be sent down to the browser and rendered client-side in any order. Therefore, you have to be careful to 
avoid too much "pop-in", where rendering each pagelet causes random parts of the page to pop in and move around, which
makes the page hard to use.

To avoid annoying your users, use CSS to size the placeholder elements appropriately so they don't resize or move much
as the actual content pops in. Alternatively, use JavaScript to ensure that the elements on a page render from top to
bottom, even if they show up in a different order (e.g. set `display: none` until all the pagelets above the current 
one have been filled in).

### SEO and noscript

You may not want to use BigPipe for clients that don't have JavaScript enabled, or don't handle JavaScript well, such 
as a search engine crawler. The simplest solution is to run a headless browser in your data center (e.g. 
[zombie](http://zombie.js.org/)) that fetches each page, lets the BigPipe JavaScript execute to render the whole page,
and then sends the rendered HTML down to the client.

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

## Where can I find more info? 

1. [Composable and Streamable Play Apps](https://engineering.linkedin.com/play/composable-and-streamable-play-apps): 
   a talk that introduces how BigPipe streaming works on top of Play (see the 
   [video](https://www.youtube.com/watch?v=4b1XLka0UIw) and 
   [slides](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps)). 
2. [BigPipe: Pipelining web pages for high performance](https://www.facebook.com/note.php?note_id=389414033919): the
   original blog post by Facebook that introduces BigPipe on PHP.
3. [New technologies for the new LinkedIn home page](http://engineering.linkedin.com/frontend/new-technologies-new-linkedin-home-page):
   the new LinkedIn homepage is using BigPipe style streaming with Play. This ping-play project is loosely based off of 
   the work done originally at LinkedIn. 

# Project info

## Status

This project is in alpha status. It has been used on small projects and is reasonably well coded, tested, and 
documented, but it needs more real world usage before it can be considered a mature library. Until the project hits
version 1.0.0, backwards compatibility is *not* guaranteed, so expect APIs to change.

## Contributing

Contributions in the form of bug reports and pull requests are very welcome. If you're using this project in production,
[drop me a line](mailto:jim@ybrikman.com), as I'd love to hear about your experiences! 

## TODO

1. Finish the "Composable pagelets" implementation and documentation (it is currently unfinished and untested).
2. Publish artifacts automatically as part of the build process instead of doing it manually.
3. Add automated tests that check the actual timing of the BigPipe streaming.
4. Add support for pagelet priorities.
5. Add support for only rendering content that's visible.
6. Add support for monitoring hooks for each pagelet.

## Release process

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

Currently, only the maintainer, [Yevgeniy Brikman](http://www.ybrikman.com) has the credentials for publishing new 
versions.

# License

This code is available under the MIT license. See the LICENSE file for more info.
