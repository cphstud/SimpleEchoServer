package classdemo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


class MyLoader {
    List<String> myStuff;
    public MyLoader(List<String> myStuff) {
        this.myStuff = myStuff;
    }

    public void myLoading(String uri) throws IOException {
        // load from file-system resource
        String line = "";
        //File f = new File(uri);
        // load from classloader resource
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream is = classLoader.getResourceAsStream(uri);
        // from stream to reader
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);

        while((line = br.readLine())!=null ) {
            myStuff.add(line);
        }
        br.close();
        ir.close();
    }

    public void mySaving(String uri, HashMap<Integer, String> input) throws IOException {
        File f = new File(uri);
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);

        Set<Integer> keys = input.keySet();
        for (Integer k : keys) {
            System.out.println("Got " + input.get(k));
            bw.write(k +" -> " + input.get(k));
        }
        bw.close();
        fw.close();
    }
}

public class EchoServer {
    public static final int DEFAULT_PORT = 2345;

    public static void main(String[] args) {
        int port = 8188;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        EchoServer echoServer = new EchoServer();
        try {
            echoServer.runServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void runServer(int port) throws IOException {
        ServerSocket ss = new ServerSocket(port);
        Socket client = ss.accept();
        ClientHandler cl = new ClientHandler(client);
        cl.greeting();
        // pass this to clienthandler

    }
}

class ClientHandler {
    // socket, in and out channel
    Socket client;
    BufferedReader br;
    PrintWriter pw;

    public ClientHandler(Socket client) {
        this.client = client;
        try {
            pw = new PrintWriter(client.getOutputStream(),true);
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            // skal håndteres
            e.printStackTrace();
        }
    }

    public void greeting() throws IOException {
        String name = "";
        pw.println("Hello My Friend. What is your name?");
        name = br.readLine();
        //pw.println("Well Hello " + name);
        pw.printf("Well Hello %s", name);
        pw.close();
        br.close();
    }
}
