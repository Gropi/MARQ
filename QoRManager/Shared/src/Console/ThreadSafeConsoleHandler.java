package Console;

import Events.IConsoleInputListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ThreadSafeConsoleHandler extends Thread {
    private boolean _StopConsole;
    private final Scanner _ConsoleReader;
    private final List<IConsoleInputListener> listeners = new ArrayList<>();

    public ThreadSafeConsoleHandler() {
        _StopConsole = false;
        _ConsoleReader = new Scanner(System.in);
    }

    public void addListener(IConsoleInputListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IConsoleInputListener listener) {
        listeners.remove(listener);
    }

    public void stopConsoleHandler() {
        _StopConsole = true;
    }

    @Override
    public void run() {
        while (!_StopConsole) {
            var input = _ConsoleReader.nextLine();
            executeEvent(input);
        }
    }

    private void executeEvent(String message) {
        for (var listener : listeners) {
            listener.HandleConsoleInput(message);
        }
    }
}
