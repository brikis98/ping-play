# Based off an image that has JDK8 on centos
FROM ingensi/oracle-jdk
MAINTAINER Yevgeniy Brikman <jim@ybrikman.com>

RUN yum update -y && yum install -y unzip

# Set up activator
RUN curl -O http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip 
RUN unzip typesafe-activator-1.3.2.zip -d / && rm typesafe-activator-1.3.2.zip && chmod a+x /activator-1.3.2/activator
ENV PATH $PATH:/activator-1.3.2

# The user should mount the ping-play app into the /src folder
RUN mkdir -p /src
VOLUME /src
WORKDIR /src
COPY . /src

# Build the app so all ivy dependencies are downloaded and all the classes are
# compiled.  This allows the app to start MUCH faster after the initial 
# checkout. We use a global SBT config to setup an external target directory so 
# the compiled code isn't blown away if the user mounts a src folder from their
# host OS.
RUN mkdir -p /sbt-target
RUN mkdir -p ~/.sbt/0.13/
RUN echo 'target := file("/sbt-target") / s"${name.value}-target"' > ~/.sbt/0.13/global.sbt
RUN activator dist

# Expose play port
EXPOSE 9000

# Default command is to run the activator shell
CMD ["activator", "shell"]