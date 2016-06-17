import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Self;

import static com.sun.btrace.BTraceUtils.Collections.size;
import static com.sun.btrace.BTraceUtils.identityHashCode;
import static com.sun.btrace.BTraceUtils.jstackStr;
import static com.sun.btrace.BTraceUtils.println;

/**
 * Traces unclosed Jar files
 */
@BTrace(unsafe = true)
public class JarFileTracer {
	private static Map<Integer, String> openFiles = new ConcurrentHashMap<>();
	private static AtomicBoolean buildRunning = new AtomicBoolean(false);

	@OnMethod(
			clazz = "java.util.jar.JarFile",
			method = "<init>")
	public static void onGetInputStream(@Self Object thisObject) {
		openFiles.put(identityHashCode(thisObject), jstackStr());
	}

	@OnMethod(
			clazz = "java.util.jar.JarFile",
			method = "close")
	public static void onClose(@Self Object thisObject) {
		openFiles.remove(identityHashCode(thisObject));
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
			if (size(openFiles) > 0) {
				for (String stackTrace : openFiles.values()) {
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
