package learn.agent.demo;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class CustomFileTransformer implements ClassFileTransformer {

    {
        // "~/logs/dump" 这样的目录不认, 把它当文件名字使类
        CtClass.debugDump = "~/logs/dump";
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.endsWith("DemoService")) return classfileBuffer;
        // idea premain 启动, 非 debug 模式下无脑 System.out 会报错, 所以正常启动模式下不要把这一句放最前面
        System.out.println("检测到加载 -> " + className);

        try {
            ClassPool cp = getClassPool();
            CtClass cl = cp.makeClass(new ByteArrayInputStream(classfileBuffer), false);
            if (cl.isFrozen()) {
                System.out.format("%s has enhanced, now is frozen");
                return classfileBuffer;
            }

            CtMethod ctMethod = cl.getDeclaredMethod("doSomething");

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
     * javassist 默认的 ClassPool 会使用 当前线程的
     * {@link Thread#currentThread()} {@link Thread#getContextClassLoader()}
     * 获取类加载器作为扫描路径
     *
     * 一般 {@link ClassFileTransformer} 被 add 进 {@link java.lang.instrument.Instrumentation}
     * 后；应用线程在加载某个类的时候执行该方法，应用线程又一个正常的 AppClassLoader
     *
     * {@link java.lang.instrument.Instrumentation#retransformClasses(Class[])} 会由一个名叫
     * system:Attach Listener 的线程来执行此方法, <em>该线程没有类加载器</em>
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
        return classPool;
    }
}
