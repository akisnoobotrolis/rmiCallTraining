import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class THServer {


    public THServer() {
        try {
            THInterface th = new THImpl();
            THInterface stub = (THInterface) UnicastRemoteObject.exportObject(th, 0);

            Registry registry = LocateRegistry.createRegistry(2000);
            registry.rebind("THServer", stub);

            System.out.println("THServer is running...");
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }

    public static void main(String[] args) {
        new THServer();
    }
}
