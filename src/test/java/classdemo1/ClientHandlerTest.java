package classdemo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.ClientInfoStatus;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {
    /*
    BlockingQueue<String> users = new ArrayBlockingQueue<>(250);
    ConcurrentMap<String, PrintWriter> allNamedPrintwriters = new ConcurrentHashMap<>();
    //Dispatcher dispatcher  = new Dispatcher(messages, users);
    Dispatcher dispatcher2  = new Dispatcher(messages, allNamedPrintwriters);
    public static final int DEFAULT_PORT = 2345;
     */
    BlockingQueue<String> messages;
    ClientHandler cl;
    Dispatcher dispatcher;
    BufferedReader br;
    PrintWriter pw;
    Thread t;

    @BeforeEach
    void setUp() {
        messages = new ArrayBlockingQueue<>(250);
        String content = "Kurt";
        String userInput = String.format("SEND#Lone#Du er smuk%nCLOSE#%n");
        br = new BufferedReader(new StringReader(userInput));
        pw = new PrintWriter(System.out,true);
        MyLoader ml = new MyLoader();
        cl = new ClientHandler(content, br, pw, ml, messages);
        t = new Thread(cl);
    }
     @Test
    void testClientSendMsg() {
        t.start();
     }
}