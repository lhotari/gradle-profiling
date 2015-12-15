import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.OnTimer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sun.btrace.BTraceUtils.Atomic.incrementAndGet;
import static com.sun.btrace.BTraceUtils.Atomic.newAtomicInteger;
import static com.sun.btrace.BTraceUtils.Collections.*;
import static com.sun.btrace.BTraceUtils.jstackStr;
import static com.sun.btrace.BTraceUtils.printNumberMap;

@BTrace
public class PatternSetCreation {
    private static Map<String, AtomicInteger> histo = newHashMap();

    @OnMethod(
            clazz = "org.gradle.api.tasks.util.PatternSet",
            method = "<init>"
    )
    public static void onPatternSetInit() {
        String stackTrace = jstackStr(6);
        AtomicInteger counter = get(histo, stackTrace);
        if (counter == null) {
            counter = newAtomicInteger(1);
            put(histo, stackTrace, counter);
        } else {
            incrementAndGet(counter);
        }
    }

    @OnTimer(5000)
    public static void print() {
        if (size(histo) != 0) {
            printNumberMap("\n\nStacktrace Histogram for 'new PatternSet()'", histo);
        }
    }
}
