import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public interface THInterface extends Remote {

    Map<String, List<SeatReservation>> getGuests() throws RemoteException;

    Map<String, Integer> getAvailableSeats() throws RemoteException;




    void notifyUsers(String type, int numberOfSeats) throws Exception;

    int bookSeats(String section, int numSeats, String customerName) throws RemoteException;



    void createNotifications(String name,String type) throws RemoteException;

    int cancelSeats(String section, int numSeats, String customerName) throws Exception;


    void sendPort(String name, int port) throws RemoteException;


}