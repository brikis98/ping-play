package com.ybrikman.ping.scalaapi.bigpipe

import org.specs2.mutable.Specification
import play.twirl.api.Html

class TestEmbed extends Specification {

  "The Embed.escapeForEmbedding method should" >> {
    "leave content without dashes unchanged" >> {
      Embed.escapeForEmbedding("foo bar baz") mustEqual Html("foo bar baz")
    }

    "leave content with single dashes unchanged" >> {
      Embed.escapeForEmbedding("foo-bar-baz") mustEqual Html("foo-bar-baz")
    }

    "escape all double dashes" >> {
      Embed.escapeForEmbedding("foo--bar--baz") mustEqual Html("foo\u002d\u002dbar\u002d\u002dbaz")
    }
  }
}
