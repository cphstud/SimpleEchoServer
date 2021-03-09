package classdemo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DispatcherTest {
    /*
     public Dispatcher(BlockingQueue<String> messages, ConcurrentMap<String,PrintWriter> allNamePrintWriters) {
     this.allNamePrintWriters = allNamePrintWriters;
     this.allNamedSockets = new ConcurrentHashMap<>();
     allMessages = messages;
 }

     */
    BlockingQueue<String> allMsg;
    ConcurrentMap<String, PrintWriter> allNamedPrintwriters;
    ConcurrentMap<String, Socket> allNamedSockets;
    String msg,msg2,msg3;
    Thread testThread;
    PrintWriter pw,pw2;
    Socket s,s2;

    @BeforeEach
    void setUp() {
        s = mock(Socket.class);
        s2 = mock(Socket.class);
        allNamedPrintwriters = new ConcurrentHashMap<>();
        allNamedSockets = new ConcurrentHashMap<>();
        allMsg = new ArrayBlockingQueue<>(240);
        msg = "SEND#Kurt,Lone#Hej med dig";
        msg2 = "CLOSE#Kurt";
        msg3 = "CONNECT#Kurt";
        pw = new PrintWriter(System.out,true);
        pw2 = new PrintWriter(System.out,true);
        allNamedPrintwriters.put("Kurt",pw);
        allNamedPrintwriters.put("Lone",pw2);
        Dispatcher dispatcher = new Dispatcher(allMsg,allNamedPrintwriters);
        dispatcher.addSocketToList("Kurt",s);
        dispatcher.addSocketToList("Lone",s2);
        testThread = new Thread(dispatcher);
    }

    @Test
    void testWriteMessag() {
        testThread.start();
        allMsg.add(msg);
    }
    @Test
    void testCloseMessage() {
        testThread.start();
        allMsg.add(msg2);
    }

    @Test
    void testConnectMessage() {
        testThread.start();
        allMsg.add(msg3);
    }

}