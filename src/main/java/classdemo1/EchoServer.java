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
        String line = "";
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
        int counter=0;
        int limit=3;
        MyLoader ml = new MyLoader();
        ml.myLoading("WTEST");
        ServerSocket ss = new ServerSocket(port);
        while(counter < limit) {
            counter++;
            Socket client = ss.accept();
            ClientHandler cl = new ClientHandler(client,ml);
            cl.start();
            //cl.greeting();
            //cl.protocol();
        }
        // pass this to clienthandler

    }
}

class ClientHandler extends Thread{
    // socket, in and out channel
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    MyLoader ml;
    String name;
    int playerID;
    int points;
    static int id=0;

    public ClientHandler(Socket client, MyLoader ml) {
        this.client = client;
        this.ml = ml;
        this.playerID = id++;
        this.points = 0;
        try {
            pw = new PrintWriter(client.getOutputStream(),true);
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            // skal håndteres
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.greeting();
            this.protocol();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void goodBye() throws IOException {
        pw.println("Goodbye My Friend. ");
        pw.close();
        br.close();
    }
    public void greeting() throws IOException {
        pw.println("Hello My Friend. What is your name?");
        this.name = br.readLine();
        //pw.println("Well Hello " + name);
        pw.printf("Well Hello %s", name);
        //pw.close();
        //br.close();
    }

    public void protocol() throws IOException {
        //200	WORLD CAPITALS	Beethoven's birthplace, it's now West Germany's capital	Bonn
        pw.println("Hvad kunne du godt tænke dig?");
        String input = br.readLine();
        while (!input.equals("Bye")) {
            switch (input) {
                //case "GEO":String q = ml.getOne();pw.println(q);break;
                case "GEO":handleGeo();break;
                default:String def = ml.getOne();pw.println(def);
            }
            pw.println("Great. Now what?");
            input = br.readLine();
        }
        goodBye();
    }

    private void handleGeo() {
        String q = ml.getOne();
        String[] qArr = q.split("\t");
        pw.println(qArr[2]);
        try {
            String ans = br.readLine();
            if (ans.equalsIgnoreCase(qArr[3])) {
                points +=Integer.parseInt(qArr[0]);
                pw.println("Well done " + points);
            } else {
                pw.println("To bad");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
