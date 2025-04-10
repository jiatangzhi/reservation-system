import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationDashboard {
    private final ReservationSystem reservationSystem = new ReservationSystem(10);
    private final JTextArea statusArea = new JTextArea();

    public void launchUI() {
        JFrame frame = new JFrame("Restaurant Reservation System");
        frame.setSize(950, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // LEFT PANEL: Availability Display
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Availability per Hour"));

        JTextArea availabilityArea = new JTextArea();
        availabilityArea.setEditable(false);
        availabilityArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(availabilityArea);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Availability");
        refreshButton.addActionListener(e -> availabilityArea.setText(getAvailabilityText()));
        leftPanel.add(refreshButton, BorderLayout.SOUTH);

        // RIGHT PANEL: Controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Reservation Controls"));

        JTextField nameField = new JTextField(15);
        rightPanel.add(new JLabel("Customer Name:"));
        rightPanel.add(nameField);

        JComboBox<String> hourBox = new JComboBox<>();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<LocalDateTime> hours = new ArrayList<>();
        LocalDateTime end = now.withHour(22); // Limit to 10 PM
        if (now.getHour() > 22) {
            now = now.plusDays(1).withHour(9); // If current hour > 10 PM, start next day at 9 AM
            end = now.withHour(22);
        }
        while (!now.isAfter(end)) {
            hours.add(now);
            hourBox.addItem(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            now = now.plusHours(1);
        }
        rightPanel.add(new JLabel("Select Hour:"));
        rightPanel.add(hourBox);

        JButton reserveBtn = new JButton("Make Reservation");
        JButton checkBtn = new JButton("Check Reservation");
        JButton cancelBtn = new JButton("Cancel Reservation");
        JButton statsBtn = new JButton("Show Stats");

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(reserveBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(checkBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(cancelBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(statsBtn);
        rightPanel.add(Box.createVerticalStrut(15));

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(300, 160));
        rightPanel.add(new JLabel("Status Log:"));
        rightPanel.add(statusScroll);

        // Button Actions
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

        checkBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            LocalDateTime selectedHour = hours.get(selectedIndex);
            boolean exists = reservationSystem.checkReservation(name, selectedHour);
            statusArea.append("[" + name + "] " +
                    (exists ? "Reservation exists at " + selectedHour + "\n" : "No reservation found.\n"));
        });

        cancelBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            LocalDateTime selectedHour = hours.get(selectedIndex);
            boolean canceled = reservationSystem.cancelReservation(name, selectedHour);
            statusArea.append("[" + name + "] " +
                    (canceled ? "Reservation canceled at " + selectedHour + "\n" : "No reservation to cancel.\n"));
        });

        statsBtn.addActionListener((ActionEvent e) -> {
            Map<LocalDateTime, Integer> stats = reservationSystem.getReservationCounts();
            statusArea.append("Reservation Summary:\n");
            stats.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> statusArea.append(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            + " - Reservations: " + entry.getValue() + "\n"));
        });

        frame.add(leftPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationDashboard().launchUI());
    }
}
