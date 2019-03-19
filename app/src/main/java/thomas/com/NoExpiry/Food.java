package thomas.com.NoExpiry;

public class Food {

    private String title;
    private String description;
    private String expiryDate;
    private int exDate;

    private Food() {
        // Fire base needs this empty constructor
    }

    Food(String title, String description, String expiryDate, int exDate) {
        this.title = title;
        this.description = description;
        this.expiryDate = expiryDate;
        this.exDate = exDate;
    }

    // these has to be get for fire base to recognize it
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public int getExDate() { return exDate; }
}
