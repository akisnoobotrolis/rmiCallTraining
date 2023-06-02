
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class THClient {
    private static Registry registry;
    private static int port;
    private static THInterface server;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Available commands are:\n" +
                    "list <hostname>\n" +
                    "book <hostname> <type> <number> <name>\n" +
                    "guests <hostname>\n" +
                    "cancel <hostname> <type> <number> <name>\n");
        } else {
            String command = args[0];
            String hostname = args.length > 1 ? args[1] : "";

            switch (command) {
                case "list":
                    availableSeats(hostname);
                    break;
                case "book":
                    if (args.length >= 5) {
                        String type = args[2];
                        int number = Integer.parseInt(args[3]);
                        String name = args[4];
                        bookPositions(hostname, type, number, name);
                    } else {
                        System.out.println("Please specify the type, number, and name correctly.\n");
                    }
                    break;
                case "guests":
                    showGuests(hostname);
                    break;
                case "cancel":
                    if (args.length >= 5) {
                        String type = args[2];
                        int number = Integer.parseInt(args[3]);
                        String name = args[4];
                        cancelBooking(hostname, type, number, name);
                    } else {
                        System.out.println("Please specify the type, number, and name correctly for cancellation.\n");
                    }
                    break;
                default:
                    System.out.println("Command not found.");
                    System.out.println("Available commands are:\n" +
                            "list <hostname>\n" +
                            "book <hostname> <type> <number> <name>\n" +
                            "guests <hostname>\n" +
                            "cancel <hostname> <type> <number> <name>\n");
                    break;
            }
        }
    }

    private static void availableSeats(String hostname) throws RemoteException, NotBoundException {
        setRegistry(hostname);
        Map<String, Integer> availableSeats = server.getAvailableSeats();

        System.out.println("AVAILABLE SEATS:\n");
        for (Map.Entry<String, Integer> seat : availableSeats.entrySet()) {
            String type = seat.getKey();
            int number = seat.getValue();
            System.out.println(number + " available seats of type " + type);
        }
        System.out.println();
    }

    private static void bookPositions(String hostname, String type, int number, String name) throws NotBoundException, RemoteException {
        setRegistry(hostname);
        String input;
        int cost = server.bookSeats(type, number, name);

        if (cost > 0) {
            System.out.println("Booking was successful.");
            System.out.println("Total cost: " + cost + " Euros.\n");
        } else {
            System.out.println("Booking was not successful.\n");

            Map<String, Integer> availableSeats = server.getAvailableSeats();
            int value = availableSeats.getOrDefault(type, 0);

            if (value > 0) {
                System.out.println("Number of available seats of type " + type + ": " + value + "\nDo you want to book them? Press Yes or No");
                input = yesNoInput();

                if (input.equals("yes")) {
                    cost = server.bookSeats(type, value, name);
                    if (cost > 0) {
                        System.out.println("Booking was successful.");
                        System.out.println("Total cost: " + cost + " Euros.\n");
                    } else {
                        System.out.println("Booking was not successful.\n");
                    }
                } else if (input.equals("no")) {
                    System.out.println("Sorry for the inconvenience");
                    notificationMessage(name, type);
                }
            } else {
                notificationMessage(name, type);
            }
        }
    }

    private static void notificationMessage(String name, String type) throws RemoteException {
        System.out.println("No available seats of type " + type + "\n");
        System.out.println("Do you want to receive notifications if there are any seats of type " + type + " available?");
        String input = yesNoInput();
        if (input.equals("yes")) {
            createNotifications(name, type );
        } else if (input.equals("no")) {
            System.out.println("Sorry for the inconvenience");
        }
    }

    private static void showGuests(String hostname) throws NotBoundException, RemoteException {
        setRegistry(hostname);
        Map<String, List<SeatReservation>> guestList  = server.getGuests();
        printGuestList(guestList);
    }

    private static void printGuestList(Map<String, List<SeatReservation>> guestList) {
        System.out.println("GUEST LIST:\n");
        for (Map.Entry<String, List<SeatReservation>> entry : guestList.entrySet()) {
            String name = entry.getKey();
            List<SeatReservation> reservations = entry.getValue();

            System.out.println("Booking for " + name + ":");
            for (SeatReservation reservation : reservations) {
                String type = reservation.getType();
                int quantity = reservation.getQuantity();
                System.out.println("Type: " + type + ", Seats: " + quantity);
            }
            System.out.println();
        }
    }

    private static void cancelBooking(String hostname, String type, int number, String name) throws Exception {
        setRegistry(hostname);
        int refund = server.cancelSeats(type, number, name);

        if (refund > 0) {
            System.out.println("Cancellation was successful. Your refund is: " + refund + " Euros.\n");
        } else {
            System.out.println("Cancellation was unsuccessful.\n");
        }
    }

    public static void setRegistry(String hostname) throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry(hostname, 2000);
        server = (THInterface) registry.lookup("THServer");
    }

    public static void createNotifications(String name, String type) throws RemoteException {
        createClient(name);
        server.sendPort(name,port);
        server.createNotifications(name, type);
        System.out.println("Notifications activated for " + name + " for seats of type " + type + "\n");
    }

    public static String yesNoInput() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().toLowerCase();
        while (!input.equals("yes") && !input.equals("no")) {
            System.out.println("Please enter either Yes or No");
            input = scanner.nextLine().toLowerCase();
        }
        return input;
    }

    private static void createClient(String name) {
        try {
            ClientInterface cl = new ClientInterfaceImpl();
            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(cl,0);
            ServerSocket s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
            Registry myRegistry = LocateRegistry.createRegistry(port);
            myRegistry.rebind(name, stub);



            System.out.println("Waiting for notifications...");
        } catch (Exception e) {
            System.out.println("Trouble: " + e);
        }
    }
}
