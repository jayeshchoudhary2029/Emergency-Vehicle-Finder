
public class Vehicle {
    private String id;
    private String type;
    private String location;
    private double distance;
    private int priority;
    private String status;

    public Vehicle(String id, String type, String location,
                   double distance, int priority, String status) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.distance = distance;
        this.priority = priority;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public double getDistance() {
        return distance;
    }

    public int getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }
}
