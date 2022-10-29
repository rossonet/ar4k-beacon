[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/rossonet/ar4k-beacon)
[![Test on master branch with Gradle](https://github.com/rossonet/ar4k-beacon/actions/workflows/test-on-master-with-gradle.yml/badge.svg)](https://github.com/rossonet/ar4k-beacon/actions/workflows/test-on-master-with-gradle.yml)
[![Build and publish docker image to DockerHub](https://github.com/rossonet/ar4k-beacon/actions/workflows/publish-to-dockerhub.yml/badge.svg)](https://github.com/rossonet/ar4k-beacon/actions/workflows/publish-to-dockerhub.yml)
[![Build and publish docker image to GitHub Registry](https://github.com/rossonet/ar4k-beacon/actions/workflows/publish-to-github-registry.yml/badge.svg)](https://github.com/rossonet/ar4k-beacon/actions/workflows/publish-to-github-registry.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/193f0dd54e7e44b980b3eece721e9ec4)](https://www.codacy.com/gh/rossonet/ar4k-beacon/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rossonet/ar4k-beacon&amp;utm_campaign=Badge_Grade)


[![Beacon Logo](https://raw.githubusercontent.com/rossonet/ar4k-beacon/master/artwork/ar4k-beacon.png)](https://github.com/rossonet/ar4k-beacon)

# Build and run on docker

To build the container
```
docker build --rm -t rossonet/ar4k-beacon:latest .
Sending build context to Docker daemon  121.8MB
Step 1/14 : FROM ubuntu:20.04 as ar4k-builder
 ---> 817578334b4d
Step 2/14 : RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y openjdk-8-jdk
 ---> Using cache
 ---> 83ca1feebe88
Step 3/14 : COPY . /ar4kAgent
[...]
Step 13/14 : COPY --from=ar4k-builder /result/beaconctl.jar /beaconctl.jar
 ---> cf9f6ec3cf21
Step 14/14 : COPY --from=ar4k-builder /result/beacon-template-nifi-processor.nar /opt/nifi/nifi-current/lib/beacon-template-nifi-processor.nar
 ---> 9fb19b09f9fd
Successfully built 9fb19b09f9fd
Successfully tagged rossonet/ar4k-beacon:latest
```

Run the container
```
mkdir ./beacon-data
docker run -it --rm -v $(pwd)/beacon-data:/beacon-data -p 8080:8080 rossonet/ar4k-beacon:latest
```


## AR4k Beacon - API

TODO: description

## AR4K Beacon Server runner (beaconctl)

TODO: description

### Project sponsor 

[![Rossonet s.c.a r.l.](https://raw.githubusercontent.com/rossonet/images/main/artwork/rossonet-logo/png/rossonet-logo_280_115.png)](https://www.rossonet.net)


