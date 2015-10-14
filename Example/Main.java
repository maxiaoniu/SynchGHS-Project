import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;

class Command {
    public int src;
    public int des;
}

class Router extends Thread {
    public ArrayBlockingQueue<Command> inputQueue = new ArrayBlockingQueue<>(1024,true);
    private ArrayBlockingQueue<Command>[] outputQueue;

    public Router(int n) {
        outputQueue = new ArrayBlockingQueue[n];
        for (int i = 0; i < n; i++) {
            outputQueue[i] = new ArrayBlockingQueue<>(1024,true);
        }
    }

    public ArrayBlockingQueue<Command> getInputQueue(int id) {
        return outputQueue[id];
    }
    public void run() {
        while(true) {
            //block action
            Command cmd;
            try {
                List<Command> cmds = new ArrayList<>();
                if(inputQueue.drainTo(cmds) == 0)
                    cmds.add(inputQueue.take());
                for (int i = 0; i < cmds.size(); i++) {
                    cmd = cmds.get(i);
                    int destination = cmd.des;
                    outputQueue[destination].put(cmd);

                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}

class Master extends Thread {
    private ArrayBlockingQueue<Command> in;
    private ArrayBlockingQueue<Command> out;

    public  Master(Router router) {
        in = router.getInputQueue(0); //assume the master's id is 0
        out = router.inputQueue;

    }

    public void send(Command cmd) {
        try {
            out.put(cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // the read method is block action
    public Command read() {
        Command cmd;
        try {
            cmd = in.take();
        } catch (Exception e) {
            System.out.println("Master is throwing exception");
            throw new RuntimeException(e);
        }
        return cmd;
    }

    public void run() {



        while(true) {
            Command sendCmd1 = new Command();
            sendCmd1.src = 0;
            sendCmd1.des = 1;
            this.send(sendCmd1);
            Command sendCmd2 = new Command();
            sendCmd2.src = 0;
            sendCmd2.des = 2;
            this.send(sendCmd2);
            Command sendCmd3 = new Command();
            sendCmd3.src = 0;
            sendCmd3.des = 3;
            this.send(sendCmd3);
            Command cmd =  this.read(); //block until get a command
            // node is too fast
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class Node extends Thread {
    private int nodeId;
    private ArrayBlockingQueue<Command> in;
    private ArrayBlockingQueue<Command> out;

    public  Node(Router router, int id) {
        nodeId = id;
        in = router.getInputQueue(id);
        out = router.inputQueue;

    }

    public void send(Command cmd) {
        try {
            out.put(cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // the read method is block action
    public Command read() {

        Command cmd;
        try {
            cmd = in.take();
        } catch (Exception e) {
            System.out.println("node is throwing exception");
            throw new RuntimeException(e);
        }
        return cmd;
    }

    public void run() {

        while(true) {
            Command cmd =  this.read(); //block until get a command
            System.out.println(cmd.src+"--->"+cmd.des);
            Command sendCmd = new Command();

            sendCmd.src = nodeId;
            sendCmd.des = nodeId - 1;
            send(sendCmd);
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws Exception {

        int nodeNum = 4;
        Router routerNode = new Router(nodeNum);
        Master master = new Master(routerNode);

        Node node1 = new Node(routerNode,1);
        Node node2 = new Node(routerNode,2);
        Node node3 = new Node(routerNode,3);
        routerNode.start();

        master.start();
        node1.start();
        node2.start();
        node3.start();
    }
}
