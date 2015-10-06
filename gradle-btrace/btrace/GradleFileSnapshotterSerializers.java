import com.sun.btrace.AnyType;
import com.sun.btrace.BTraceUtils;
import com.sun.btrace.BTraceUtils.*;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.annotations.*;

import java.util.Collection;
import java.util.Deque;
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
    private static Deque<Aggregation> inputAggregations = BTraceUtils.Collections.newDeque();

    static {
        addLast(inputAggregations, inputAverage);
        addLast(inputAggregations, inputMin);
        addLast(inputAggregations, inputMax);
        addLast(inputAggregations, inputCount);
    }

    private static Aggregation outputAverage = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation outputMax = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation outputMin = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation outputCount = Aggregations.newAggregation(AggregationFunction.COUNT);
    private static Deque<Aggregation> outputAggregations = BTraceUtils.Collections.newDeque();

    static {
        addLast(outputAggregations, outputAverage);
        addLast(outputAggregations, outputMin);
        addLast(outputAggregations, outputMax);
        addLast(outputAggregations, outputCount);
    }

    private static Aggregation outputFileIdsAverage = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation outputFileIdsMax = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation outputFileIdsMin = Aggregations.newAggregation(AggregationFunction.MINIMUM);
    private static Aggregation outputFileIdsCount = Aggregations.newAggregation(AggregationFunction.COUNT);
    private static Deque<Aggregation> outputFileIdsAggregations = BTraceUtils.Collections.newDeque();

    static {
        addLast(outputFileIdsAggregations, outputFileIdsAverage);
        addLast(outputFileIdsAggregations, outputFileIdsMin);
        addLast(outputFileIdsAggregations, outputFileIdsMax);
        addLast(outputFileIdsAggregations, outputFileIdsCount);
    }

    @OnMethod(
            clazz = "org.gradle.api.internal.changedetection.state.DefaultFileSnapshotterSerializer",
            method = "write"
    )
    public static void writeSnapshot(AnyType[] args) {
        Object snapshotImpl = args[1];
        Map<Object, Object> snapshots = (Map<Object, Object>) Reflective.get(Reflective.field(classOf(snapshotImpl), "snapshots"), snapshotImpl);
        int snapshotSize = size(snapshots);
        Collection<Aggregation> aggregations = outputfiles ? outputAggregations : inputAggregations;
        Object[] aggregationsArray = BTraceUtils.Collections.toArray(aggregations);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[0], snapshotSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[1], snapshotSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[2], snapshotSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[3], snapshotSize);
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
        Collection<Aggregation> aggregations = outputFileIdsAggregations;
        Object[] aggregationsArray = BTraceUtils.Collections.toArray(aggregations);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[0], rootFileIdsSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[1], rootFileIdsSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[2], rootFileIdsSize);
        Aggregations.addToAggregation((Aggregation)aggregationsArray[3], rootFileIdsSize);
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
            method = "run",
            location = @Location(Kind.RETURN))
    public static void reset() {
        BTraceUtils.println("--------------------------------------------------");
        Aggregations.printAggregation("inputs", "", inputAggregations);
        Aggregations.printAggregation("outputs", "", outputAggregations);
        Aggregations.printAggregation("outputFileIds", "", outputFileIdsAggregations);
        BTraceUtils.println("--------------------------------------------------");
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
    }
}
