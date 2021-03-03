package classdemo1;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QuizDispatcher extends Thread{
    Queue<ClientHandler> clients;
    Queue<String> toAll;

    public QuizDispatcher() {
        this.clients = new ArrayBlockingQueue<>(255);
        this.toAll = new LinkedBlockingQueue<>();
    }


}
