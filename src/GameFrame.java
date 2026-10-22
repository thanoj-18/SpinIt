import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Main window containing the three‑column layout:
 * left – player management, center – game canvas, right – recent spins.
 */
public class GameFrame extends JFrame {

    private final GamePanel gamePanel;
    private final SpinEngine spinEngine;
    private final List<String> players = new ArrayList<>();
    private final DefaultListModel<String> playerListModel = new DefaultListModel<>();
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private JButton spinButton;

    public GameFrame() {
        super("SpinIt");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left panel - Player management
        JPanel leftPanel = createPlayerPanel();
        
        // Right panel - Spin history
        JPanel rightPanel = createHistoryPanel();

        gamePanel = new GamePanel();
        spinEngine = new SpinEngine(gamePanel, this::onSpinComplete);

        // SPIN button
        spinButton = new JButton("SPIN");
        spinButton.setEnabled(false);
        spinButton.addActionListener(this::onSpinClicked);
        spinButton.setPreferredSize(new Dimension(100, 40));
        spinButton.setFont(spinButton.getFont().deriveFont(Font.BOLD, 16f));

        // Assemble layout
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(gamePanel, BorderLayout.CENTER);
        centerWrapper.add(createSpinButtonPanel(), BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(centerWrapper, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createPlayerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Players"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(250, 0));

        // Player list
        JList<String> playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setVisibleRowCount(10);
        JScrollPane listScroll = new JScrollPane(playerList);
        panel.add(listScroll, BorderLayout.CENTER);

        // Input panel for adding players
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        JTextField nameField = new JTextField();
        nameField.setToolTipText("Enter player name (Enter to add)");
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addPlayer(nameField.getText().trim()));
        nameField.addActionListener(e -> addPlayer(nameField.getText().trim()));
        inputPanel.add(nameField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.NORTH);

        // Remove button
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            int idx = playerList.getSelectedIndex();
            if (idx >= 0) removePlayer(idx);
        });
        panel.add(removeButton, BorderLayout.SOUTH);

        // Keyboard shortcuts for the panel
        setupPlayerPanelKeyBindings(panel, nameField, playerList);

        return panel;
    }

    private void setupPlayerPanelKeyBindings(JPanel panel, JTextField nameField, JList<String> playerList) {
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

        // Space to spin (when not typing in name field)
        im.put(KeyStroke.getKeyStroke("SPACE"), "spin");
        am.put("spin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.isFocusOwner() && spinButton.isEnabled()) {
                    onSpinClicked(null);
                }
            }
        });

        // Delete to remove selected player
        im.put(KeyStroke.getKeyStroke("DELETE"), "removePlayer");
        am.put("removePlayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.isFocusOwner()) {
                    int idx = playerList.getSelectedIndex();
                    if (idx >= 0) removePlayer(idx);
                }
            }
        });
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Last 5 Spins"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(250, 0));

        JList<String> historyList = new JList<>(historyListModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setVisibleRowCount(10);
        JScrollPane scroll = new JScrollPane(historyList);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSpinButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        spinButton.setToolTipText("Click or press SPACE to spin");
        panel.add(spinButton);
        return panel;
    }

    private void addPlayer(String name) {
        if (name.isEmpty()) return;
        if (players.contains(name)) {
            JOptionPane.showMessageDialog(this, "Player already exists!", "Duplicate", JOptionPane.WARNING_MESSAGE);
            return;
        }
        players.add(name);
        playerListModel.addElement(name);
        gamePanel.setPlayers(players);
        updateSpinButtonState();
    }

    private void removePlayer(int index) {
        if (index < 0 || index >= players.size()) return;
        players.remove(index);
        playerListModel.remove(index);
        gamePanel.setPlayers(players);
        updateSpinButtonState();
    }

    private void updateSpinButtonState() {
        spinButton.setEnabled(players.size() >= 2 && !spinEngine.isSpinning());
    }

    private void onSpinClicked(ActionEvent e) {
        spinButton.setEnabled(false);
        spinEngine.startSpin();
    }

    /** Callback from SpinEngine when animation finishes. */
    private void onSpinComplete(String winner) {
        // Add to history (newest first)
        String entry = winner + "  •  " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        historyListModel.add(0, entry);
        // Keep only last 5
        while (historyListModel.size() > 5) {
            historyListModel.remove(historyListModel.size() - 1);
        }
        
        // Find winner index and trigger highlight animation
        int winnerIdx = players.indexOf(winner);
        if (winnerIdx >= 0) {
            gamePanel.highlightWinner(winnerIdx);
        }
        
        // Re-enable spin button if we still have enough players
        updateSpinButtonState();
    }
}