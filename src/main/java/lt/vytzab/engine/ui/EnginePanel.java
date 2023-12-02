package lt.vytzab.engine.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.LogPanel;

/**
 * Main content panel
 */
public class EnginePanel extends JPanel implements Observer, ActionListener {
    private final LogPanel logPanel;

    public EnginePanel(LogPanel logPanel, EngineApplication application) {
        setName("Engine Panel");
        this.logPanel = logPanel;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        constraints.gridx++;
        constraints.weighty = 10;

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Logs", logPanel);
        add(tabbedPane, constraints);

        constraints.weighty = 0;
    }

    public void update(Observable o, Object arg) {
    }

    public void actionPerformed(ActionEvent e) {
    }
}