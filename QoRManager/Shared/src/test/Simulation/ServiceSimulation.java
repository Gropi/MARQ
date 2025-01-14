package Simulation;

import Events.IMessageReceivedListener;
import Network.Connection.ICommunication;

import java.io.IOException;
import java.util.ArrayList;

public class ServiceSimulation implements ICommunication {
    ArrayList<IMessageReceivedListener> _listeners = new ArrayList<>();

    @Override
    public void addMessageReceivedListener(IMessageReceivedListener listener) {
        if(!_listeners.contains(listener))
            _listeners.add(listener);
    }

    @Override
    public void removeMessageReceivedListener(IMessageReceivedListener listener) {
        _listeners.remove(listener);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        return true;
    }

    @Override
    public boolean close() throws IOException {
        return true;
    }

    public boolean sendMessage(String message) throws IOException, NullPointerException {


        var request = message.toLowerCase().split(";");

        if(request[0].equalsIgnoreCase("INPUT")){

        } else if(request[0].equalsIgnoreCase("SUBSCRIBE")){
        } else if(request[0].equalsIgnoreCase("UNSUBSCRIBE")){
        } else if(request[0].equalsIgnoreCase("EXIT")){}


        return false;
    }

    @Override
    public void run() {

    }
}