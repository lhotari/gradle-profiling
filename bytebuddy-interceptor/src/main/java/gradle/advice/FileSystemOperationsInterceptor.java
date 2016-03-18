package gradle.advice;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.gradle.api.internal.file.collections.DirectoryFileTree;

import java.io.File;

public class FileSystemOperationsInterceptor {
    public static void install() {
        ByteBuddyAgent.install();

        ClassLoader targetClassLoader = File.class.getClassLoader();

        // interceptor class must be injected to the same classloader as the target class that is intercepted
        new ByteBuddy().redefine(CountFileSystemOperations.class)
                .make()
                .load(targetClassLoader,
                        ClassReloadingStrategy.fromInstalledAgent());

        new ByteBuddy().redefine(DirectoryFileTree.class)
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().writerFlags(ClassWriter.COMPUTE_FRAMES)
                        .method(ElementMatchers.named("length"), Advice.to(CountFileSystemOperations.LengthMethod.class))
                        .method(ElementMatchers.named("isFile"), Advice.to(CountFileSystemOperations.IsFileMethod.class))
                        .method(ElementMatchers.named("isDirectory"), Advice.to(CountFileSystemOperations.IsDirectoryMethod.class))
                        .method(ElementMatchers.named("lastModified"), Advice.to(CountFileSystemOperations.LastModifiedMethod.class))
                        .method(ElementMatchers.named("exists"), Advice.to(CountFileSystemOperations.ExistsMethod.class))

                )
                .make()
                .load(targetClassLoader,
                        ClassReloadingStrategy.fromInstalledAgent());
    }
}