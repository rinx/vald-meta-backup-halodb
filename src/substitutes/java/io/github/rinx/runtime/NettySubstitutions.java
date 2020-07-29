package io.github.rinx.runtime;

import sun.misc.Unsafe;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;

import java.util.function.Predicate;
import java.security.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@TargetClass(className = "io.grpc.netty.ProtocolNegotiators")
final class Target_io_grpc_netty_ProtocolNegotiators {

    @Substitute
    static void logSslEngineDetails(Level level, ChannelHandlerContext ctx, String msg, Throwable t) {
        Logger log = Logger.getLogger("io.grpc.netty.ProtocolNegotiators");
        if (log.isLoggable(level)) {
            log.log(level, msg + "\nNo SSLEngine details available!", t);
        }
    }
}

@TargetClass(className = "io.grpc.netty.GrpcSslContexts")
final class Target_io_grpc_netty_GrpcSslContexts {

    @Substitute
    public static SslContextBuilder configure(SslContextBuilder builder, SslProvider provider) {
        switch (provider) {
            case JDK: {
                Provider jdkProvider = findJdkProvider();
                if (jdkProvider == null) {
                    throw new IllegalArgumentException(
                            "Could not find Jetty NPN/ALPN or Conscrypt as installed JDK providers");
                }
                return configure(builder, jdkProvider);
            }
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    @Alias
    private static Provider findJdkProvider() {
        return null;
    }

    @Alias
    public static SslContextBuilder configure(SslContextBuilder builder, Provider jdkProvider) {
        return null;
    }

}

@TargetClass(className = "com.google.protobuf.UnsafeUtil")
final class Target_com_google_protobuf_UnsafeUtil {
    @Substitute
    static sun.misc.Unsafe getUnsafe() {
        return null;
    }
}

public class NettySubstitutions {
}
