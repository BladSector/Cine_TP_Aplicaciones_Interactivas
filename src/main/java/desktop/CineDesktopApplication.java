package desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class CineDesktopApplication {
    private CineDesktopApplication() {
    }

    public static void main(String[] args) {
        configurarApariencia();
        SwingUtilities.invokeLater(() -> {
            String apiUrl = System.getProperty("cine.api.url", "http://localhost:8080");
            new LoginFrame(new ApiClient(apiUrl)).setVisible(true);
        });
    }

    private static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo apariencia : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(apariencia.getName())) {
                    UIManager.setLookAndFeel(apariencia.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
