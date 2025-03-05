package vr3;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class MapClient extends JFrame {
    private final int rows = 100;
    private final int cols = 100;
    private int cellSize = 40;
    private final int gap = 1;
    private final DisplayCell[][] displayGrid;
    private final ScrollablePanel centerPanel;
    private final JScrollPane centerScrollPane;
    private Socket socket;
    private BufferedReader in;
    // Matrice per salvare l'ultimo numero di sequenza per ogni cella
    private final int[][] lastSeq = new int[rows][cols];

    public MapClient(String host, int port) {
        super("BrixWorld - Client Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);

        centerPanel = new ScrollablePanel(new GridLayout(rows, cols, gap, gap));
        centerPanel.setBackground(new Color(0xDEB887));
        int totalWidth = cols * cellSize + (cols - 1) * gap;
        int totalHeight = rows * cellSize + (rows - 1) * gap;
        centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

        displayGrid = new DisplayCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                displayGrid[r][c] = new DisplayCell(cellSize);
                centerPanel.add(displayGrid[r][c]);
                lastSeq[r][c] = 0;
            }
        }

        centerScrollPane = new JScrollPane(centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(centerScrollPane, BorderLayout.CENTER);

        setVisible(true);
        connectToServer(host, port);
    }

    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this::listenForUpdates).start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore nella connessione al server: " + ex.getMessage());
        }
    }

    private void listenForUpdates() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                System.out.println("Messaggio ricevuto: " + line);
                if (line.startsWith("UPDATE;")) {
                    String[] parts = line.split(";");
                    if (parts.length == 6) {
                        int seq = Integer.parseInt(parts[1]);
                        int r = Integer.parseInt(parts[2]);
                        int c = Integer.parseInt(parts[3]);
                        // Aggiorna solo se il nuovo numero di sequenza è maggiore
                        if (seq > lastSeq[r][c]) {
                            lastSeq[r][c] = seq;
                            Color color = parseHexColor(parts[4]);
                            String occupant = parts[5];
                            if (occupant.isEmpty()) {
                                occupant = null;
                            }
                            final int row = r;
                            final int col = c;
                            final Color cellColor = color;
                            final String cellOccupant = occupant;
                            SwingUtilities.invokeLater(() ->
                                    displayGrid[row][col].updateView(cellColor, cellOccupant)
                            );
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Color parseHexColor(String hex) {
        String digits = hex.substring(1);
        if (digits.length() != 6) {
            return new Color(245, 222, 179); // SAND
        }
        int r = Integer.parseInt(digits.substring(0, 2), 16);
        int g = Integer.parseInt(digits.substring(2, 4), 16);
        int b = Integer.parseInt(digits.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 40;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 40;
        }
    }

    private static class DisplayCell extends JLabel {
        private Color baseColor;
        public DisplayCell(int size) {
            setOpaque(true);
            baseColor = new Color(245, 222, 179); // SAND
            setBackground(baseColor);
            setPreferredSize(new Dimension(size, size));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(new Font("SansSerif", Font.BOLD, 10));
        }
        public void updateView(Color color, String occupant) {
            baseColor = color;
            setBackground(color);
            setText((occupant == null || occupant.isEmpty()) ? "" : occupant.substring(0, 1).toUpperCase());
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MapClient("localhost", 5000));
    }
}
