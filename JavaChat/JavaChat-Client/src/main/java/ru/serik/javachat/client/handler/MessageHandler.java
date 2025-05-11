package ru.serik.javachat.client.handler;

@FunctionalInterface
public interface MessageHandler {
    public void message(String msg);
}
