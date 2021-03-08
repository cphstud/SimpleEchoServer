package classdemo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


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
        File f2 = new File("src/main/resources/output.bin");
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

        //OutputStream out = new DataOutputStream(new FileOutputStream(f2));
        int x = 75;
        DataOutputStream out = new DataOutputStream(new FileOutputStream(f2));
        out.writeInt(x);
        out.writeChar(x);
        out.close();


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

    public synchronized String getOne() {
        return myStuff.remove(myStuff.size()-1);
    }
}

public class EchoServer {
    BlockingQueue<String> messages = new ArrayBlockingQueue<>(250);
    BlockingQueue<String> users = new ArrayBlockingQueue<>(250);
    ConcurrentMap<String, PrintWriter> allNamedPrintwriters = new ConcurrentHashMap<>();
    //Dispatcher dispatcher  = new Dispatcher(messages, users);
    Dispatcher dispatcher2  = new Dispatcher(messages, allNamedPrintwriters);
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
        ConcurrentMap<String,Socket> clients = new ConcurrentHashMap<>() ;
        int counter=0;
        int limit=3;
        MyLoader ml = new MyLoader();
        ml.myLoading("WTEST");
        ServerSocket ss = new ServerSocket(port);
        //dispatcher.start();
        dispatcher2.start();
        while(true) {
            System.out.println("Waiting  ..." + counter);
            Socket client = ss.accept();
            // create info
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
            String loginLine = br.readLine();
            if (loginLine == null) {
                client.close();
            } else {

            String token = loginLine.split("#")[0];
            String content = loginLine.split("#")[1];
            if (token.equals("CONNECT")) {
                //dispatcher.addClientWriter(pw);
                //clients.put(content,client);
                messages.add(loginLine);
                dispatcher2.addSocketToList(content,client);
                dispatcher2.addClientToNamedWriter(content, pw);
            }
            //ClientHandler cl = new ClientHandler(br, pw, messages);
            ClientHandler cl = new ClientHandler(content, br, pw, ml, messages);
            cl.start();
            }
        }
        //ClientHandler cl = new ClientHandler(client,ml,dispatcher);

        //cl.greeting();
        //cl.protocol();
        // pass this to clienthandler
    }
}

class Dispatcher extends Thread {
    // den skal lytte til  køen .. altså en tråd
    // datastruktur hvor den lytter på en kø til beskeder
    // datastruktur hvor den kan finde alle klienter (og tilføje og slette)
    //List<Socket> allClients;
    //List<PrintWriter> allWriteToClientLine;
    //ConcurrentMap<String,PrintWriter> allNamePrintWriters;
    ConcurrentMap<String,PrintWriter> allNamePrintWriters;
    ConcurrentMap<String,Socket> allNamedSockets;
    BlockingQueue<PrintWriter> allWriteToClientLine;
    BlockingQueue<String> allMessages;
    //BlockingQueue<Message> allMessages;
    BlockingQueue<String> allUsers;

    public Dispatcher(BlockingQueue<String> messages, ConcurrentMap<String,PrintWriter> allNamePrintWriters) {
        this.allNamePrintWriters = allNamePrintWriters;
        this.allNamedSockets = new ConcurrentHashMap<>();
        allMessages = messages;
    }

    public Dispatcher(BlockingQueue<String> msgQueue) {
        allWriteToClientLine = new ArrayBlockingQueue<PrintWriter>(200);
        allMessages = msgQueue;
    }

    public Dispatcher(BlockingQueue<String> messages, BlockingQueue<String> users) {
        allWriteToClientLine = new ArrayBlockingQueue<PrintWriter>(200);
        allMessages = messages;
        allUsers = users;
    }

    public void addSocketToList(String name, Socket socket) {
        allNamedSockets.put(name,socket);
    }

    public void removeSocketFromList(String name) {
        allNamedSockets.remove(name);
    }

    public void addClientToNamedWriter(String name,PrintWriter pw) {
        allNamePrintWriters.put(name,pw);
    }
    public void removeClientFrommNamedWriter(String name) {
        allNamePrintWriters.remove(name);
    }

    public void addClientWriter(PrintWriter pw) {
        allWriteToClientLine.add(pw);
    }

    public void addUser(String user) {
        allUsers.add(user);
    }

    public void removeUser(String user) {
        allUsers.remove(user);
    }

    @Override
    public void run() {
        while (true) {
            // take element and send it to all clients
            try {
                String head = allMessages.take();
                //sendMessageToAll(head);
                sendMessage(head);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void sendMessage(String head) {
        //SEND#Per,Kurt#Hej Kurtbasse -> MESSAGE#Per#Hej Kurtbasse (til Kurts pw)
        StringBuilder sb = new StringBuilder();
        String[] content = head.split("#");
        if (content[0].equals("CONNECT")) {
            sb.append("ONLINE#");
            for (Map.Entry<String, PrintWriter> entry : allNamePrintWriters.entrySet()) {
                sb.append(entry.getKey() + ",");
            }
            for (Map.Entry<String, PrintWriter> entry : allNamePrintWriters.entrySet()) {
                entry.getValue().println(sb.toString());
            }
        } else if (content[0].equals("CLOSE")) {
            allNamePrintWriters.remove(content[1]);
            try {
                allNamedSockets.get(content[1]).close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            sb.append("MESSAGE#");
        }
        if (content[1].contains("*")) {
            sb.append(head);
            // send to all
            for (PrintWriter pw : allNamePrintWriters.values()) {
                pw.println(head);
            }
        } else {
            String[] recipients = content[1].split(",");
            // find printwriters
            sb.append(recipients[0]);
            sb.append("#");
            sb.append(content);
            for (int i = 1; i < recipients.length; i++) {
                if (allNamePrintWriters.containsKey(recipients[i])) {
                    allNamePrintWriters.get(recipients[i]).println(sb.toString());
                }
            }
        }
    }

    private void sendMessageToAll(String msg) {
        for (PrintWriter pw : allWriteToClientLine) {
            pw.println(msg);
        }
    }
}

class ClientHandler extends Thread{
    // socket, in and out channel
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    MyLoader ml;
    BlockingQueue<String> allMessages;
    BlockingQueue<String> users;
    ConcurrentMap<Integer,String> allMessagesMap;
    ConcurrentMap<String,Integer> allUsersMap;
    //Dispatcher dispatcher;

    String name;
    String[] inputArr;
    int playerID;
    int points;
    static int id=0;

    public ClientHandler(BufferedReader br, PrintWriter pw, ConcurrentMap<Integer,String> allMessages, ConcurrentMap<String,Integer> allUsers) {
        this.ml = ml;
        this.playerID = id++;
        this.points = 0;
        this.br = br;
        this.pw = pw;
        this.allMessagesMap = allMessages;
        this.allUsersMap = allUsers;
    }

    public ClientHandler(Socket client, MyLoader ml,  Dispatcher dispatcher) {
        this.playerID = id++;
        this.points = 0;
        this.br = br;
        this.pw = pw;
        //this.allMessages = allMessages;
    }
    public ClientHandler(Socket client, MyLoader ml,  BlockingQueue<String> allMessages) {
        //this.allMessages = allMessages;
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

    public ClientHandler(String name, BufferedReader br, PrintWriter pw, MyLoader ml,  BlockingQueue<String> allMessages) {
        this.ml = ml;
        this.playerID = id++;
        this.points = 0;
        this.br = br;
        this.pw = pw;
        this.allMessages = allMessages;
    }

    public ClientHandler(BufferedReader br, PrintWriter pw, BlockingQueue<String> allMessages, BlockingQueue<String> users) {
        this.playerID = id++;
        this.points = 0;
        this.br = br;
        this.pw = pw;
        this.allMessages = allMessages;
    }

    @Override
    public void run() {
        try {
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
        pw.println("Velkommenn kunne du godt tænke dig?");
        String input = "";
        input = br.readLine();
        if (input == null) {
            input = "CLOSE";
        }
        inputArr = input.split("#");
        String token = inputArr[0];
        String[] subsetArr = Arrays.copyOfRange(inputArr, 1, inputArr.length);
        String content = String.join("#",subsetArr);
        // get recipients

        while (!token.equals("Bye")) {
            switch (token) {
                //case "GEO":String q = ml.getOne();pw.println(q);break;
                // case "TOSOME": TOSOME#Kurt,Verner#Her bor Otto
                case "GEO":handleGeo();break;
                case "SEND":handleMsgToSome(content, token);break;
                case "CLOSE":handleMsgToSome(content, token);break;
                default:handleMsgToSome("0","CLOSE");
            }
            pw.println("Great. Now what?");
            input = br.readLine();
        }
        goodBye();
    }

    private void handleMsgToSome(String content, String token) {
        //SEND#Kurt#Hej Kurtbasse
        //SEND#Per,Kurt#Hej Kurtbasse -> MESSAGE#Per#Hej Kurtbasse (til Kurts pw)
        //SEND#Per,Kurt,Verner#Hej venner -> MESSAGE#Per#Hej Venner (til Kurts pw)
        //                                -> MESSAGE#Per#Hej Venner (til Verners pw)
        allMessages.add(token+"#"+name+content);
    }

    private void handleMsgToAll() throws IOException { pw.println("Hvad vil du sende ud?");
        String msg = br.readLine();
        // TODO: Hvordan sender jeg det til alle forbundne klienter?
        //dispatcher.sendMessageToAll(msg);
        allMessages.add(msg);
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
