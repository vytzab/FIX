package lt.vytzab.initiator.ui;

import lt.vytzab.initiator.ExecutionTableModel;

import javax.swing.*;
import java.awt.*;

/**
 * Contains the logs panel
 */
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

    public DefaultListModel<String> getLogModel () {
        return model;
    }
}