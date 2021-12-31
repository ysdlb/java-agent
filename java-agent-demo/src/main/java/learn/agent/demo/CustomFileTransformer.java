package learn.agent.demo;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class CustomFileTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"learn/agent/app/DemoService".equals(className)) return classfileBuffer;
        // idea premain 启动, 非 debug 模式下无脑 System.out 会报错, 所以正常启动模式下不要把这一句放最前面
        System.out.println("检测到加载 -> " + className);
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
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
}
