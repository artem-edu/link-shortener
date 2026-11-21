package mephi.service;

import java.awt.*;
import java.net.URI;
import java.util.Locale;

public class BrowserService {
    public void open(String url) {
        if (isLinux() && (tryXdgOpen(url) || tryDesktop(url))) {
            System.out.println("ok");
            return;
        }
        if (tryDesktop(url)) {
            return;
        }
        System.out.println("Не удалось открыть браузер автоматически. Перейдите по ссылке вручную: " + url);
    }

    private boolean tryDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }


    private boolean tryXdgOpen(String url) {
        try {
            new ProcessBuilder("xdg-open", url).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isLinux() {
        return System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("linux");
    }
}
