# Ping-Play

This is a sample [Play Framework](http://playframework.com/) application that shows how to:

1. **Compose standalone controllers** to build a more complex page from simpler parts.
2. **Implement BigPipe-style streaming** so you can break a page into "pagelets" and stream each pagelet to the browser as soon as the data is available, dramatically reducing load times. See [Facebook BigPipe](https://www.facebook.com/note.php?note_id=389414033919) for more info.

LinkedIn is using composable endpoints and BigPipe streaming in production on its [new homepage](http://engineering.linkedin.com/frontend/new-technologies-new-linkedin-home-page). However, the code in *this* repo was built for a talk called *Composable and Streamable Play Apps* ([slides](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps), [video](https://www.youtube.com/watch?v=4b1XLka0UIw)) at [Ping Conference 2014](http://www.ping-conf.com/), so it's just for demonstration and education purposes, and includes no tests.

# How to run the app

Pre-requisite: [Install Play](http://www.playframework.com/download) 

1. `git clone` this repo
2. `activator run`
3. Go to `http://localhost:9000` to test

# Running with Docker

As an alternative to installing activator and waiting for SBT to download all
dependencies and compile the code, if you have [Docker](https://www.docker.com/) 
and [Docker Compose](https://docs.docker.com/compose/) already installed, 
you can run a Docker image of ping-play that has all the code already compiled
within it.

On Linux:

1. `git clone` this repo
2. `docker-compose up`
3. Go to `http://localhost:9000` to test 

On OS X, using the [docker-osx-dev](https://github.com/brikis98/docker-osx-dev)
project:

1. `git clone` this repo
2. `docker-osx-dev`
3. `docker-compose up`
4. Go to `http://dockerhost:9000` to test

# More info 

* Composable and Streamable Play Apps: [slides](http://www.slideshare.net/brikis98/composable-and-streamable-play-apps), [video](https://www.youtube.com/watch?v=4b1XLka0UIw)
* [Facebook BigPipe](https://www.facebook.com/note.php?note_id=389414033919)

# How to browse the code

The main example code to look at:

1. `app/controllers/Wvyp` and `app/controllers/Wvyu` are examples of simple, standalone modules.
2. `app/controllers/Aggregator` shows how these modules can be combined to build a more complicated module.
3. `app/controllers/WvypStream` shows how to stream the contents of a module using a BigPipe approach.
4. `app/ui` a reusable library for composing modules and streaming HTML.

# De-duping service calls

To be able to use standalone endpoints, you need to de-dupe remote service calls, or you'll end up repeating the same calls over and over again in your controllers, which could significantly increase load on the remote endpoints. The basic idea behind de-duping is easy: when you make a remote service call, store the `Future` it returns in a cache, and if the same service call is repeated, just return the cached `Future`.

Below is an outline (ie, an untested, partial implementation) of a `RestClient` you can use to wrap Play's `WS` client with caching. Note that it uses a `Cache` class (see [Cache.scala](https://gist.github.com/brikis98/5843195)), which is just a Scala-friendly wrapper of a `ConcurrentHashMap`. 

```scala
// This is a client you use everywhere in your code to make REST requests. 
// It will de-dupe read requests so you never perform the same HTTP GET more
// than once for the same user. 
// 
// Note that this caching strategy can be used with *any* remote protocol, not 
// just HTTP/REST. The only thing you need is:
//
// 1. A way to tell if it's safe to use the cache
// 2. A way to tell if two requests are identical
//
// For example, for REST: 
//
// 1. Any GET should be cacheable.
// 2. Two GETs with identical URLs are equal.
//
object RestClient {
  
  // Basicaly a ConcurrentHashMap from request id => a cache of service calls made 
  // while processing that request.
  // For the Cache class, see: https://gist.github.com/brikis98/5843195
  private val cache = new Cache[Long, Cache[String, Future[Response]]]()
  
  // Make an HTTP GET request. Assumption: two requests with the same URL are
  // identical, so they will be de-duped. The first time there is a unique URL,
  // we use WS to actually make the request and store the Future object in the
  // cache. The next time we see the same URL, we just return the cached Future.
  def get(url: String)(implicit request: RequestHeader): Future[Response] = {
    cache.get(request.id).getOrElseUpdate(url, WS.url(url).get())
  }
  
  // Initialize the cache for each incoming HTTP request. The best place to call this method
  // is from a filter.
  def initCacheForRequest(request: RequestHeader): Unit = {
    cache.put(request.id, new Cache[String, Future[Response]]())
  }
  
  // Once you are doing processing an incoming request, don't forget to clean up the cache, 
  // or you will have a memory leak. The best place to call this method is from a filter.
  def cleanupCacheForRequest(request: RequestHeader): Unit = {
    cache.remove(request.id)
  }
}
```

To use this `RestClient`, you also need a `CacheFilter` to initialize and cleanup the cache for each request:

```scala
// Put this filter early in your filter chain so it can initialize and clean up
// the cache
object CacheFilter extends Filter {
  
  def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    def init = RestClient.initCacheForRequest(request)
    def cleanup = RestClient.cleanupCacheForRequest(request)
    
    // You have to be very careful with error handling to garauntee the cache gets cleaned 
    // up, or you'll have a memory leak.
    try {
      init
      next(request).map { result => 
        result.body.onDoneEnumerating(cleanup)
      }.recover { case t: Throwable => 
        cleanup
        // Log or re-throw the exception
      }
    } catch {
      case t: Throwable => 
        cleanup
        // Log or re-throw the exception
    }
  }
}
```

Finally, here is an example usage of the `RestClient` in a controller:

```scala
object ExampleUsage extends Controller {
  
  def index = Action { implicit request =>
    // These two calls should be de-duped, so only one remote call
    // is actually made.
    val future1 = RestClient.get("http://www.my-site.com/foo")
    val future2 = RestClient.get("http://www.my-site.com/foo")
    
    for {
      foo1 <- future1
      foo2 <- future2
    } yield {
      // ...
    }
  }
}
```

# Safely injecting "pagelets"

The method I used to inject pagelets in `pagelet.scala.html` is **for demonstration purposes only**. I had to keep the talk short and I wanted to minimize dependencies in this repo, but it's **not** a good idea to shove HTML directly into a script tag:

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

You can pass this JSON to your favorite templating technology, render it, and inject it into the DOM. Here's an example using [Mustache.js](https://github.com/janl/mustache.js/):

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
