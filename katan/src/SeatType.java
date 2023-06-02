public enum SeatType {
    PA("PA", 45.0),
    PB("PB", 35.0),
    PC("PC", 25.0),
    KE("KE", 30.0),
    PT("PT", 20.0);

    private final String code;
    private final double price;

    SeatType(String code, double price) {
        this.code = code;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public double getPrice() {
        return price;
    }
}
