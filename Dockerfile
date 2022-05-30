FROM ubuntu:20.04 as ar4k-builder
RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y openjdk-8-jdk
COPY . /ar4kAgent
WORKDIR /ar4kAgent
RUN chmod +x gradlew
RUN ./gradlew clean shadowJar

FROM ubuntu:20.04
RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y openjdk-8-jre redis redis-server redis-sentinel redis-tools && apt-get clean && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java"]
CMD ["-XX:+UnlockExperimentalVMOptions","-Djava.net.preferIPv4Stack=true","-XX:+UseCGroupMemoryLimitForHeap","-XshowSettings:vm","-Djava.security.egd=file:/dev/./urandom","-jar","/agent.jar"]
COPY --from=ar4k-builder /ar4kAgent/build/libs/*-all.jar /agent.jar
