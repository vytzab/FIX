package lt.vytzab.engine.ui.panels;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {
    DefaultListModel<String> model = new DefaultListModel<>();

    public LogPanel() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());
        JList<String> jList = new JList<>();
        jList.setModel(model);

        JScrollPane logsScrollPane = new JScrollPane(jList);
        add(logsScrollPane, BorderLayout.CENTER);
    }

    public DefaultListModel<String> getLogModel() {
        return model;
    }
}