package learn.agent.demo.app;

import java.util.Scanner;

public class DemoApp {

    public static void main(String[] args){
        System.out.println("########### enter in 1 do something ############");

        int number;
        do {
            Scanner reader = new Scanner(System.in);
            number = reader.nextInt();
            DemoService demoService = new DemoService();
            demoService.doSomething();
        } while (number == 1);
    }

}
