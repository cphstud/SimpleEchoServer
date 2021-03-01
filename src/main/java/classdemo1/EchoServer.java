package classdemo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;


class MyLoader {
    List<String> myStuff;

    public MyLoader() {
        this.myStuff = new LinkedList<>();
    }

    public void playWithIO() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream is = classLoader.getResourceAsStream("output");
        URL url = classLoader.getResource("output");
        File f = new File("src/main/resources/output");
        //FileReader fr = new FileReader(f);
        Reader fr = new FileReader(f);
        System.out.printf("%c\n",fr.read());
        System.out.printf("%c\n",fr.read());
        System.out.printf("%c\n",fr.read());
        BufferedReader br2 = new BufferedReader(fr);
        InputStreamReader ir = new InputStreamReader(url.openStream());
        BufferedReader br = new BufferedReader(ir);
        System.out.println(ir.getEncoding());
        System.out.println(br.readLine());
        System.out.println(br2.readLine());
        System.out.println(" donee ..");

        //InputStream is = new FileInputStream(uri)
        int res = is.read();
        //System.out.println(res);
        System.out.printf("%c %x %d\n",res,res,res);
        int res2 = is.read();
        System.out.printf("%c %x %d\n",res2,res2,res2);
        //System.out.println(res2);
        int res3 = is.read();
        System.out.printf("%c %x %d\n",res3,res3,res3);
        //System.out.println(res3);
        int res4 = is.read();
        System.out.printf("%c %x %d\n",res4,res4,res4);
        //System.out.println(res4);
        int res5 = is.read();
        System.out.println(res5);
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

    public String getOne() {
        return myStuff.remove(myStuff.size()-1);
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
        MyLoader ml = new MyLoader();
        ml.myLoading("Test");
        ServerSocket ss = new ServerSocket(port);
        Socket client = ss.accept();
        ClientHandler cl = new ClientHandler(client,ml);
        cl.greeting();
        cl.protocol();
        // pass this to clienthandler

    }
}

class ClientHandler {
    // socket, in and out channel
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    MyLoader ml;

    public ClientHandler(Socket client, MyLoader ml) {
        this.client = client;
        this.ml = ml;
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
        //pw.close();
        //br.close();
    }

    public void protocol() throws IOException {
        pw.println("Hvad kunne du godt tænke dig?");
        String input = br.readLine();
        while (!input.equals("Bye")) {
            switch (input) {
                case "GEO":String q = ml.getOne();pw.println(q);break;
                default:String def = ml.getOne();pw.println(def);
            }
            pw.println("Great. Now what?");
            input = br.readLine();
        }
    }
}
