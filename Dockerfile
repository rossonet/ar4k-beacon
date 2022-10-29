FROM ubuntu:20.04 as ar4k-builder
RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y openjdk-8-jdk
COPY . /ar4kAgent
WORKDIR /ar4kAgent
RUN chmod +x gradlew
RUN mkdir /result
RUN ./gradlew clean generateBeaconctlShadowJar
RUN mv /ar4kAgent/beacon-beaconctl/build/libs/*-all.jar /result/beaconctl.jar
RUN ./gradlew clean generateTemplateNifiPlugin
RUN mv /ar4kAgent/beacon-template-nifi-processor/build/libs/beacon-template-nifi-processor-*.nar /result/beacon-template-nifi-processor.nar
RUN mv /ar4kAgent/beacon-template-nifi-service/build/libs/beacon-template-nifi-service-*.nar /result/beacon-template-nifi-service.nar

#FROM ubuntu:20.04
FROM apache/nifi:1.18.0
ARG MAINTAINER="Andrea Ambrosini <andrea.ambrosini@rossonet.org>"
ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions","-Djava.net.preferIPv4Stack=true","-XX:+UseCGroupMemoryLimitForHeap","-XshowSettings:vm","-Djava.security.egd=file:/dev/./urandom","-jar","/beaconctl.jar"]
COPY --from=ar4k-builder /result/beaconctl.jar /beaconctl.jar
COPY --from=ar4k-builder /result/beacon-template-nifi-processor.nar /opt/nifi/nifi-current/lib/beacon-template-nifi-processor.nar
COPY --from=ar4k-builder /result/beacon-template-nifi-service.nar /opt/nifi/nifi-current/lib/beacon-template-nifi-service.nar
