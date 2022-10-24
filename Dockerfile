FROM ubuntu:20.04 as ar4k-builder
RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y openjdk-8-jdk
COPY . /ar4kAgent
WORKDIR /ar4kAgent
RUN chmod +x gradlew
RUN ./gradlew clean :beacon-beaconctl:shadowJar

#FROM ubuntu:20.04
FROM apache/nifi:1.18.0
ARG MAINTAINER="Andrea Ambrosini <andrea.ambrosini@rossonet.org>"
ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions","-Djava.net.preferIPv4Stack=true","-XX:+UseCGroupMemoryLimitForHeap","-XshowSettings:vm","-Djava.security.egd=file:/dev/./urandom","-jar","/beaconctl.jar"]
COPY --from=ar4k-builder /ar4kAgent/beacon-beaconctl/build/libs/*-all.jar /beaconctl.jar
