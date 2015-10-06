import com.sun.btrace.AnyType;
import com.sun.btrace.BTraceUtils;
import com.sun.btrace.BTraceUtils.Aggregations;
import com.sun.btrace.BTraceUtils.Reflective;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.*;

import java.util.Map;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class GradleFileSnapshotterSerializers {
    @TLS
    private static boolean outputfiles = false;

    private static Aggregation inputAverage = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation inputMax = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation inputMin = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation inputCount = Aggregations.newAggregation(AggregationFunction.COUNT);

    private static Aggregation outputAverage = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation outputMax = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation outputMin = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation outputCount = Aggregations.newAggregation(AggregationFunction.COUNT);

    private static Aggregation outputFileIdsAverage = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation outputFileIdsMax = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation outputFileIdsMin = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation outputFileIdsCount = Aggregations.newAggregation(AggregationFunction.COUNT);

    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.DefaultFileSnapshotterSerializer",
            method = "write"
    )
    public static void writeSnapshot(AnyType[] args) {
        Object snapshotImpl = args[1];
        Map<Object, Object> snapshots = (Map<Object, Object>) Reflective.get(Reflective.field(classOf(snapshotImpl), "snapshots"), snapshotImpl);
        int snapshotSize = size(snapshots);
        if(outputfiles) {
            Aggregations.addToAggregation(outputAverage, snapshotSize);
            Aggregations.addToAggregation(outputMin, snapshotSize);
            Aggregations.addToAggregation(outputMax, snapshotSize);
            Aggregations.addToAggregation(outputCount, snapshotSize);
        } else {
            Aggregations.addToAggregation(inputAverage, snapshotSize);
            Aggregations.addToAggregation(inputMin, snapshotSize);
            Aggregations.addToAggregation(inputMax, snapshotSize);
            Aggregations.addToAggregation(inputCount, snapshotSize);
        }
    }

    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.OutputFilesSnapshotSerializer",
            method = "write"
    )
    public static void writeOutputSnapshot(AnyType[] args) {
        outputfiles = true;
        Object snapshotImpl = args[1];
        Map<Object, Object> rootFileIds = (Map<Object, Object>) Reflective.get(Reflective.field(classOf(snapshotImpl), "rootFileIds"), snapshotImpl);
        int rootFileIdsSize = size(rootFileIds);
        Aggregations.addToAggregation(outputFileIdsAverage, rootFileIdsSize);
        Aggregations.addToAggregation(outputFileIdsMin, rootFileIdsSize);
        Aggregations.addToAggregation(outputFileIdsMax, rootFileIdsSize);
        Aggregations.addToAggregation(outputFileIdsCount, rootFileIdsSize);
    }

    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.OutputFilesSnapshotSerializer",
            method = "write",
            location = @Location(Kind.RETURN)
    )
    public static void writeOutputSnapshotReturn() {
        outputfiles = false;
    }

    @OnMethod(clazz = "org.gradle.launcher.exec.InProcessBuildActionExecuter$DefaultBuildController",
            method = "run")
    public static void beforeBuild() {
        BTraceUtils.println("------------ BEFORE BUILD -------------------------");
        Aggregations.printAggregation("inputs avg", inputAverage);
        Aggregations.printAggregation("inputs min", inputMin);
        Aggregations.printAggregation("inputs max", inputMax);
        Aggregations.printAggregation("inputs count", inputCount);

        Aggregations.printAggregation("outputs avg", outputAverage);
        Aggregations.printAggregation("outputs min", outputMin);
        Aggregations.printAggregation("outputs max", outputMax);
        Aggregations.printAggregation("outputs count", outputCount);

        Aggregations.printAggregation("outputFileIds avg", outputFileIdsAverage);
        Aggregations.printAggregation("outputFileIds min", outputFileIdsMin);
        Aggregations.printAggregation("outputFileIds max", outputFileIdsMax);
        Aggregations.printAggregation("outputFileIds count", outputFileIdsCount);

        BTraceUtils.println("--------------------------------------------------");
    }

    @OnMethod(clazz = "org.gradle.launcher.exec.InProcessBuildActionExecuter$DefaultBuildController",
            method = "run",
            location = @Location(Kind.RETURN))
    public static void afterBuild() {
        BTraceUtils.println("------------ AFTER BUILD -------------------------");
        Aggregations.printAggregation("inputs avg", inputAverage);
        Aggregations.printAggregation("inputs min", inputMin);
        Aggregations.printAggregation("inputs max", inputMax);
        Aggregations.printAggregation("inputs count", inputCount);

        Aggregations.printAggregation("outputs avg", outputAverage);
        Aggregations.printAggregation("outputs min", outputMin);
        Aggregations.printAggregation("outputs max", outputMax);
        Aggregations.printAggregation("outputs count", outputCount);

        Aggregations.printAggregation("outputFileIds avg", outputFileIdsAverage);
        Aggregations.printAggregation("outputFileIds min", outputFileIdsMin);
        Aggregations.printAggregation("outputFileIds max", outputFileIdsMax);
        Aggregations.printAggregation("outputFileIds count", outputFileIdsCount);

        BTraceUtils.println("--------------------------------------------------");

        /*
        Aggregations.clearAggregation(inputAverage);
        Aggregations.clearAggregation(inputMax);
        Aggregations.clearAggregation(inputMin);
        Aggregations.clearAggregation(inputCount);

        Aggregations.clearAggregation(outputAverage);
        Aggregations.clearAggregation(outputMax);
        Aggregations.clearAggregation(outputMin);
        Aggregations.clearAggregation(outputCount);

        Aggregations.clearAggregation(outputFileIdsAverage);
        Aggregations.clearAggregation(outputFileIdsMax);
        Aggregations.clearAggregation(outputFileIdsMin);
        Aggregations.clearAggregation(outputFileIdsCount);
        */
    }
}
