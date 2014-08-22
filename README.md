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

The main example code to look:

1. `app/controllers/Wvyp` and `app/controllers/Wvyu` show a simple, standalone modules.
2. `app/controllers/Aggregator` shows how these modules can be combined to build a more complicated module.
3. `app/controllers/WvypStream` shows how to stream the contents of a module using a BigPipe approach.
4. `app/ui` contains reusable library for composing modules and streaming HTML.

# License

This code is available under the MIT license. See the LICENSE file for more info.
