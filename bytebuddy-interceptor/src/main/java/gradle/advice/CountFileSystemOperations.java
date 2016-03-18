package gradle.advice;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CountFileSystemOperations {
    public static ConcurrentHashMap<String, AtomicInteger> COUNTS = new ConcurrentHashMap<String, AtomicInteger>();

    public static void incrementCounter(String key) {
        AtomicInteger count = COUNTS.get(key);
        if (count == null) {
            count = new AtomicInteger(0);
        }
        AtomicInteger existing = COUNTS.putIfAbsent(key, count);
        if (existing != null) {
            count = existing;
        }
        count.incrementAndGet();
    }

    public synchronized static void reset() {
        COUNTS.clear();
    }

    public static class LengthMethod {
        @Advice.OnMethodEnter
        public synchronized static void methodCalled() {
            incrementCounter("length");
        }
    }

    public static class IsFileMethod {
        @Advice.OnMethodEnter
        public synchronized static void methodCalled() {
            incrementCounter("isFile");
        }
    }

    public static class IsDirectoryMethod {
        @Advice.OnMethodEnter
        public synchronized static void methodCalled() {
            incrementCounter("isDirectory");
        }
    }

    public static class ExistsMethod {
        @Advice.OnMethodEnter
        public synchronized static void methodCalled() {
            incrementCounter("exists");
        }
    }

    public static class LastModifiedMethod {
        @Advice.OnMethodEnter
        public synchronized static void methodCalled() {
            incrementCounter("lastModified");
        }
    }
}