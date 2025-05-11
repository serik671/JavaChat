package ru.serik.javachat.server.handler;

@FunctionalInterface
public interface MessageHandler {
    public void writeMessage(String usename, String msg);
}
