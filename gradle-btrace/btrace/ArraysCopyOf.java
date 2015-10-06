import com.sun.btrace.AnyType;
import com.sun.btrace.BTraceUtils;
import com.sun.btrace.BTraceUtils.Aggregations;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;

import java.util.Deque;

import static com.sun.btrace.BTraceUtils.Numbers.unbox;
import static com.sun.btrace.BTraceUtils.addLast;

@BTrace
public class ArraysCopyOf {
    private static Aggregation count = Aggregations.newAggregation(AggregationFunction.COUNT);
    private static Aggregation max = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Deque<Aggregation> aggregations = BTraceUtils.Collections.newDeque();

    static {
        addLast(aggregations, max);
        addLast(aggregations, count);
    }

    // javap ./subprojects/core/build/classes/main/org/gradle/api/internal/changedetection/state/InMemoryTaskArtifactCache\$1.class
    @OnMethod(
            clazz = "java.util.Arrays",
            method = "copyOf"
    )
    public static void arraysCopyOf(AnyType[] args) {
        Object newSizeArg = args[1];
        int newSize = unbox((Integer) newSizeArg);
        Aggregations.addToAggregation(max, newSize);
        Aggregations.addToAggregation(count, 1L);
    }

    @OnMethod(clazz = "org.gradle.launcher.exec.InProcessBuildActionExecuter$DefaultBuildController",
            method = "run",
            location = @Location(Kind.RETURN))
    public static void afterBuild() {
        BTraceUtils.println("------------ AFTER BUILD -------------------------");
        Aggregations.printAggregation("ArraysCopyOf", "%1$s %2$s %3$s", aggregations);
        BTraceUtils.println("--------------------------------------------------");
        Aggregations.clearAggregation(count);
    }
}
