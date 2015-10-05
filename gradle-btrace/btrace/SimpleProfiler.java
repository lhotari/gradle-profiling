import com.sun.btrace.BTraceUtils;
import com.sun.btrace.Profiler;
import com.sun.btrace.annotations.*;

@BTrace
public class SimpleProfiler {
    @Property(name = "profiler")
    public static Profiler profiler = BTraceUtils.Profiling.newProfiler();

    @OnProbe(namespace = "profiler-probes", name = "profiler-entry")
    public static void entry(@ProbeMethodName(fqn=true) String probeMethod) {
        BTraceUtils.Profiling.recordEntry(profiler, probeMethod);
    }

    @OnProbe(namespace = "profiler-probes", name = "profiler-exit")
    public static void exit(@ProbeMethodName(fqn=true) String probeMethod, @Duration long duration) {
        BTraceUtils.Profiling.recordExit(profiler, probeMethod, duration);
    }

    @OnTimer(5000)
    public static void timer() {
        BTraceUtils.Profiling.printSnapshot("Profiling snapshot", profiler, "%1$s %2$s %8$s %9$s %10$s");
    }
}
