import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class THImpl implements THInterface {

    private static Registry registry;
    private static ClientInterface client;
    private Map<String, Integer> availableSeats;
    private Map<String, List<SeatReservation>> guestList;

    private Map<String, SeatType> cancelNotificationList;

    private Map<String, Integer> userPortList;

    public THImpl() throws RemoteException {
        availableSeats = new HashMap<>();
        availableSeats.put("PA", 100);
        availableSeats.put("PB", 200);
        availableSeats.put("PC", 400);
        availableSeats.put("KE", 225);
        availableSeats.put("PT", 75);
        guestList = new HashMap<>();
        cancelNotificationList = new HashMap<>();
        userPortList = new HashMap<>();
    }

    @Override
    public synchronized void sendPort(String name, int port) {
        userPortList.put(name, port);
    }

    @Override
    public synchronized Map<String, List<SeatReservation>> getGuests() {
        return guestList;
    }

    @Override
    public synchronized Map<String, Integer> getAvailableSeats() {
        return availableSeats;
    }

    private Integer getUserPort(String name) {
        for (Map.Entry<String, Integer> entry : userPortList.entrySet()) {
            if (name.equals(entry.getKey()))
                return entry.getValue();
        }
        return null;
    }

    @Override
    public synchronized void notifyUsers(String type, int numberOfSeats) throws Exception {


        for (Map.Entry<String, SeatType> entry : cancelNotificationList.entrySet()) {
            String name = entry.getKey();
            Integer userPort = getUserPort(name);
            if (userPort == null) {
                throw new Exception("User with name " + name + "not found");
            }
            SeatType seatType = entry.getValue();
            if (SeatType.valueOf(type).equals(seatType)) {
                setRegistry(name, userPort);
                client.getNotification(type, numberOfSeats);
            }
        }
    }


    public static void setRegistry(String name, int port) throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry("localhost", port);
        client = (ClientInterface) registry.lookup(name);
    }

    @Override
    public synchronized int bookSeats(String type, int number, String name) {
        int available = availableSeats.getOrDefault(type, 0);
        if (available >= number) {
            List<SeatReservation> reservations = guestList.getOrDefault(name, new ArrayList<>());
            SeatReservation existingReservation = findReservation(reservations, type);
            if (existingReservation != null) {
                existingReservation.setQuantity(existingReservation.getQuantity() + number);
            } else {
                reservations.add(new SeatReservation(type, number));
            }
            guestList.put(name, reservations);
            availableSeats.put(type, available - number);

            System.out.println("Booking was successful.");
            System.out.println("Total cost: " + calculateCost(type, number) + " Euros.");
            System.out.println("AVAILABLE SEATS:\n");
            printAvailableSeats();
            System.out.println();
            printGuestList();

            return calculateCost(type, number);
        } else {
            return 0;
        }
    }

    @Override
    public void createNotifications(String name, String type) throws RemoteException {
        cancelNotificationList.put(name, SeatType.valueOf(type));
    }

    @Override
    public synchronized int cancelSeats(String type, int number, String name) throws Exception {
        int cost = 0;
        List<SeatReservation> reservations = guestList.getOrDefault(name, new ArrayList<>());
        SeatReservation existingReservation = findReservation(reservations, type);
        if (existingReservation != null && existingReservation.getQuantity() >= number) {
            existingReservation.setQuantity(existingReservation.getQuantity() - number);
            if (existingReservation.getQuantity() == 0) {
                reservations.remove(existingReservation);
            }
            guestList.put(name, reservations);
            int available = availableSeats.getOrDefault(type, 0);
            availableSeats.put(type, available + number);

            System.out.println("Cancellation was successful.");
            System.out.println("Refund amount: " + calculateCost(type, number) + " Euros.");
            System.out.println("AVAILABLE SEATS:\n");
            printAvailableSeats();
            System.out.println();
            printGuestList();
            notifyUsers(type,number);

            return calculateCost(type, number);
        } else {
            System.out.println("Cancellation failed. No booking found for " + name + " of type " + type);
            return 0;
        }
    }

    private SeatReservation findReservation(List<SeatReservation> reservations, String type) {
        for (SeatReservation reservation : reservations) {
            if (reservation.getType().equals(type)) {
                return reservation;
            }
        }
        return null;
    }


    private int calculateCost(String seatType, int number) {
        int cost = 0;
        switch (SeatType.valueOf(seatType)) {
            case PA:
                cost = (int) (SeatType.PA.getPrice() * number);
                break;
            case PB:
                cost = (int) (SeatType.PB.getPrice() * number);
                break;
            case PC:
                cost = (int) (SeatType.PC.getPrice() * number);
                break;
            case KE:
                cost = (int) (SeatType.KE.getPrice() * number);
                break;
            case PT:
                cost = (int) (SeatType.PT.getPrice() * number);
                break;
        }
        return cost;
    }

    private void printAvailableSeats() {
        System.out.println("Type\tAvailable Seats");
        for (Map.Entry<String, Integer> entry : availableSeats.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }

    private void printGuestList() {
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
}





