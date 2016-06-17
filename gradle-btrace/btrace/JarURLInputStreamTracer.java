import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Return;
import com.sun.btrace.annotations.Self;

import static com.sun.btrace.BTraceUtils.Collections.size;
import static com.sun.btrace.BTraceUtils.identityHashCode;
import static com.sun.btrace.BTraceUtils.jstackStr;
import static com.sun.btrace.BTraceUtils.println;

/**
 * Traces unclosed Jar resources
 */
@BTrace(unsafe = true)
public class JarURLInputStreamTracer {
	private static Map<Integer, String> liveStreams = new ConcurrentHashMap<>();
	private static AtomicBoolean buildRunning = new AtomicBoolean(false);

	@OnMethod(
			clazz = "+java.net.JarURLConnection",
			method = "getInputStream",
			location = @Location(Kind.RETURN))
	public static void onGetInputStream(@Self Object thisObject,
			@Return Object inputStreamInstance) {
		liveStreams.put(identityHashCode(inputStreamInstance), jstackStr());
	}

	@OnMethod(
			clazz = "+java.net.InputStream",
			method = "close")
	public static void onClose(@Self Object thisObject) {
		liveStreams.remove(identityHashCode(thisObject));
	}

	@OnMethod(clazz = "+org.gradle.BuildListener",
			method = "buildStarted")
	public static void buildStarted() {
		buildRunning.set(true);
	}

	@OnMethod(clazz = "+org.gradle.BuildListener",
			method = "buildFinished")
	public static void buildFinished() {
		if (buildRunning.compareAndSet(true, false)) {
			println("--------------------------------------------------");
			if (size(liveStreams) > 0) {
				for (String stackTrace : liveStreams.values()) {
					println(stackTrace);
					println();
				}
			}
			else {
				println("No unclosed streams.");
			}
			println("--------------------------------------------------");
		}
	}
}
