import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.matcher.ElementMatchers;
import org.gradle.api.internal.file.collections.DirectoryFileTree;

import java.io.File;

public class DirectoryScanningInterceptor {

    public static void install() {
        ByteBuddyAgent.install();

        new ByteBuddy().redefine(DirectoryFileTree.class)
                .visit(new AsmVisitorWrapper.ForDeclaredMethods().writerFlags(ClassWriter.COMPUTE_FRAMES).method(ElementMatchers.named("visitFrom"), Advice.to(CountVisits.class)))
                .make()
                .load(DirectoryFileTree.class.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());
    }

    public static class CountVisits {
        @Advice.OnMethodEnter
        public static void interceptVisitFrom(@Advice.Argument(1) File fileOrDirectory) {
            System.out.println("visiting " + fileOrDirectory);
        }
    }
}
