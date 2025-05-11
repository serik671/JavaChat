package ru.serik.javachat.server;

import ru.serik.javachat.server.handler.LogHandler;
import ru.serik.javachat.server.handler.MessageHandler;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection implements Runnable{
    private Socket clientSocket;
    private MessageHandler msgHandler;
    private LogHandler logHandler;
    private String username;
    private boolean start;
    private PrintWriter writer;
    private Scanner scan;

    public ClientConnection(Socket clientSocket, MessageHandler msgHandler, LogHandler logHandler) throws Exception {
        this.clientSocket = clientSocket;
        this.msgHandler = msgHandler;
        this.logHandler = logHandler;
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        scan = new Scanner(clientSocket.getInputStream());
        start = true;
    }

    @Override
    public void run() {
        while(start){                
            if(scan.hasNextLine()){
                String msg = scan.nextLine();
                if(username == null && msg.matches("Connect:[A-я0-9]+")){
                    username = msg.split(":")[1];
                    logHandler.writeLog(String.format("User [%s] has been connected", username));
                }else{
                    msgHandler.writeMessage(username, msg);
                }
            }else{
                logHandler.writeLog(String.format("User [%s] has been disconnected", username));
                break;
            }
        }
        logHandler.writeLog(username + " is stopped");
        closeSession();
    }
    
    public void closeSession(){
        start = false;
        try{
            writer.close();
            scan.close();
            clientSocket.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    public void sendMessage(String msg){
        try{
            writer.println(msg);
        }catch(Exception e){
            logHandler.writeLog("Send Message Error: "+e.getMessage());
        }
    }

    public boolean isStart() {
        return start;
    }

    public String getUsername() {
        return username;
    }
    
}
