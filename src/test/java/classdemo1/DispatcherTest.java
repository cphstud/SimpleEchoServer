package classdemo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {
    /*
        public Dispatcher(BlockingQueue<String> messages, ConcurrentMap<String,PrintWriter> allNamePrintWriters) {
        this.allNamePrintWriters = allNamePrintWriters;
        allMessages = messages;
     */

    BlockingQueue<String> messages;
    ConcurrentMap<String, PrintWriter> allNamedWriters;
    Thread testThread;
    Dispatcher dispatcher;
    PrintWriter pw;
    String name;
    String message;

    @BeforeEach
    void setUp() {
        messages = new ArrayBlockingQueue<>(230);
        allNamedWriters = new ConcurrentHashMap<>();
        dispatcher = new Dispatcher(messages,allNamedWriters);
        pw = new PrintWriter(System.out,true);
        name = "Kurt";
        message = "CONNECT#Kurt";
        dispatcher.addClientToNamedWriter(name,pw);
        testThread = new Thread(dispatcher);
    }

    @Test
    void testConnect() {
        testThread.start();
        messages.add(message);
        pw.println("Kurt");
    }
}