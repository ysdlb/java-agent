package learn.agent.demo.attach;

import com.sun.tools.attach.VirtualMachine;

public class DemoAttach {

    public static void main(String[] args) throws Exception{
        if (args.length < 2) return;
        String pid = args[0], agentJarPath = args[1];
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentJarPath);
    }

}
