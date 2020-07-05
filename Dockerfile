FROM golang:latest AS go

FROM rinx/grpc-java-builder:latest AS grpc-java-builder
LABEL maintainer "rinx <rintaro.okamura@gmail.com>"

ENV GOPATH /go
ENV GOROOT /usr/local/go
ENV GO111MODULE on

RUN apt-get update \
    && apt-get install -y \
    git \
    curl \
    gcc \
    musl-dev \
    && rm -rf /var/lib/apt/lists/*

COPY --from=go /usr/local/go/src  $GOROOT/src
COPY --from=go /usr/local/go/lib  $GOROOT/lib
COPY --from=go /usr/local/go/pkg  $GOROOT/pkg
COPY --from=go /usr/local/go/misc $GOROOT/misc
COPY --from=go /usr/local/go/bin  $GOROOT/bin

COPY --from=go /go $GOPATH
RUN chmod a+rw -R /go

ENV PATH=$PATH:$GOPATH/bin:$GOROOT/bin

RUN mkdir -p /work

WORKDIR /work

COPY Makefile .
COPY versions .

RUN make proto

FROM clojure:lein-alpine AS builder
LABEL maintainer "rinx <rintaro.okamura@gmail.com>"

RUN set -eux && apk update && apk --no-cache add git openssh

RUN mkdir -p /usr/src/app/src
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/
RUN lein deps
COPY src/vald_meta_backup_halodb /usr/src/app/src/vald_meta_backup_halodb
COPY --from=grpc-java-builder /work/src/main /usr/src/app/src/main
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

FROM alpine:latest
LABEL maintainer "rinx <rintaro.okamura@gmail.com>"

RUN set -eux && apk update && apk --no-cache add openjdk8-jre

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY --from=builder /usr/src/app/app-standalone.jar /usr/src/app/app-standalone.jar

ENTRYPOINT ["/usr/bin/java"]
CMD ["-Djava.security.policy=/usr/src/app/java.policy", "-jar", "app-standalone.jar"]
