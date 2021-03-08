package classdemo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class DispatcherTest {
    /*
     public Dispatcher(BlockingQueue<String> messages, ConcurrentMap<String,PrintWriter> allNamePrintWriters) {
     this.allNamePrintWriters = allNamePrintWriters;
     this.allNamedSockets = new ConcurrentHashMap<>();
     allMessages = messages;
 }

     */
    BlockingQueue<String> allMsg;
    String msg;
    Thread testThread;
    PrintWriter pw;

    @BeforeEach
    void setUp() {
        allMsg = new ArrayBlockingQueue<>(240);
        msg = "SEND#Kurt,Lone#Hej med dig";
        pw = new PrintWriter(System.out);
        Dispatcher dispatcher = new Dispatcher(allMsg);
        testThread = new Thread(dispatcher);
    }

    @Test
    void testWriteMessag() {
        testThread.start();
    }

}