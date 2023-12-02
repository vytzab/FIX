package lt.vytzab.engine.ui;

import javax.swing.JFrame;

import lt.vytzab.engine.EngineApplication;
import lt.vytzab.engine.LogPanel;

import java.awt.BorderLayout;

public class EngineFrame extends JFrame {

    public EngineFrame(LogPanel logPanel, final EngineApplication application) {
        super();
        setTitle("Engine Frame");
        setSize(600, 400);
        getContentPane().add(new EnginePanel(logPanel, application),
                BorderLayout.CENTER);
        setVisible(true);
    }
}