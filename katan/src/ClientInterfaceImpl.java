public class ClientInterfaceImpl implements ClientInterface{

    @Override
    public void getNotification(String type, int numberOfSeats) {

        System.out.println( numberOfSeats + " Seats of type "+ type + " are available");

    }
}
