import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservationDashboard {
    private final ReservationSystem reservationSystem = new ReservationSystem(10); // Example: 10 tables
    private final JTextArea statusArea = new JTextArea();

    public void launchUI() {
        JFrame frame = new JFrame("Restaurant Reservation System");
        frame.setSize(900, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Left panel: Table availability per hour
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

        // Right panel: Controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Reservation Controls"));

        JTextField nameField = new JTextField(15);
        rightPanel.add(new JLabel("Customer Name:"));
        rightPanel.add(nameField);

        // Hour selector using JComboBox
        JComboBox<String> hourBox = new JComboBox<>();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        List<LocalDateTime> hours = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDateTime time = now.plusHours(i);
            hours.add(time);
            hourBox.addItem(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        rightPanel.add(new JLabel("Select Hour:"));
        rightPanel.add(hourBox);

        JButton reserveBtn = new JButton("Make Reservation");
        JButton checkBtn = new JButton("Check Reservation");
        JButton cancelBtn = new JButton("Cancel Reservation");

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(reserveBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(checkBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(cancelBtn);
        rightPanel.add(Box.createVerticalStrut(15));

        // Status area to show messages and stats
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(250, 140));
        rightPanel.add(new JLabel("Status Log:"));
        rightPanel.add(statusScroll);

        // Button Actions
        reserveBtn.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            int selectedIndex = hourBox.getSelectedIndex();
            if (name.isEmpty()) {
                statusArea.append("Please enter a name.\n");
                return;
            }
            LocalDateTime selectedHour = hours.get(selectedIndex);
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

        // Add panels to frame
        frame.add(leftPanel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setVisible(true);
    }

    // Generates availability text for next few hours
    private String getAvailabilityText() {
        StringBuilder builder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        for (int i = 0; i < 12; i++) {
            LocalDateTime slot = now.plusHours(i);
            int freeTables = reservationSystem.getFreeTables(slot);
            builder.append(slot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                   .append(" - Available tables: ").append(freeTables).append("\n");
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationDashboard().launchUI());
    }
} 
