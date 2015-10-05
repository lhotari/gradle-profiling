import com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.Profiler;
import com.sun.btrace.annotations.*;

import java.util.Deque;
import java.io.File;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class GradleDirTreeScans {
    @Property(name = "GradleDirTreeScans")
    public static Profiler profiler = Profiling.newProfiler();

    @TLS
    public static Deque<Long> entryTimes = Collections.newDeque();

    @OnMethod(
            clazz = "org.gradle.api.internal.file.collections.DirectoryFileTree",
            method = "visit"
    )
    public static void entry() {
        Collections.push(entryTimes, box(timeNanos()));
    }

    @OnMethod(
            clazz = "org.gradle.api.internal.file.collections.DirectoryFileTree",
            method = "visit",
            location = @Location(Kind.RETURN)
    )
    public static void exit(@Self Object thisObject) {
        long startTime = unbox(Collections.removeFirst(entryTimes));
        long duration = timeNanos() - startTime;
        File dir = (File)Reflective.get(Reflective.field(classOf(thisObject), "dir"), thisObject);
        String displayName = Strings.str(dir);
        Profiling.recordEntry(profiler, displayName);
        Profiling.recordExit(profiler, displayName, duration);
    }

    @OnTimer(5000)
    public static void timer() {
        Profiling.printSnapshot("GradleTaskInputsAndOutputs", profiler, "%1$s %2$s %8$s %9$s %10$s");
    }

    @OnEvent("clear_stats")
    public static void reset() {
        Profiling.reset(profiler);
    }
}
