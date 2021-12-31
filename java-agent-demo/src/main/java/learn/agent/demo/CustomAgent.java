package learn.agent.demo;

import java.lang.instrument.Instrumentation;

public class CustomAgent {

    /**
     * jvm 参数形式启动，运行此方法
     */
    public static void premain(String agentArgs, Instrumentation inst){
        System.out.println("premain");
        inst.addTransformer(new CustomFileTransformer(), true);
    }

    /**
     * 动态 attach 方式启动，运行此方法
     */
    public static void agentmain(String agentArgs, Instrumentation inst){
        System.out.println("agentmain");
        inst.addTransformer(new CustomFileTransformer(), true);
    }
}