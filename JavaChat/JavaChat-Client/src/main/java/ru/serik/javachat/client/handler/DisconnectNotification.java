package ru.serik.javachat.client.handler;

@FunctionalInterface
public interface DisconnectNotification {
    public void notifyDisconnect();
}
