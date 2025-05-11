package ru.serik.javachat.server;

import ru.serik.javachat.server.handler.LogHandler;
import ru.serik.javachat.server.handler.MessageHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaChatServer {

    private String port;
    private String address;
    private ServerSocket socket;
    private boolean start;
    private MessageHandler msgHandler;
    private LogHandler logHandler;
    
    private List<ClientConnection> clients;

    public JavaChatServer(){
        clients = new ArrayList<>();
        msgHandler = (username, msg)->{
            sendOutMessage(username, msg);
        };
        logHandler = (log)->{
            System.out.printf("%s: %s", LocalDateTime.now(), log);
        };
    }
    
    private void sendOutMessage(String username, String msg){
        clients.forEach(client->{
            client.sendMessage(username+": "+msg);
        });        
    }
    
    private void startClientCheckerService() throws Exception{
        new Thread(()->{
            while(start){
                Iterator<ClientConnection> clientConnection = clients.listIterator();
                while(clientConnection.hasNext()){
                    if(!clientConnection.next().isStart()){
                        clientConnection.remove();
                    }
                }
                for(int i=0; i<clients.size(); i++){
                    String username = clients.get(i).getUsername();
                    for(int j=i+1; j<clients.size(); j++){
                        if(username.equals(clients.get(j).getUsername())){
                            clients.get(j).sendMessage(String.format("Имя пользователя [%s] занято. Пожалуйста, подключитесь под другим именем пользователя.", username));
                            clients.get(j).closeSession();
                            clients.remove(j);
                        }
                    }
                }
                try{
                    Thread.sleep(Duration.ofSeconds(1));
                }catch(Exception e){}
            }
        }).start();
    }
    
    public void onMessageAccepted(MessageHandler messageHandler){
        this.msgHandler = (usr, msg)->{
            messageHandler.writeMessage(usr, msg);
            sendOutMessage(usr, msg);
        };
    }
    public void onLogAccepted(LogHandler logHandler){
        this.logHandler = logHandler;
    }
    
    public void start() throws Exception{
        socket = new ServerSocket(0);
        address = String.valueOf(socket.getInetAddress().getHostAddress());
        port = String.valueOf(socket.getLocalPort());
        start = true;
        startClientCheckerService();
        new Thread(()->{
            ExecutorService execService = Executors.newCachedThreadPool();
            while(start){
                try{
                    Socket clientSocket = socket.accept();
                    logHandler.writeLog(String.format("Connected from %s:%d",
                            clientSocket.getInetAddress().getHostAddress(),
                            clientSocket.getPort()));
                    ClientConnection client = new ClientConnection(clientSocket,
                            msgHandler, logHandler);
                    clients.add(client);
                    client.sendMessage("--------Добро пожаловать на сервер--------");
                    execService.execute(client);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }
    
    public void stop() throws Exception{
        start = false;
        clients.forEach(ClientConnection::closeSession);
        clients.clear();
        socket.close();        
    }

    public String getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getUsers() {
        return clients.stream().map(ClientConnection::getUsername).toList();
    }
    public void kickUser(String username){
        ListIterator<ClientConnection> listIterator = clients.listIterator();
        while(listIterator.hasNext()){
            ClientConnection client = listIterator.next();
            if(client.getUsername().equals(username)){
                listIterator.remove();
                client.sendMessage("Вы были исключены с сервера");
                client.closeSession();
            }
        }
    }

}
