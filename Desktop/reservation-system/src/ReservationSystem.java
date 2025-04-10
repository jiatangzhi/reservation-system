import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Class to manage reservations for a restaurant system
public class ReservationSystem {
    private final int totalTables; // Total number of tables in the restaurant
    private final Map<LocalDateTime, List<String>> reservations; // Stores reservations per hour

    // Initializes the system with the given number of tables
    public ReservationSystem(int totalTables) {
        this.totalTables = totalTables;
        this.reservations = new HashMap<>();
    }

    // Makes a reservation for a customer at a specific hour
    // Returns true if reservation is successful, false if fully booked
    public boolean makeReservation(String customerName, LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        reservations.putIfAbsent(dateTime, new ArrayList<>());
        List<String> reserved = reservations.get(dateTime);

        if (reserved.size() < totalTables) {
            reserved.add(customerName);
            return true;
        }
        return false;
    }

    // Checks if a customer has a reservation at a given time
    public boolean checkReservation(String customerName, LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        return reservations.containsKey(dateTime) &&
               reservations.get(dateTime).contains(customerName);
    }

    // Cancels a customer's reservation for a given hour
    public boolean cancelReservation(String customerName, LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        if (reservations.containsKey(dateTime)) {
            return reservations.get(dateTime).remove(customerName);
        }
        return false;
    }

    // Returns the number of free tables at a specific hour
    public int getFreeTables(LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        return totalTables - reservations.getOrDefault(dateTime, new ArrayList<>()).size();
    }

    // Returns all reservation counts per hour, useful for statistics
    public Map<LocalDateTime, Integer> getReservationCounts() {
        return reservations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

    // Normalizes any given time to the top of the hour
    private LocalDateTime normalizeHour(LocalDateTime dateTime) {
        return dateTime.withMinute(0).withSecond(0).withNano(0);
    }
}