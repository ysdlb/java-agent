package learn.probe.util;

import com.sun.tools.attach.VirtualMachine;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * 将一个 agent attach 到一个类上的工具方法
 */
public class AttachUtil {

    private static final String COLOR_1 = "\033[38;2;0;255;255m";
    private static final String COLOR_2 = "\033[38;2;255;0;255m";
    private static final String COLOR_3 = "\033[38;2;255;255;0m";
    private static final String COLOR_RESET = "\033[0m";

    /**
     * {@link com.sun.tools.attach.VirtualMachine}
     * 这个类代表一个JVM抽象，可以通过这个类找到目标JVM，并且将 Agent 挂载到目标JVM上。
     */
    public static void main(String[] args) throws Exception{
        PrintStream printS = System.out;
        printS.format("%s%-9s exec%s\n", COLOR_1, "pid", COLOR_RESET);
        VirtualMachine.list().forEach(vmDesc -> printS.format(
                "%s%-9s %s%s\n", COLOR_3, vmDesc.id(), vmDesc.displayName(), COLOR_RESET
        ));
        printS.format("%s%s%s", COLOR_2, "please input attached pid: ", COLOR_RESET);
        String agentJarPath = args[0];
        String pid = String.valueOf(readPid());
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentJarPath);
        // learn from net.bytebuddy.agent.Attacher#install
        vm.detach();
    }

    private static int readPid() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }
}
