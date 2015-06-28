# Based off an image that has JDK8 on busybox
FROM frolvlad/alpine-oraclejdk8:cleaned
MAINTAINER Yevgeniy Brikman <jim@ybrikman.com>

RUN apk --update add bash

# Set up activator
RUN wget http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2-minimal.zip \
    && unzip typesafe-activator-1.3.2-minimal.zip \
    && rm -f typesafe-activator-1.3.2-minimal.zip \
    && chmod +x activator-1.3.2-minimal/activator
ENV PATH $PATH:/activator-1.3.2-minimal

# The source code will be in the /src folder
RUN mkdir -p /src
VOLUME /src
WORKDIR /src
COPY . /src

# Use a global SBT config to setup an external target directory so that the 
# compiled code isn't blown away if the user mounts a src folder from their
# host OS.
RUN mkdir -p /sbt-target \
    && mkdir -p ~/.sbt/0.13/ \
    && echo 'target := file("/sbt-target") / s"${name.value}-target"' > ~/.sbt/0.13/global.sbt

# Build the entire app so that all the dependencies are downloaded and all the
# code is compiled. This will make starting the app the first time much faster.
RUN activator dist

# Expose play port
EXPOSE 9000

# Default command is to run the app
CMD ["activator", "run"]