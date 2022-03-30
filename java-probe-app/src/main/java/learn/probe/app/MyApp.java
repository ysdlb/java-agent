package learn.probe.app;

import java.util.Scanner;

public class MyApp {

    public static void main(String[] args){
        Thread thread = Thread.currentThread();
        System.out.format("%s%s:%s%s -> main start\n",
                "\033[38;5;118m", thread.getThreadGroup().getName(), thread.getName(), "\033[0m");
        System.out.println("########### enter in 1 do something ############");

        int number;
        do {
            Scanner reader = new Scanner(System.in);
            number = reader.nextInt();
            MyService myService = new MyService();
            String ret = myService.doSomething();
            System.out.println(ret);
        } while (number == 1);
    }

}
