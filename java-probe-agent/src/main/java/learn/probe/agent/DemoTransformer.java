package learn.probe.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class DemoTransformer implements ClassFileTransformer {
    public static final String TARGET_CLASS = "learn.probe.app.MyService";
    private static final String TARGET_CLASS_V2 = "learn/probe/app/MyService";
    private static final String TARGET_METHOD = "doSomething";

    {
        // "~/logs/dump" 这样的目录不认, 把它当文件名字使类
        CtClass.debugDump = "~/logs/dump";
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(TARGET_CLASS_V2)) return classfileBuffer;
        // idea premain 启动, 非 debug 模式下无脑 System.out 会报错, 所以正常启动模式下不要把这一句放最前面
        System.out.println("检测到加载 -> " + className);

        try {
            ClassPool cp = getClassPool();

            CtClass cl = cp.makeClass(new ByteArrayInputStream(classfileBuffer), false);
            CtMethod ctMethod = cl.getDeclaredMethod(TARGET_METHOD);

            System.out.println("获方法名称："+ ctMethod.getName());
            ctMethod.insertBefore("System.out.println(\"before advice\");");
            ctMethod.insertAfter("System.out.println(\"after advice; the method return: \" + $_);");
            System.out.println("已完成增强!!!!");

            return cl.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }

    /**
     * javassist 默认的 ClassPool 会使用实例方法 {@link Thread#getContextClassLoader()} 获得当前线程的
     * 类加载器, 将它的 path 作为自己的扫描路径
     *
     * <p>
     * premain 启动是由 普通应用进程启动的
     * 一般 {@link ClassFileTransformer} 被 add 进 {@link Instrumentation}后;
     * 应用线程在加载某个类的时候执行执行 {@link ClassFileTransformer#transform} 方法,
     * 进而用同一个线程调用该方法, 应用线程总有一个正常的 AppClassLoader,
     * 所以这个 ClassLoader 既可以加载 app jar 包里的类 (增强相关的类), 也可以加载 agent jar 包里的类 (增强目标)
     *
     * <p>
     * agentmain 启动是由 额外的进程启动的, 此时应用进程早已在运行中了. agentmain 里的指令由这个额外的进程来执行
     * 一般叫 group 是 system, 线程名是 Attach Listener, <em>该线程不在 app 进程中, 属于系统线程, 它没有类加载器</em>
     * </p>
     *
     * <p>
     * agentmain 启动后, 我们的增强目标可能加载过了, 也可能没加载过;
     *   1. jvm 只允许我们在获取字节码的时候对其增强, 所以如果加载过了, 我们需要调用 {@link Instrumentation#retransformClasses(Class[])}
     *      重新加载一次, 它的参数是 Class 对象, 在只有全限名的情况下, 这个对象我现在只会通过 Class.forName 来获取
     *   2. 确定一个类没有被加载过, 还能不触发加载动作, 我不知道该怎么做, 起码 Class.forName 没有这个能力; 所以我们总使用 1 的方法调用
     *   {@link Instrumentation#retransformClasses(Class[])} 方法
     *
     * ps: 2 会对一份字节码执行两遍 {@link ClassFileTransformer#transform} 的逻辑, 可以通过一定的逻辑来保证同一个
     *     {@link ClassFileTransformer} 不会对一份字节码生效两次
     * </p>
     */
    private static ClassPool getClassPool() {
        ClassPool classPool = ClassPool.getDefault();
        if (Thread.currentThread().getContextClassLoader() != null)
            return classPool;

        Thread thread = Thread.currentThread();
        System.out.format("%s%s:%s%s -> no class loader, so we do it.\n",
                "\033[38;5;118m", thread.getThreadGroup().getName(), thread.getName(), "\033[0m");
//        classPool.appendClassPath(new LoaderClassPath(ClassLoaders.appClassLoader()));
        classPool.insertClassPath(new ClassClassPath(Object.class));
        classPool.getClassLoader();
        return classPool;
    }
}
