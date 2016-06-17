import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.OnTimer;
import com.sun.btrace.annotations.Self;

import static com.sun.btrace.BTraceUtils.Atomic.incrementAndGet;
import static com.sun.btrace.BTraceUtils.Atomic.newAtomicInteger;
import static com.sun.btrace.BTraceUtils.Collections.get;
import static com.sun.btrace.BTraceUtils.Collections.newHashMap;
import static com.sun.btrace.BTraceUtils.Collections.put;
import static com.sun.btrace.BTraceUtils.Collections.size;
import static com.sun.btrace.BTraceUtils.classOf;
import static com.sun.btrace.BTraceUtils.compare;
import static com.sun.btrace.BTraceUtils.jstackStr;
import static com.sun.btrace.BTraceUtils.name;
import static com.sun.btrace.BTraceUtils.printNumberMap;

@BTrace
public class URLClassLoaderCreation {
	private static Map<String, AtomicInteger> histo = newHashMap();

	@OnMethod(
			clazz = "java.net.URLClassLoader",
			method = "<init>")
	public static void onInit(@Self Object thisObject) {
		if (compare(name(classOf(thisObject)), "java.net.URLClassLoader")) {
			String stackTrace = jstackStr(6);
			AtomicInteger counter = get(histo, stackTrace);
			if (counter == null) {
				counter = newAtomicInteger(1);
				put(histo, stackTrace, counter);
			}
			else {
				incrementAndGet(counter);
			}
		}
	}

	@OnTimer(5000)
	public static void print() {
		if (size(histo) != 0) {
			printNumberMap("\n\nStacktrace Histogram for 'new URLClassLoader()'", histo);
		}
	}
}
