package learn.probe.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ThreadPoolTransformer implements ClassFileTransformer {
    private final static String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private final static String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";
    private final static String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";
    private final static String THREAD_POOL_EXECUTOR_CLASS_NAME_V2 = "java/util/concurrent/ThreadPoolExecutor";
    private final static String S_THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ScheduledThreadPoolExecutor";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!THREAD_POOL_EXECUTOR_CLASS_NAME_V2.equals(className)) return null;

        ClassPool classPool = ClassPool.getDefault();
        try {
            CtClass cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer), false);
            CtMethod[] methods = cl.getMethods();
            for (CtMethod method: methods) {
                CtClass[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    CtClass parameterType = parameterTypes[i];
                    if (RUNNABLE_CLASS_NAME.equals(parameterType.getName())) {
                        System.out.printf("runnable: %s%n", method.getLongName());
                        method.insertBefore(String.format("$%d = learn.probe.agent.trace.RunnableWrapper.wrap($%<d);", i+1));
                    } else if (CALLABLE_CLASS_NAME.equals(parameterType.getName())) {
                        System.out.printf("callable: %s%n", method.getLongName());
                    }
                }
            }
            System.out.println("完成" + THREAD_POOL_EXECUTOR_CLASS_NAME + "增强");
            return cl.toBytecode();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

}
