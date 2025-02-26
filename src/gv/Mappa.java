package gv;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Mappa extends JFrame {

    // Colori base
    private static final Color SAND = new Color(245, 222, 179);   // marroncino sabbia
    private static final Color PARETE = new Color(50, 50, 50);    // grigio molto scuro
    private static final Color COPERTURA_3_4 = new Color(100, 100, 100);
    private static final Color COPERTURA_1_2 = new Color(150, 150, 150);
    private static final Color NEMICO = new Color(255, 127, 80);  // rosso corallo

    // Marrone chiaro di sfondo (locanda)
    private static final Color LOCANDA_LIGHT = new Color(0xDEB887);

    // Variabili per la selezione corrente
    private Color selectedColor = null;
    private String selectedCharacter = null;

    // Mappa dei personaggi e elenco
    private final Map<String, Point> characterPositions = new HashMap<>();
    private final List<String> charactersList = new ArrayList<>();

    // Dimensioni griglia 100x100
    private final int rows = 100;
    private final int cols = 100;

    // Dimensione delle celle (variabile)
    private int cellSize = 40;
    // Gap tra le celle
    private final int gap = 1;

    // Matrice di bottoni (celle)
    private final CellButton[][] grid = new CellButton[rows][cols];

    // Pannello centrale e scrollPane (finestra principale)
    private final ScrollablePanel centerPanel;
    private final JScrollPane centerScrollPane;

    // Pannello per i personaggi a sinistra
    private final JPanel charactersPanel = new JPanel();

    // Flag per modalità
    private boolean isPainting = false;
    private boolean isErasing = false;
    private boolean isHighlighting = false; // se true, evidenzia soltanto

    // Riferimento alla finestra secondaria (se aperta)
    private static MappaSecondaria secondFrame = null;

    public Mappa() {
        super("BrixWorld");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // -------------------------
        // PANNELLO SINISTRO
        // -------------------------
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setBackground(LOCANDA_LIGHT);

        // Pulsante "Finestra 2": apre la finestra secondaria
        JButton btnFinestra2 = new JButton("Finestra 2");
        formatButton(btnFinestra2, 160, 40, Color.ORANGE, Color.BLACK);
        btnFinestra2.addActionListener(e -> {
            if (secondFrame == null) {
                secondFrame = new MappaSecondaria(rows, cols, cellSize, gap);
            } else {
                secondFrame.toFront();
            }
        });
        leftContainer.add(btnFinestra2);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Save"
        JButton btnSave = new JButton("Save");
        formatButton(btnSave, 160, 40, Color.CYAN, Color.BLACK);
        btnSave.addActionListener(e -> onSave());
        leftContainer.add(btnSave);
        leftContainer.add(Box.createVerticalStrut(2));

        // Pulsante "Load"
        JButton btnLoad = new JButton("Load");
        formatButton(btnLoad, 160, 40, Color.PINK, Color.BLACK);
        btnLoad.addActionListener(e -> onLoad());
        leftContainer.add(btnLoad);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Imposta Dimensioni"
        JButton btnImpostaDimensioni = new JButton("Imposta Dimensioni");
        formatButton(btnImpostaDimensioni, 160, 40, Color.WHITE, Color.BLACK);
        btnImpostaDimensioni.addActionListener(e -> cambiaDimensioniCelle());
        leftContainer.add(btnImpostaDimensioni);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Nuovo Personaggio"
        JButton btnNuovoPersonaggio = new JButton("Nuovo Personaggio");
        formatButton(btnNuovoPersonaggio, 160, 40, Color.LIGHT_GRAY, Color.BLACK);
        btnNuovoPersonaggio.addActionListener(e -> creaNuovoPersonaggio());
        leftContainer.add(btnNuovoPersonaggio);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Cancella (singola)"
        JButton btnCancellaSingola = new JButton("Cancella (singola)");
        formatButton(btnCancellaSingola, 160, 40, SAND, Color.BLACK);
        btnCancellaSingola.addActionListener(e -> {
            selectedColor = SAND;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btnCancellaSingola);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Cancella Tutto"
        JButton btnCancellaTutto = new JButton("Cancella Tutto");
        formatButton(btnCancellaTutto, 160, 40, Color.WHITE, Color.BLACK);
        btnCancellaTutto.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    Mappa.this,
                    "Sei sicuro di voler CANCELLARE TUTTA la mappa?",
                    "Conferma cancellazione",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                clearAll();
            }
        });
        leftContainer.add(btnCancellaTutto);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Parete"
        JButton btnParete = new JButton("Parete");
        formatButton(btnParete, 160, 40, PARETE, Color.WHITE);
        btnParete.addActionListener(e -> {
            selectedColor = PARETE;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btnParete);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "3/4 Copertura"
        JButton btn34Cop = new JButton("3/4 Copertura");
        formatButton(btn34Cop, 160, 40, COPERTURA_3_4, Color.WHITE);
        btn34Cop.addActionListener(e -> {
            selectedColor = COPERTURA_3_4;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btn34Cop);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "1/2 Copertura"
        JButton btn12Cop = new JButton("1/2 Copertura");
        formatButton(btn12Cop, 160, 40, COPERTURA_1_2, Color.BLACK);
        btn12Cop.addActionListener(e -> {
            selectedColor = COPERTURA_1_2;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btn12Cop);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Nemico"
        JButton btnNemico = new JButton("Nemico");
        formatButton(btnNemico, 160, 40, NEMICO, Color.BLACK);
        btnNemico.addActionListener(e -> {
            selectedColor = NEMICO;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btnNemico);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pulsante "Evidenzia"
        JButton btnEvidenzia = new JButton("Evidenzia");
        formatButton(btnEvidenzia, 160, 40, Color.WHITE, Color.WHITE);
        btnEvidenzia.addActionListener(e -> {
            selectedColor = null;
            selectedCharacter = null;
            isPainting = false;
            isErasing = false;
            isHighlighting = true;
        });
        leftContainer.add(btnEvidenzia);
        leftContainer.add(Box.createVerticalStrut(5));

        // Pannello personaggi
        charactersPanel.setLayout(new BoxLayout(charactersPanel, BoxLayout.Y_AXIS));
        charactersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        charactersPanel.setBackground(LOCANDA_LIGHT);
        leftContainer.add(charactersPanel);

        // ScrollPane sinistro (larghezza 180 px)
        JScrollPane leftScrollPane = new JScrollPane(leftContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.getViewport().setBackground(LOCANDA_LIGHT);
        leftScrollPane.setPreferredSize(new Dimension(180, 0));
        getContentPane().add(leftScrollPane, BorderLayout.WEST);

        // -------------------------
        // PANNELLO CENTRALE: Griglia
        // -------------------------
        centerPanel = new ScrollablePanel(new GridLayout(rows, cols, gap, gap));
        centerPanel.setBackground(LOCANDA_LIGHT);
        int totalWidth = cols * cellSize + (cols - 1) * gap;
        int totalHeight = rows * cellSize + (rows - 1) * gap;
        centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

        // Creiamo le celle
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new CellButton(r, c);
                centerPanel.add(grid[r][c]);
            }
        }

        centerScrollPane = new JScrollPane(centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centerScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(30, 0));
        centerScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 30));
        centerScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(139, 69, 19);
                trackColor = new Color(222, 184, 135);
            }
        });
        centerScrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(139, 69, 19);
                trackColor = new Color(222, 184, 135);
            }
        });
        centerScrollPane.getViewport().setBackground(LOCANDA_LIGHT);
        getContentPane().add(centerScrollPane, BorderLayout.CENTER);

        // Sincronizza lo scrolling con la finestra secondaria
        centerScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (secondFrame != null) {
                secondFrame.syncScrollVertical(e.getValue());
            }
        });
        centerScrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
            if (secondFrame != null) {
                secondFrame.syncScrollHorizontal(e.getValue());
            }
        });

        setVisible(true);
    }

    // Metodo per aggiornare la finestra secondaria per la cella (r, c)
    private void updateSecondaryFrame(int r, int c) {
        if (secondFrame != null) {
            secondFrame.updateCell(r, c, grid[r][c].getBaseColor(), grid[r][c].getOccupantCharacter());
        }
    }

    // Bottone "Imposta Dimensioni": cambia la dimensione delle celle senza perdere i dati.
    private void cambiaDimensioniCelle() {
        String input = JOptionPane.showInputDialog(this, "Inserisci la nuova dimensione delle celle (es. 50):", cellSize);
        if (input == null) return;
        input = input.trim();
        if (input.isEmpty()) return;
        try {
            int newSize = Integer.parseInt(input);
            if (newSize < 1 || newSize > 200) {
                JOptionPane.showMessageDialog(this, "Valore non valido (1-200).");
                return;
            }
            cellSize = newSize;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    grid[r][c].aggiornaDimensioniCella(cellSize);
                    updateSecondaryFrame(r, c);
                }
            }
            int totalWidth = cols * cellSize + (cols - 1) * gap;
            int totalHeight = rows * cellSize + (rows - 1) * gap;
            centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
            centerPanel.revalidate();
            centerPanel.repaint();
            centerScrollPane.revalidate();
            centerScrollPane.repaint();
            if (secondFrame != null) {
                secondFrame.updateDimensions(cellSize);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserisci un numero intero valido.");
        }
    }

    // ---------------------------
    // Metodi Save/Load
    // ---------------------------
    private void onSave() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveToFile(file);
        }
    }

    private void onLoad() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadFromFile(file);
        }
    }

    private void saveToFile(File file) {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(charactersList.size());
            for (String name : charactersList) {
                pw.println(name);
            }
            pw.println("STARTGRID");
            for (int r = 0; r < rows; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < cols; c++) {
                    CellButton cell = grid[r][c];
                    String occupant = cell.getOccupantCharacter();
                    if (occupant != null) {
                        sb.append(occupant);
                    } else {
                        Color col = cell.getBaseColor();
                        String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
                        sb.append(hex);
                    }
                    if (c < cols - 1) sb.append(",");
                }
                pw.println(sb.toString());
            }
            pw.println("ENDGRID");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore di scrittura file:\n" + ex.getMessage());
        }
    }

    private void loadFromFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            clearAll();
            String line = br.readLine();
            int numCharacters = Integer.parseInt(line.trim());
            for (int i = 0; i < numCharacters; i++) {
                String name = br.readLine().trim();
                addCharacterToUI(name);
            }
            line = br.readLine();
            if (!"STARTGRID".equals(line)) {
                throw new IOException("File non valido: manca STARTGRID");
            }
            for (int r = 0; r < rows; r++) {
                line = br.readLine();
                if (line == null) {
                    throw new IOException("File non valido: mancano righe della griglia");
                }
                String[] tokens = line.split(",", -1);
                if (tokens.length != cols) {
                    throw new IOException("File non valido: riga " + r + " non ha " + cols + " campi");
                }
                for (int c = 0; c < cols; c++) {
                    String value = tokens[c];
                    if (value.startsWith("#")) {
                        Color color = parseHexColor(value);
                        grid[r][c].setBaseColor(color);
                    } else {
                        if (!value.isEmpty()) {
                            grid[r][c].setOccupantCharacter(value);
                            characterPositions.put(value, new Point(r, c));
                        } else {
                            grid[r][c].setBaseColor(SAND);
                        }
                    }
                    updateSecondaryFrame(r, c);
                }
            }
            line = br.readLine();
            if (!"ENDGRID".equals(line)) {
                throw new IOException("File non valido: manca ENDGRID");
            }
            JOptionPane.showMessageDialog(this, "Mappa caricata correttamente!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore di lettura file:\n" + ex.getMessage());
        }
    }

    private Color parseHexColor(String hex) {
        String digits = hex.substring(1);
        if (digits.length() != 6) {
            return SAND;
        }
        int r = Integer.parseInt(digits.substring(0, 2), 16);
        int g = Integer.parseInt(digits.substring(2, 4), 16);
        int b = Integer.parseInt(digits.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    // ---------------------------
    // Gestione Personaggi
    // ---------------------------
    private void creaNuovoPersonaggio() {
        final String input = JOptionPane.showInputDialog(this, "Inserisci il nome del nuovo personaggio:");
        if (input != null && !input.trim().isEmpty()) {
            final String nome = input.trim();
            addCharacterToUI(nome);
        }
    }

    private void addCharacterToUI(String nome) {
        if (!charactersList.contains(nome)) {
            charactersList.add(nome);
        }
        JPanel personPanel = new JPanel();
        personPanel.setLayout(new BoxLayout(personPanel, BoxLayout.X_AXIS));
        personPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        personPanel.setBackground(LOCANDA_LIGHT);

        JButton btnPersonaggio = new JButton(nome);
        Dimension dName = new Dimension(120, 40);
        btnPersonaggio.setPreferredSize(dName);
        btnPersonaggio.setMinimumSize(dName);
        btnPersonaggio.setMaximumSize(dName);
        btnPersonaggio.setFocusPainted(false);
        btnPersonaggio.setBackground(Color.LIGHT_GRAY);
        btnPersonaggio.setForeground(Color.BLACK);
        btnPersonaggio.addActionListener(e -> {
            selectedCharacter = nome;
            selectedColor = null;
            isHighlighting = false;
        });

        JButton btnRimuovi = new JButton("X");
        Dimension dRemove = new Dimension(40, 40);
        btnRimuovi.setPreferredSize(dRemove);
        btnRimuovi.setMinimumSize(dRemove);
        btnRimuovi.setFont(new Font("SansSerif", Font.BOLD, 7));
        btnRimuovi.setMaximumSize(dRemove);
        btnRimuovi.setFocusPainted(false);
        btnRimuovi.addActionListener(e -> {
            charactersPanel.remove(personPanel);
            charactersPanel.revalidate();
            charactersPanel.repaint();
            charactersList.remove(nome);
            if (nome.equals(selectedCharacter)) {
                selectedCharacter = null;
            }
            if (characterPositions.containsKey(nome)) {
                Point p = characterPositions.remove(nome);
                if (p != null) {
                    grid[p.x][p.y].setOccupantCharacter(null);
                    updateSecondaryFrame(p.x, p.y);
                }
            }
        });

        personPanel.add(btnPersonaggio);
        personPanel.add(Box.createHorizontalStrut(2));
        personPanel.add(btnRimuovi);
        charactersPanel.add(personPanel);
        charactersPanel.add(Box.createVerticalStrut(2));
        charactersPanel.revalidate();
        charactersPanel.repaint();
    }

    // ---------------------------
    // Pulizia totale
    // ---------------------------
    private void clearAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].setBaseColor(SAND);
                updateSecondaryFrame(r, c);
            }
        }
        characterPositions.clear();
        charactersList.clear();
        charactersPanel.removeAll();
        charactersPanel.revalidate();
        charactersPanel.repaint();
    }

    // ---------------------------
    // Funzione di formattazione bottone
    // ---------------------------
    private void formatButton(JButton button, int width, int height, Color bg, Color fg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true); // importante per il CrossPlatform LookAndFeel
        Dimension d = new Dimension(width, height);
        button.setPreferredSize(d);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // ---------------------------
    // ScrollablePanel
    // ---------------------------
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

    // ---------------------------
    // Celle interattive
    // ---------------------------
    private class CellButton extends JButton {
        private final int row;
        private final int col;
        private Color baseColor = SAND;
        private String occupantCharacter = null;

        public CellButton(int r, int c) {
            this.row = r;
            this.col = c;
            setDimensioniCella(cellSize);
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(baseColor);
            setFont(new Font("SansSerif", Font.BOLD, 10));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isHighlighting) {
                        return;
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        isErasing = true;
                        eraseCell();
                    } else {
                        if (selectedCharacter != null) {
                            placeCharacter();
                        } else if (selectedColor != null) {
                            isPainting = true;
                            paintCell();
                        }
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        isErasing = false;
                    } else {
                        isPainting = false;
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isHighlighting) {
                        setBackground(shadeColor(baseColor, -30));
                        return;
                    }
                    if (isErasing) {
                        eraseCell();
                    } else if (isPainting && selectedColor != null) {
                        paintCell();
                    } else {
                        setBackground(shadeColor(baseColor, -30));
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(baseColor);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isHighlighting) {
                        return;
                    }
                    if (isErasing) {
                        eraseCell();
                    } else if (isPainting && selectedColor != null) {
                        paintCell();
                    }
                }
            });
        }

        public void setDimensioniCella(int size) {
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        public void aggiornaDimensioniCella(int newSize) {
            setDimensioniCella(newSize);
        }

        private void paintCell() {
            if (occupantCharacter != null) {
                characterPositions.remove(occupantCharacter);
                occupantCharacter = null;
                setText("");
            }
            setBaseColor(selectedColor);
            Mappa.this.updateSecondaryFrame(row, col);
        }

        private void eraseCell() {
            if (occupantCharacter != null) {
                characterPositions.remove(occupantCharacter);
                occupantCharacter = null;
                setText("");
            }
            setBaseColor(SAND);
            Mappa.this.updateSecondaryFrame(row, col);
        }

        private void placeCharacter() {
            if (characterPositions.containsKey(selectedCharacter)) {
                Point oldPos = characterPositions.get(selectedCharacter);
                if (oldPos != null) {
                    grid[oldPos.x][oldPos.y].setOccupantCharacter(null);
                    Mappa.this.updateSecondaryFrame(oldPos.x, oldPos.y);
                }
            }
            characterPositions.put(selectedCharacter, new Point(row, col));
            setOccupantCharacter(selectedCharacter);
            Mappa.this.updateSecondaryFrame(row, col);
        }

        public void setOccupantCharacter(String occupant) {
            occupantCharacter = occupant;
            setText(occupant == null ? "" : occupant.substring(0, 1).toUpperCase());
        }

        public void setBaseColor(Color c) {
            baseColor = c;
            setBackground(baseColor);
            occupantCharacter = null;
            setText("");
        }

        public String getOccupantCharacter() {
            return occupantCharacter;
        }

        public Color getBaseColor() {
            return baseColor;
        }

        private Color shadeColor(Color color, int offset) {
            int r = Math.max(0, Math.min(255, color.getRed() + offset));
            int g = Math.max(0, Math.min(255, color.getGreen() + offset));
            int b = Math.max(0, Math.min(255, color.getBlue() + offset));
            return new Color(r, g, b);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(Mappa::new);
    }
}

// ---------------------------
// Finestra secondaria (solo mappa)
// ---------------------------
class MappaSecondaria extends JFrame {

    private final int rows;
    private final int cols;
    private int cellSize;
    private final int gap;
    private final DisplayCell[][] displayGrid;
    private final ScrollablePanel centerPanel;
    private final JScrollPane centerScrollPane;
    private static final Color LOCANDA_LIGHT = new Color(0xDEB887);

    public MappaSecondaria(int rows, int cols, int cellSize, int gap) {
        super("BrixWorld - Finestra 2");
        // Rimuovo le decorazioni per lasciare la finestra con il look di default
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(null);

        this.rows=rows;
        this.cols=cols;
        this.cellSize=cellSize;
        this.gap=gap;

        centerPanel = new ScrollablePanel(new GridLayout(rows, cols, gap, gap));
        centerPanel.setBackground(LOCANDA_LIGHT);
        int totalWidth = cols * cellSize + (cols - 1) * gap;
        int totalHeight = rows * cellSize + (rows - 1) * gap;
        centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

        displayGrid = new DisplayCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                displayGrid[r][c] = new DisplayCell(cellSize);
                centerPanel.add(displayGrid[r][c]);
            }
        }

        // Nella finestra secondaria non mostriamo le scrollbar, ma lasciamo il viewport sincronizzato
        centerScrollPane = new JScrollPane(centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        centerScrollPane.getViewport().setBackground(LOCANDA_LIGHT);
        add(centerScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // Metodo per aggiornare una cella
    public void updateCell(int r, int c, Color color, String occupant) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return;
        displayGrid[r][c].setBackground(color);
        if (occupant != null && !occupant.isEmpty()) {
            displayGrid[r][c].setText(occupant.substring(0, 1).toUpperCase());
        } else {
            displayGrid[r][c].setText("");
        }
    }

    // Aggiorna le dimensioni della griglia
    public void updateDimensions(int newCellSize) {
        this.cellSize = newCellSize;
        int totalWidth = cols * cellSize + (cols - 1) * gap;
        int totalHeight = rows * cellSize + (rows - 1) * gap;
        centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                displayGrid[r][c].setPreferredSize(new Dimension(cellSize, cellSize));
                displayGrid[r][c].setSize(new Dimension(cellSize, cellSize));
            }
        }
        centerPanel.revalidate();
        centerPanel.repaint();
        centerScrollPane.revalidate();
        centerScrollPane.repaint();
    }

    // Metodo per sincronizzare lo scrolling (anche se non mostriamo le scrollbar)
    public void syncScrollVertical(int value) {
        centerScrollPane.getViewport().setViewPosition(new Point(centerScrollPane.getViewport().getViewPosition().x, value));
    }
    public void syncScrollHorizontal(int value) {
        centerScrollPane.getViewport().setViewPosition(new Point(value, centerScrollPane.getViewport().getViewPosition().y));
    }
}

// Cella per la finestra secondaria (DisplayCell) senza bordo, con sfondo SAND
class DisplayCell extends JLabel {
    public DisplayCell(int size) {
        setOpaque(true);
        setBackground(new Color(245, 222, 179)); // SAND
        setPreferredSize(new Dimension(size, size));
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        // Rimosso il bordo per avere lo stesso look delle celle della mappa principale
        setFont(new Font("SansSerif", Font.BOLD, 10));
    }
}

// ScrollablePanel (uguale a quello usato nella finestra principale)
class ScrollablePanel extends JPanel implements Scrollable {
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
