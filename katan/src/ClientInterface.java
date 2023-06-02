import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    public void getNotification(String type, int numberOfSeats) throws RemoteException;

}
