# Based off an image that has JDK8 on centos
FROM ingensi/oracle-jdk
MAINTAINER Yevgeniy Brikman <jim@ybrikman.com>

RUN yum update -y && yum install -y unzip

# Set up activator
RUN curl -O http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip 
RUN unzip typesafe-activator-1.3.2.zip -d / && rm typesafe-activator-1.3.2.zip && chmod a+x /activator-1.3.2/activator
ENV PATH $PATH:/activator-1.3.2

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