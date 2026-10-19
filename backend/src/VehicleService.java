
import java.util.ArrayList;
import java.util.List;

public class VehicleService {

    private List<Vehicle> vehicles = new ArrayList<>();

    public VehicleService() {
        vehicles.add(new Vehicle(
            "A101", "Ambulance", "Borivali",
            4.5, 2, "Available"
        ));

        vehicles.add(new Vehicle(
            "A102", "Ambulance", "Kandivali",
            2.1, 1, "Available"
        ));

        vehicles.add(new Vehicle(
            "F101", "Fire Truck", "Dahisar",
            3.2, 1, "Available"
        ));

        vehicles.add(new Vehicle(
            "P101", "Police", "Borivali East",
            1.8, 2, "Busy"
        ));

        vehicles.add(new Vehicle(
            "A103", "Ambulance", "Malad",
            6.8, 3, "Available"
        ));
    }

    public List<Vehicle> getAllVehicles() {
        return vehicles;
    }

    public Vehicle findNearestVehicle(String emergencyType) {
        Vehicle nearest = null;

        for (Vehicle vehicle : vehicles) {

            if (vehicle.getType().equalsIgnoreCase(emergencyType)
                    && vehicle.getStatus().equalsIgnoreCase("Available")) {

                if (nearest == null
                        || vehicle.getDistance() < nearest.getDistance()) {
                    nearest = vehicle;
                }
            }
        }

        return nearest;
    }
    
    public Vehicle linearSearchById(String targetId) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId().equalsIgnoreCase(targetId)) {
                return vehicle;
            }
        }

        return null;
    }

    
    public Vehicle binarySearchById(String targetId) {
        // Make a copy so the original vehicle list remains unchanged
        List<Vehicle> sortedVehicles = new ArrayList<>(vehicles);

        // Sort vehicles by ID
        sortedVehicles.sort(
            (a, b) -> a.getId().compareToIgnoreCase(b.getId())
        );

        int low = 0;
        int high = sortedVehicles.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;

            int comparison = sortedVehicles.get(mid)
                .getId()
                .compareToIgnoreCase(targetId);

            if (comparison == 0) {
                return sortedVehicles.get(mid);
            } else if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null;
    }

        
    public List<Vehicle> sortVehiclesByDistance() {
        List<Vehicle> sortedVehicles = new ArrayList<>(vehicles);
        return mergeSort(sortedVehicles);
    }

    private List<Vehicle> mergeSort(List<Vehicle> list) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;

        List<Vehicle> left = new ArrayList<>(
            list.subList(0, mid)
        );
        List<Vehicle> right = new ArrayList<>(
            list.subList(mid, list.size())
        );

        left = mergeSort(left);
        right = mergeSort(right);

        return merge(left, right);
    }

    private List<Vehicle> merge(
            List<Vehicle> left,
            List<Vehicle> right) {

        List<Vehicle> result = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getDistance()
                    <= right.get(j).getDistance()) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }

        return result;
    }


}
