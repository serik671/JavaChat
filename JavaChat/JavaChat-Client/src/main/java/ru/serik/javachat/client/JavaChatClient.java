package ru.serik.javachat.client;

import ru.serik.javachat.client.handler.DisconnectNotification;
import ru.serik.javachat.client.handler.MessageHandler;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaChatClient {
    private Socket socket;
    private PrintWriter writer;
    private Scanner scanner;
    private MessageHandler msgHandler;
    private boolean start;
    private DisconnectNotification disconnectNotification;
    
    public JavaChatClient(){
        msgHandler = (msg)->{
            System.out.printf("[%s] %s\n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), msg);
        };
        disconnectNotification = ()->{
            System.out.println("The server has terminated the connection.");
        };
    }
        
    public void onMessageAccept(MessageHandler msgHandler){
        this.msgHandler = msgHandler;
    }
    public void onServerDisconnect(DisconnectNotification dn){
        this.disconnectNotification = dn;
    }
    
    public void connect(String host, int port) throws Exception{
        connect(host, port, "anonymous");
    }
    public void connect(String host, int port, String username) throws Exception{
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("Connect:"+username);
        scanner = new Scanner(socket.getInputStream());
        start = true;
        listenMessage();
    }
    public void disconnect() throws Exception{
        start = false;
        socket.close();
        writer.close();
        scanner.close();
    }
    public void sendMessage(String msg) throws Exception{
        writer.println(msg);
    }
    
    private void listenMessage(){
        ExecutorService execService = Executors.newCachedThreadPool();
        execService.execute(()->{
            while(start){
                if(scanner.hasNextLine()){
                    String msg = scanner.nextLine();
                    msgHandler.message(msg);
                }else{
                    disconnectNotification.notifyDisconnect();
                    break;
                }
            }
        });        
    }
    
}
