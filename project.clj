(defproject vald-meta-backup-halodb "0.1.0-SNAPSHOT"
  :description "A meta / backup-manager implementation for Vald using HaloDB."
  :url "https://github.com/rinx/vald-meta-backup-halodb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.clojure/core.async "0.7.559"]
                 [org.clojure/java.data "0.1.1"]
                 [com.stuartsierra/component "0.4.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [io.forward/yaml "1.0.10"]
                 [clj-halodb "0.0.3"]
                 [io.grpc/grpc-netty "1.30.2"]
                 [io.grpc/grpc-protobuf "1.30.2"]
                 [io.grpc/grpc-stub "1.30.2"]
                 [org.apache.tomcat/annotations-api "6.0.53"]]
  :plugins [[info.sunng/lein-bootclasspath-deps "0.3.0"]]
  :java-source-paths ["src/main/java"]
  :boot-dependencies [[com.google.protobuf/protobuf-java "3.12.2"]]
  :profiles {:uberjar {:aot :all
                       :main vald-meta-backup-halodb.core}
             :native {:aot :all
                      :dependencies [[org.graalvm.nativeimage/svm "20.1.0"]
                                     [borkdude/clj-reflector-graal-java11-fix "0.0.1-graalvm-20.1.0"]]
                      :java-source-paths ["src/substitutes/java"]
                      :main vald-meta-backup-halodb.core}})
