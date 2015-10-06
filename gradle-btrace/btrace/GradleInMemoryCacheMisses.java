import com.sun.btrace.BTraceUtils;
import com.sun.btrace.BTraceUtils.Aggregations;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.aggregation.AggregationKey;
import com.sun.btrace.annotations.*;

import java.util.Deque;

import static com.sun.btrace.BTraceUtils.addLast;
import static com.sun.btrace.BTraceUtils.classOf;

@BTrace
public class GradleInMemoryCacheMisses {
    @TLS
    private static boolean inCacheGet = false;
    @TLS
    private static String currentCacheId = null;

    private static Aggregation average = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation max = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation min = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation count = Aggregations.newAggregation(AggregationFunction.COUNT);
    private static Deque<Aggregation> aggregations = BTraceUtils.Collections.newDeque();

    static {
        addLast(aggregations, average);
        addLast(aggregations, min);
        addLast(aggregations, max);
        addLast(aggregations, count);
    }

    // javap ./subprojects/core/build/classes/main/org/gradle/api/internal/changedetection/state/InMemoryTaskArtifactCache\$1.class
    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.InMemoryTaskArtifactCache$1",
            method = "get"
    )
    public static void cacheGet(@Self Object cacheInstance) {
        inCacheGet = true;
        currentCacheId = (String) BTraceUtils.Reflective.get(BTraceUtils.Reflective.field(classOf(cacheInstance), "val$cacheId"), cacheInstance);
    }

    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.InMemoryTaskArtifactCache$1",
            method = "get",
            location = @Location(Kind.RETURN)
    )
    public static void cacheGetReturn(@Duration long duration) {
        if (inCacheGet) {
            AggregationKey key = Aggregations.newAggregationKey(currentCacheId + ".hit");
            Aggregations.addToAggregation(average, key, duration);
            Aggregations.addToAggregation(min, key, duration);
            Aggregations.addToAggregation(max, key, duration);
            Aggregations.addToAggregation(count, key, duration);
            inCacheGet = false;
        }
    }

    @OnMethod(
            clazz = "org.gradle.cache.internal.DefaultMultiProcessSafePersistentIndexedCache",
            method = "get",
            location = @Location(Kind.RETURN)
    )
    public static void cacheGetPersistentReturn(@Duration long duration) {
        if (inCacheGet) {
            AggregationKey key = Aggregations.newAggregationKey(currentCacheId + ".miss");
            Aggregations.addToAggregation(average, key, duration);
            Aggregations.addToAggregation(min, key, duration);
            Aggregations.addToAggregation(max, key, duration);
            Aggregations.addToAggregation(count, key, duration);
            inCacheGet = false;
        }
    }

    @OnMethod(clazz = "org.gradle.launcher.exec.InProcessBuildActionExecuter$DefaultBuildController",
            method = "run",
            location = @Location(Kind.RETURN))
    public static void afterBuild() {
        BTraceUtils.println("------------ AFTER BUILD -------------------------");
        Aggregations.printAggregation("GradleInMemoryCacheMisses", "%1$s %2$s %3$s %4$s %5$s", aggregations);
        BTraceUtils.println("--------------------------------------------------");
        Aggregations.clearAggregation(average);
        Aggregations.clearAggregation(min);
        Aggregations.clearAggregation(max);
        Aggregations.clearAggregation(count);
    }
}
