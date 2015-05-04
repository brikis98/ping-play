# Based off an image that has JDK8 on centos
FROM ingensi/oracle-jdk
MAINTAINER Yevgeniy Brikman <jim@ybrikman.com>

RUN yum update -y && yum install -y unzip

# Set up activator
RUN curl -O http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip 
RUN unzip typesafe-activator-1.3.2.zip -d / && rm typesafe-activator-1.3.2.zip && chmod a+x /activator-1.3.2/activator
ENV PATH $PATH:/activator-1.3.2

# The user should mount the ping-play app into the /app folder
RUN mkdir /app
WORKDIR /app

# Copy the app and build it so all ivy dependencies are downloaded and all the
# compiled classes are in the external target directory. This allows the app 
# to start MUCH faster after the initial checkout.
COPY . /app
RUN mkdir /sbt-target
RUN mkdir -p ~/.sbt/0.13/
RUN echo 'target := file("/sbt-target") / s"${name.value}-target"' > ~/.sbt/0.13/global.sbt
RUN activator dist
RUN rm -rf *

# Expose play ports
EXPOSE 9000 8888

# Default command is to run the activator shell
CMD ["activator", "shell"]