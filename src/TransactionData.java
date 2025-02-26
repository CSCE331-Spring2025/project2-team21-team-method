import java.sql.Timestamp;

class TransactionData {
    int productId;
    int orderId;
    int customerId;
    String productName;
    Timestamp purchaseDate;  
    double iceAmount;
    String toppingType;

    public TransactionData(int productId, int orderId, int customerId, String productName, Timestamp purchaseDate, double iceAmount, String toppingType) {
        this.productId = productId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.productName = productName;
        this.purchaseDate = purchaseDate;
        this.iceAmount = iceAmount;
        this.toppingType = toppingType;
    }
}
