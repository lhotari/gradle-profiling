import static com.sun.btrace.BTraceUtils.*;

import java.util.Deque;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.BTraceUtils.Strings;
import com.sun.btrace.Profiler;
import com.sun.btrace.annotations.*;

@BTrace
public class SimpleProfiler {
    @Property(name = "profiler")
    public static Profiler profiler = BTraceUtils.Profiling.newProfiler();

    @TLS
    public static Deque<Long> entryTimes = BTraceUtils.Collections.newDeque();

    @OnProbe(namespace = "profiler-probes", name = "profiler-entry")
    public static void entry() {
        BTraceUtils.Collections.push(entryTimes, box(timeMillis()));
        BTraceUtils.Profiling.recordEntry(profiler,
                Strings.strcat(Strings.strcat(name(probeClass()), "."), probeMethod()));
    }

    @OnProbe(namespace = "profiler-probes", name = "profiler-exit")
    public static void exit() {
        long startTime = unbox(BTraceUtils.Collections.removeFirst(entryTimes));
        long duration = timeMillis() - startTime;
        BTraceUtils.Profiling.recordExit(profiler,
                Strings.strcat(Strings.strcat(name(probeClass()), "."), probeMethod()), duration);
    }

    @OnTimer(5000)
    public static void timer() {
        BTraceUtils.Profiling.printSnapshot("Profiling snapshot", profiler, "%1$s %2$s %8$s %9$s %10$s");
    }
}
