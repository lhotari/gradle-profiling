import com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.Profiler;
import com.sun.btrace.annotations.*;

import java.util.Deque;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class GradleTaskInputsAndOutputs {
    @Property(name = "GradleTaskInputsAndOutputs")
    public static Profiler profiler = Profiling.newProfiler();

    @TLS
    public static Deque<Long> entryTimes = Collections.newDeque();

    @OnMethod(
            clazz = "/org\\.gradle\\.api\\.internal\\.tasks\\.DefaultTask(Out|In)puts/",
            method = "getFiles"
    )
    public static void entry() {
        Collections.push(entryTimes, box(timeNanos()));
    }

    @OnMethod(
            clazz = "/org\\.gradle\\.api\\.internal\\.tasks\\.DefaultTask(Out|In)puts/",
            method = "getFiles",
            location = @Location(Kind.RETURN)
    )
    public static void exit(@Self Object thisObject, @Return Object returnValue) {
        long startTime = unbox(Collections.removeFirst(entryTimes));
        long duration = timeNanos() - startTime;
        String displayName;
        if(compare(classOf(thisObject), classForName("org.gradle.api.internal.tasks.DefaultTaskInputs", contextClassLoader()))) {
            Object inputFiles = Reflective.get(Reflective.field(classOf(thisObject), "inputFiles"), thisObject);
            displayName = (String) Reflective.get(Reflective.field(classOf(inputFiles), "displayName"), inputFiles);
        } else {
            displayName = (String) Reflective.get(Reflective.field(classOf(returnValue), "displayName"), returnValue);
        }
        Profiling.recordEntry(profiler, displayName);
        Profiling.recordExit(profiler, displayName, duration);
    }

    @OnTimer(5000)
    public static void timer() {
        Profiling.printSnapshot("GradleTaskInputsAndOutputs", profiler, "%1$s %2$s %8$s %9$s %10$s");
    }
}
