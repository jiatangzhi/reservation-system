import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationDashboard {
    // Core reservation system handling logic for up to 10 tables
    private final ReservationSystem reservationSystem = new ReservationSystem(10);

    // Status display area for logging actions and results
    private final JTextArea statusArea = new JTextArea();

    public void launchUI() {
        // Main application window
        JFrame frame = new JFrame("Restaurant Reservation System");
        frame.setSize(950, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // === LEFT PANEL: Displays hourly availability ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Availability per Hour"));

        JTextArea availabilityArea = new JTextArea();
        availabilityArea.setEditable(false);
        availabilityArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(availabilityArea);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Button to refresh and display current availability
        JButton refreshButton = new JButton("Refresh Availability");
        refreshButton.addActionListener(e -> availabilityArea.setText(getAvailabilityText()));
        leftPanel.add(refreshButton, BorderLayout.SOUTH);

        // === RIGHT PANEL: Controls for interacting with the system ===
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Reservation Controls"));

        // Input for customer name
        JTextField nameField = new JTextField(15);
        rightPanel.add(new JLabel("Customer Name:"));
        rightPanel.add(nameField);

        // Dropdown for selecting an available hour
        JComboBox<String> hourBox = new JComboBox<>();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<LocalDateTime> hours = new ArrayList<>();
        LocalDateTime end = now.withHour(22); // Reservations only up to 10 PM
        if (now.getHour() > 22) {
            // If it's already past 10 PM, shift to next day's 9 AM
            now = now.plusDays(1).withHour(9);
            end = now.withHour(22);
        }
        while (!now.isAfter(end)) {
            hours.add(now);
            hourBox.addItem(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            now = now.plusHours(1);
        }
        rightPanel.add(new JLabel("Select Hour:"));
        rightPanel.add(hourBox);

        // Reservation-related buttons
        JButton reserveBtn = new JButton("Make Reservation");
        JButton checkBtn = new JButton("Check Reservation");
        JButton cancelBtn = new JButton("Cancel Reservation");
        JButton statsBtn = new JButton("Show Stats");

        // Add vertical spacing and buttons to panel
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(reserveBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(checkBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(cancelBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(statsBtn);
        rightPanel.add(Box.createVerticalStrut(15));

        // Text area to display user interaction logs
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(300, 160));
        rightPanel.add(new JLabel("Status Log:"));
        rightPanel.add(statusScroll);

        // === BUTTON LOGIC ===

        // Reserve a table if available
        reserveBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            LocalDateTime selectedHour = hours.get(selectedIndex);

            if (name.isEmpty()) {
                statusArea.append("Please enter a name.\n");
                return;
            }
            if (selectedHour.isBefore(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0))) {
                statusArea.append("Cannot reserve for past time.\n");
                return;
            }

            boolean reserved = reservationSystem.makeReservation(name, selectedHour);
            statusArea.append("[" + name + "] " +
                    (reserved ? "Reservation successful at " + selectedHour + "\n" : "No tables available.\n"));
        });

        // Check if reservation exists
        checkBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            LocalDateTime selectedHour = hours.get(selectedIndex);

            boolean exists = reservationSystem.checkReservation(name, selectedHour);
            statusArea.append("[" + name + "] " +
                    (exists ? "Reservation exists at " + selectedHour + "\n" : "No reservation found.\n"));
        });

        // Cancel a reservation if it exists
        cancelBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            LocalDateTime selectedHour = hours.get(selectedIndex);

            boolean canceled = reservationSystem.cancelReservation(name, selectedHour);
            statusArea.append("[" + name + "] " +
                    (canceled ? "Reservation canceled at " + selectedHour + "\n" : "No reservation to cancel.\n"));
        });

        // Display a summary of reservations per hour
        statsBtn.addActionListener((ActionEvent e) -> {
            Map<LocalDateTime, Integer> stats = reservationSystem.getReservationCounts();
            statusArea.append("Reservation Summary:\n");
            stats.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> statusArea.append(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            + " - Reservations: " + entry.getValue() + "\n"));
        });

        // Add panels to main frame
        frame.add(leftPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

    // Generate a text report of table availability by hour
    private String getAvailabilityText() {
        StringBuilder builder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = now.withHour(22);
        if (now.getHour() > 22) {
            now = now.plusDays(1).withHour(9);
            end = now.withHour(22);
        }

        while (!now.isAfter(end)) {
            LocalDateTime slot = now;
            int freeTables = reservationSystem.getFreeTables(slot);
            builder.append(slot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append(" - Available tables: ").append(freeTables);
            if (freeTables == 0) {
                builder.append(" ❌");
            } else if (freeTables < 10) {
                builder.append(" ✅");
            }
            builder.append("\n");
            now = now.plusHours(1);
        }
        return builder.toString();
    }

    // Entry point to start the Swing application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationDashboard().launchUI());
    }
}
