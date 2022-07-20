package learn.probe.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolTransformer implements ClassFileTransformer {
    private final static String EXECUTE_METHOD_NAME = "execute";
    private final static String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private final static String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";
    private final static String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";
    private final static String THREAD_POOL_EXECUTOR_CLASS_NAME_V2 = "java/util/concurrent/ThreadPoolExecutor";
    private final static String S_THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ScheduledThreadPoolExecutor";

    /*
     * ThreadPoolExecutor 中涉及到 Runnable 和 Callable 的方法
     *
     * callable: java.util.concurrent.AbstractExecutorService.newTaskFor(java.util.concurrent.Callable)
     * runnable: java.util.concurrent.ThreadPoolExecutor.beforeExecute(java.lang.Thread,java.lang.Runnable)
     * callable: java.util.concurrent.AbstractExecutorService.submit(java.util.concurrent.Callable)
     * runnable: java.util.concurrent.ThreadPoolExecutor.afterExecute(java.lang.Runnable,java.lang.Throwable)
     * runnable: java.util.concurrent.ThreadPoolExecutor.remove(java.lang.Runnable)
     * runnable: java.util.concurrent.ThreadPoolExecutor.reject(java.lang.Runnable)
     * runnable: java.util.concurrent.AbstractExecutorService.newTaskFor(java.lang.Runnable,java.lang.Object)
     * runnable: java.util.concurrent.AbstractExecutorService.submit(java.lang.Runnable)
     * runnable: java.util.concurrent.AbstractExecutorService.submit(java.lang.Runnable,java.lang.Object)
     * runnable: java.util.concurrent.ThreadPoolExecutor.execute(java.lang.Runnable)
     */

    /**
     * 关于 Callable 以及其他携带 Runnable 或 Callable 的方法是否需要代理的讨论:
     * 看 {@link ThreadPoolExecutor ThreadPoolExecutor} 的源代码, 发现它其实是一个消费者和生产者的组合
     * <p></p>
     *
     * 消费者为 {@link ThreadPoolExecutor#runWorker runWorker}, 它由非静态内部类对象 Worker 实例
     * 的 {@link ThreadPoolExecutor.Worker#run() run()} 方法来调用
     * <p></p>
     *
     * 生产者为 {@link ThreadPoolExecutor#execute(Runnable)}, 其他所有提交异步代码块的方法, 其参数总会封装为 一个 Runnable 子类,
     * 最后提交给 execute 方法.<br>
     * 1. {@link ThreadPoolExecutor#submit(Runnable)}: 直接提交 <br>
     * 2. {@link ThreadPoolExecutor#submit(Callable)}: 封装为一个 Runnable + Future 接口 {@link java.util.concurrent.RunnableFuture RunnableFuture} 的实例, 将实例返回作为句柄接收计算结果 <br>
     * 3. {@link ThreadPoolExecutor#submit(Runnable, Object)}: 同上 Callable <br>
     * <p></p>
     *
     * 综上, 是否可以只增强 {@link ThreadPoolExecutor#execute(Runnable)} 就可以完成对上下文跨线程传递的功能
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!THREAD_POOL_EXECUTOR_CLASS_NAME_V2.equals(className)) return null;

        ClassPool classPool = ClassPool.getDefault();
        try {
            CtClass cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer), false);
            CtMethod[] methods = cl.getMethods();
            for (CtMethod method: methods) {
                CtClass[] parameterTypes = method.getParameterTypes();
                if (EXECUTE_METHOD_NAME.equals(method.getName())
                        && parameterTypes.length == 1 && RUNNABLE_CLASS_NAME.equals(parameterTypes[0].getName())) {
                    String code = String.format("$%d = learn.probe.agent.trace.RunnableWrapper.wrap($%<d);", 1);
                    method.insertBefore(code);
                    System.out.format("%s%s%s\n",
                            "\033[38;5;13m", code, "\033[0m");
                }
//                for (int i = 0; i < parameterTypes.length; i++) {
//                    CtClass parameterType = parameterTypes[i];
//                    if ( RUNNABLE_CLASS_NAME.equals(parameterType.getName())) {
//                        System.out.printf("runnable: %s%n", method.getLongName());
//                        method.insertBefore(String.format("$%d = learn.probe.agent.trace.RunnableWrapper.wrap($%<d);", i+1));
//                    } else if (CALLABLE_CLASS_NAME.equals(parameterType.getName())) {
//                        System.out.printf("callable: %s%n", method.getLongName());
//                    }
//                }
            }
            System.out.format("%s%s%s\n",
                    "\033[38;5;116m", "完成 " + THREAD_POOL_EXECUTOR_CLASS_NAME + " 增强", "\033[0m");
            return cl.toBytecode();
        } catch (IOException | NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

}
