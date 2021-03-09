package classdemo1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;


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
        ExecutorService executorService = Executors.newFixedThreadPool(5);

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
                    messages.add(loginLine);
                    dispatcher2.addSocketToList(content,client);
                    dispatcher2.addClientToNamedWriter(content, pw);
                }
                ClientHandler cl = new ClientHandler(content, br, pw, ml, messages);
                System.out.println("Add to executor ..");
                executorService.execute(cl);
            }
        }
    }
}

class Dispatcher extends Thread {
    ConcurrentMap<String,PrintWriter> allNamePrintWriters;
    ConcurrentMap<String,Socket> allNamedSockets;
    BlockingQueue<String> allMessages;
    BlockingQueue<String> allUsers;

    public Dispatcher(BlockingQueue<String> messages, ConcurrentMap<String,PrintWriter> allNamePrintWriters) {
        this.allNamePrintWriters = allNamePrintWriters;
        this.allNamedSockets = new ConcurrentHashMap<>();
        allMessages = messages;
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
                sendMessage(head);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void sendMessage(String head) {
        //SEND#Per,Kurt#Hej Kurtbasse -> MESSAGE#Per#Hej Kurtbasse (til Kurts pw)
        System.out.println(Thread.currentThread().getName() + " is in " + head);
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
                allNamedSockets.remove(content[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (content[1].contains("*")) {
            sb.append("MESSAGE#");
            sb.append(head);
            // send to all
            for (PrintWriter pw : allNamePrintWriters.values()) {
                pw.println(head);
            }
        } else {
            sb.append("MESSAGE#");
            String[] recipients = content[1].split(",");
            // find printwriters
            sb.append(recipients[0]);
            sb.append("#");
            sb.append(content[2]);
            for (int i = 1; i < recipients.length; i++) {
                if (allNamePrintWriters.containsKey(recipients[i])) {
                    allNamePrintWriters.get(recipients[i]).println(sb.toString());
                }
            }
        }
    }
}

class ClientHandler implements Runnable{
    Socket client;
    BufferedReader br;
    PrintWriter pw;
    MyLoader ml;
    BlockingQueue<String> allMessages;

    String name;
    String[] inputArr;
    int playerID;
    int points;
    static int id=0;

    public ClientHandler(String name, BufferedReader br, PrintWriter pw, MyLoader ml,  BlockingQueue<String> allMessages) {
        this.ml = ml;
        this.playerID = id++;
        this.points = 0;
        this.br = br;
        this.pw = pw;
        this.allMessages = allMessages;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            this.protocol();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void protocol() throws IOException {
        boolean go = true;
        String input = "";
        while (go) {
            input = br.readLine();
            if (input == null) {
                input = "CLOSE#1";
            }
            inputArr = input.split("#");
            String token = inputArr[0];
            String[] subsetArr = Arrays.copyOfRange(inputArr, 1, inputArr.length);
            String content = String.join("#",subsetArr);
            // get recipients

            switch (token) {
                case "SEND":handleMsgToSome(content, token);break;
                case "CLOSE":handleMsgToSome(content, token);go=false;break;
                default:handleMsgToSome("0","CLOSE");go=false;
            }
        }
    }
    private void handleMsgToSome(String content, String token) {
        String comma = "";
        if (content.length()>1) comma=",";
        String output = String.format("%s#%s%s%s%n",token,name,comma,content);
        allMessages.add(output);
    }
}
