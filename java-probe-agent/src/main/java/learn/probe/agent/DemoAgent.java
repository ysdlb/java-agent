package learn.probe.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class DemoAgent {

    /**
     * jvm 参数形式启动
     */
    public static void premain(String agentArgs, Instrumentation inst){
        Thread thread = Thread.currentThread();
        System.out.format("%s%s:%s%s -> agentmain start\n",
                "\033[38;5;118m", thread.getThreadGroup().getName(), thread.getName(), "\033[0m");
        inst.addTransformer(new DemoTransformer(), true);
    }

    /**
     * 动态 attach 方式启动
     * retransform 会重新触发一次类加载的过程
     *
     * 如果 目标类 在 attach 之间就完成了类加载, 那么我们调用 retransform 触发重新加载一次没问题
     * 如果 目标类 在 attach 之前从未使用（没有加载
     * 此时 Class.forName 会触发一次类加载, retransform 也会触发一次, 这里会调用两次
     * {@link DemoTransformer#transform()}
     *
     * 仅判断一个类是否被加载过但不触发加载动作但骚操作详见：
     * https://stackoverflow.com/questions/482633/in-java-is-it-possible-to-know-whether-a-class-has-already-been-loaded
     */
    public static void agentmain(String agentArgs, Instrumentation inst){
        Thread thread = Thread.currentThread();
        System.out.format("%s%s:%s%s -> agentmain start\n",
                "\033[38;5;118m", thread.getThreadGroup().getName(), thread.getName(), "\033[0m");

        inst.addTransformer(new DemoTransformer(), true);

        try {
            inst.retransformClasses(Class.forName(DemoTransformer.TARGET_CLASS));
            System.out.println(DemoTransformer.TARGET_CLASS + " has retransfromed");
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }
}