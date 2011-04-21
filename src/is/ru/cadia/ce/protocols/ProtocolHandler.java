package is.ru.cadia.ce.protocols;

public interface ProtocolHandler {

    public void handle(String message);
    public int getTimeForThisMove(int sideToMove);
    public void sendMessage(String message);
    
}
