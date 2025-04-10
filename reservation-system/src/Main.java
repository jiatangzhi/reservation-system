public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ReservationDashboard dashboard = new ReservationDashboard();
            dashboard.launchUI();
        });
    }
}
