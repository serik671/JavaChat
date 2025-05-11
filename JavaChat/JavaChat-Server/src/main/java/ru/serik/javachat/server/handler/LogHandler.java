package ru.serik.javachat.server.handler;

@FunctionalInterface
public interface LogHandler {
    public void writeLog(String message);
}
