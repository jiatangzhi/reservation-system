import java.time.LocalDateTime;
import java.util.*;

public class ReservationSystem {
    private final int totalTables;
    private final Map<LocalDateTime, List<String>> reservations;

    public ReservationSystem(int totalTables) {
        this.totalTables = totalTables;
        this.reservations = new HashMap<>();
    }

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

    public boolean checkReservation(String customerName, LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        return reservations.containsKey(dateTime) &&
               reservations.get(dateTime).contains(customerName);
    }

    public boolean cancelReservation(String customerName, LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        if (reservations.containsKey(dateTime)) {
            return reservations.get(dateTime).remove(customerName);
        }
        return false;
    }

    public int getFreeTables(LocalDateTime dateTime) {
        dateTime = normalizeHour(dateTime);
        return totalTables - reservations.getOrDefault(dateTime, new ArrayList<>()).size();
    }

    private LocalDateTime normalizeHour(LocalDateTime dateTime) {
        return dateTime.withMinute(0).withSecond(0).withNano(0);
    }
} 
