VALDREPO = github.com/vdaas/vald
VALD_VERSION = versions/VALD_VERSION

PROTO_ROOT  = vald/apis/proto
JAVA_ROOT   = src/main/java

PROTOS      = meta/meta.proto manager/backup/backup_manager.proto payload/payload.proto
PROTOS     := $(PROTOS:%=$(PROTO_ROOT)/%)
JAVASOURCES = $(PROTOS:$(PROTO_ROOT)/%.proto=$(JAVA_ROOT)/%.java)

PROTODIRS   = $(shell find $(PROTO_ROOT) -type d | sed -e "s%$(PROTO_ROOT)/%%g" | grep -v "$(PROTO_ROOT)")

PROTO_PATHS = \
	$(PROTODIRS:%=$(PROTO_ROOT)/%) \
	$(GOPATH)/src/github.com/protocolbuffers/protobuf/src \
	$(GOPATH)/src/github.com/gogo/protobuf/protobuf \
	$(GOPATH)/src/github.com/googleapis/googleapis \
	$(GOPATH)/src/github.com/envoyproxy/protoc-gen-validate

MAKELISTS   = Makefile

red    = /bin/echo -e "\x1b[31m\#\# $1\x1b[0m"
green  = /bin/echo -e "\x1b[32m\#\# $1\x1b[0m"
yellow = /bin/echo -e "\x1b[33m\#\# $1\x1b[0m"
blue   = /bin/echo -e "\x1b[34m\#\# $1\x1b[0m"
pink   = /bin/echo -e "\x1b[35m\#\# $1\x1b[0m"
cyan   = /bin/echo -e "\x1b[36m\#\# $1\x1b[0m"

define go-get
	GO111MODULE=on go get -u $1
endef

define go-get-no-mod
	GO111MODULE=off go get -u $1
endef

define mkdir
	mkdir -p $1
endef

.PHONY: all
## execute clean and proto
all: clean proto

.PHONY: help
## print all available commands
help:
	@awk '/^[a-zA-Z_0-9%:\\\/-]+:/ { \
	  helpMessage = match(lastLine, /^## (.*)/); \
	  if (helpMessage) { \
	    helpCommand = $$1; \
	    helpMessage = substr(lastLine, RSTART + 3, RLENGTH); \
      gsub("\\\\", "", helpCommand); \
      gsub(":+$$", "", helpCommand); \
	    printf "  \x1b[32;01m%-35s\x1b[0m %s\n", helpCommand, helpMessage; \
	  } \
	} \
	{ lastLine = $$0 }' $(MAKELISTS) | sort -u
	@printf "\n"

.PHONY: clean
## clean
clean:
	rm -rf $(JAVA_ROOT)

.PHONY: proto
## build proto
proto: \
	$(JAVASOURCES) \
	$(JAVA_ROOT)/io/envoyproxy/pgv/validate/ValidateGrpc.java

$(JAVA_ROOT):
	$(call mkdir, $@)
	$(call rm, -rf, $@/*)

$(JAVASOURCES): vald proto/deps $(JAVA_ROOT)
	@$(call green, "generating .java files...")
	sed -i -e '/^.*gql\.proto.*$$\|^.*gql\..*_type.*$$/d' $(patsubst $(JAVA_ROOT)/%.java,$(PROTO_ROOT)/%.proto,$@)
	protoc \
		$(PROTO_PATHS:%=-I %) \
		--plugin=protoc-gen-grpc-java=`which protoc-gen-grpc-java` \
		--java_out=$(JAVA_ROOT) \
		--grpc-java_out=$(JAVA_ROOT) \
		$(patsubst $(JAVA_ROOT)/%.java,$(PROTO_ROOT)/%.proto,$@)

vald:
	git clone --depth 1 https://$(VALDREPO)


$(PROTO_ROOT)/validate.proto: proto/deps $(SHADOW_ROOT_VALIDATE)
	cp $(GOPATH)/src/github.com/envoyproxy/protoc-gen-validate/validate/validate.proto $(PROTO_ROOT)/validate.proto

$(JAVA_ROOT)/io/envoyproxy/pgv/validate/ValidateGrpc.java: proto/deps $(JAVA_ROOT) $(PROTO_ROOT)/validate.proto
	@$(call green, "generating .java files...")
	protoc \
		$(PROTO_PATHS:%=-I %) \
		-I $(PROTO_ROOT) \
		--plugin=protoc-gen-grpc-java=`which protoc-gen-grpc-java` \
		--java_out=$(JAVA_ROOT) \
		--grpc-java_out=$(JAVA_ROOT) \
		$(PROTO_ROOT)/validate.proto

XMS = 2g
XMX = 7g

TARGET_JAR=target/vald-meta-backup-halodb-0.1.0-SNAPSHOT-standalone.jar

$(TARGET_JAR):
	lein with-profiles +native uberjar

vald-meta-backup-halodb: \
	$(TARGET_JAR)
	native-image \
	-jar $(TARGET_JAR) \
	-H:Name=vald-meta-backup-halodb \
	-H:+ReportExceptionStackTraces \
	-H:Log=registerResource: \
	-H:ReflectionConfigurationFiles=reflection.json \
	-H:ResourceConfigurationFiles=resources.json \
	-H:+RemoveSaturatedTypeFlows \
	--enable-url-protocols=http,https \
	--enable-all-security-services \
	--install-exit-handlers \
	-H:+JNI \
	--verbose \
	--no-fallback \
	--no-server \
	--report-unsupported-elements-at-runtime \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.OpenSsl \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.SSL \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.ConscryptAlpnSslEngine \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.util.internal.logging.Log4JLogger \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.ReferenceCountedOpenSslEngine \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2FrameWriter \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2CodecUtil \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.util.ThreadLocalInsecureRandom \
	--initialize-at-run-time=io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateVerifier \
	--initialize-at-build-time \
	--allow-incomplete-classpath \
	--static \
	-J-Dclojure.spec.skip-macros=true \
	-J-Dclojure.compiler.direct-linking=true \
	-J-Xms$(XMS) \
	-J-Xmx$(XMX)

.PHONY: proto/deps
## install proto deps
proto/deps: \
	$(GOPATH)/bin/protoc-gen-doc \
	$(GOPATH)/bin/protoc-gen-go \
	$(GOPATH)/bin/protoc-gen-gogo \
	$(GOPATH)/bin/protoc-gen-gofast \
	$(GOPATH)/bin/protoc-gen-gogofast \
	$(GOPATH)/bin/protoc-gen-gogofaster \
	$(GOPATH)/bin/protoc-gen-gogoslick \
	$(GOPATH)/bin/protoc-gen-grpc-gateway \
	$(GOPATH)/bin/protoc-gen-swagger \
	$(GOPATH)/bin/protoc-gen-validate \
	$(GOPATH)/bin/prototool \
	$(GOPATH)/bin/swagger \
	$(GOPATH)/src/google.golang.org/genproto \
	$(GOPATH)/src/github.com/envoyproxy/protoc-gen-validate \
	$(GOPATH)/src/github.com/protocolbuffers/protobuf \
	$(GOPATH)/src/github.com/googleapis/googleapis

$(GOPATH)/src/github.com/protocolbuffers/protobuf:
	git clone \
		--depth 1 \
		https://github.com/protocolbuffers/protobuf \
		$(GOPATH)/src/github.com/protocolbuffers/protobuf

$(GOPATH)/src/github.com/googleapis/googleapis:
	git clone \
		--depth 1 \
		https://github.com/googleapis/googleapis \
		$(GOPATH)/src/github.com/googleapis/googleapis

$(GOPATH)/src/github.com/envoyproxy/protoc-gen-validate:
	git clone \
		--depth 1 \
		https://github.com/envoyproxy/protoc-gen-validate \
		$(GOPATH)/src/github.com/envoyproxy/protoc-gen-validate

$(GOPATH)/src/google.golang.org/genproto:
	$(call go-get, google.golang.org/genproto/...)

$(GOPATH)/bin/protoc-gen-go:
	$(call go-get-no-mod, github.com/golang/protobuf/protoc-gen-go)

$(GOPATH)/bin/protoc-gen-gogo:
	$(call go-get-no-mod, github.com/gogo/protobuf/protoc-gen-gogo)

$(GOPATH)/bin/protoc-gen-gofast:
	$(call go-get-no-mod, github.com/gogo/protobuf/protoc-gen-gofast)

$(GOPATH)/bin/protoc-gen-gogofast:
	$(call go-get-no-mod, github.com/gogo/protobuf/protoc-gen-gogofast)

$(GOPATH)/bin/protoc-gen-gogofaster:
	$(call go-get-no-mod, github.com/gogo/protobuf/protoc-gen-gogofaster)

$(GOPATH)/bin/protoc-gen-gogoslick:
	$(call go-get-no-mod, github.com/gogo/protobuf/protoc-gen-gogoslick)

$(GOPATH)/bin/protoc-gen-grpc-gateway:
	$(call go-get, github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway)

$(GOPATH)/bin/protoc-gen-swagger:
	$(call go-get, github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger)

$(GOPATH)/bin/protoc-gen-validate:
	$(call go-get, github.com/envoyproxy/protoc-gen-validate)

$(GOPATH)/bin/prototool:
	$(call go-get, github.com/uber/prototool/cmd/prototool)

$(GOPATH)/bin/protoc-gen-doc:
	$(call go-get, github.com/pseudomuto/protoc-gen-doc/cmd/protoc-gen-doc)

$(GOPATH)/bin/swagger:
	$(call go-get-no-mod, github.com/go-swagger/go-swagger/cmd/swagger)
