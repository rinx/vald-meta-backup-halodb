package io.github.rinx.runtime;

import sun.misc.Unsafe;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "com.google.protobuf.UnsafeUtil")
public final class UnsafeUtilSubstitution {
    @Substitute
    static sun.misc.Unsafe getUnsafe() {
        return null;
    }
}
