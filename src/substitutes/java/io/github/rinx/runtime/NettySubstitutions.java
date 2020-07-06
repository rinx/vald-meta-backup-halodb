package io.github.rinx.runtime;

import sun.misc.Unsafe;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import java.util.function.Predicate;

@TargetClass(className = "io.netty.util.internal.PlatformDependent0")
final class Target_io_netty_util_internal_PlatformDependent0 {
    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, //
            declClassName = "java.nio.Buffer", //
            name = "address") //
    private static long ADDRESS_FIELD_OFFSET;
}

@TargetClass(className = "io.netty.util.internal.CleanerJava6")
final class Target_io_netty_util_internal_CleanerJava6 {
    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, //
            declClassName = "java.nio.DirectByteBuffer", //
            name = "cleaner") //
    private static long CLEANER_FIELD_OFFSET;
}

@TargetClass(className = "io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess")
final class Target_io_netty_util_internal_shaded_org_jctools_util_UnsafeRefArrayAccess {
    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.ArrayIndexShift, declClass = Object[].class) //
    public static int REF_ELEMENT_SHIFT;
}

@TargetClass(className = "io.netty.buffer.AbstractReferenceCountedByteBuf", onlyWith = PlatformHasClass.class)
final class Target_io_netty_buffer_AbstractReferenceCountedByteBuf {
    @Alias
    @RecomputeFieldValue(kind = Kind.FieldOffset, //
            declClassName = "io.netty.buffer.AbstractReferenceCountedByteBuf", //
            name = "refCnt") //
    private static long REFCNT_FIELD_OFFSET;
}

@TargetClass(className = "io.netty.util.AbstractReferenceCounted", onlyWith = PlatformHasClass.class)
final class Target_io_netty_util_AbstractReferenceCounted {
    @Alias
    @RecomputeFieldValue(kind = Kind.FieldOffset, //
            declClassName = "io.netty.util.AbstractReferenceCounted", //
            name = "refCnt") //
    private static long REFCNT_FIELD_OFFSET;
}

/**
 * A predicate to tell whether this platform includes the argument class.
 */
final class PlatformHasClass implements Predicate<String> {
    @Override
    public boolean test(String className) {
        try {
            @SuppressWarnings({ "unused" })
            final Class<?> classForName = Class.forName(className);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
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
