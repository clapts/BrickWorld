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

    // Marrone chiaro di sfondo
    private static final Color LOCANDA_LIGHT = new Color(0xDEB887);

    // Variabili per la selezione corrente (pennello e personaggi)
    private Color selectedColor = null;
    private String selectedCharacter = null;

    // Mappa di [personaggio -> posizione in griglia]
    private final Map<String, Point> characterPositions = new HashMap<>();
    // Elenco dei personaggi creati
    private final List<String> charactersList = new ArrayList<>();

    // Numero di righe e colonne (100x100)
    private final int rows = 100;
    private final int cols = 100;

    // Dimensione delle celle (variabile, perché può cambiare con "Imposta Dimensioni")
    private int cellSize = 40;
    // Gap tra le celle
    private final int gap = 1;

    // Matrice di bottoni (celle)
    private final CellButton[][] grid = new CellButton[rows][cols];

    // Pannello che contiene le celle
    private final ScrollablePanel centerPanel;
    // ScrollPane al centro (lo usiamo per revalidate in caso di cambio dimensioni)
    private final JScrollPane centerScrollPane;

    // Pannello per i personaggi a sinistra
    private final JPanel charactersPanel = new JPanel();

    // Flag per la modalità "pennello" (sinistro), "cancellazione" (destro), e "solo evidenzia"
    private boolean isPainting = false;
    private boolean isErasing = false;
    private boolean isHighlighting = false; // se true, evidenzia soltanto

    public Mappa() {
        super("BrixWorld");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // fullscreen opzionale

        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // -------------------------
        // PANNELLO SINISTRO
        // -------------------------
        JPanel leftContainer = new JPanel();
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
        leftContainer.setBackground(LOCANDA_LIGHT);

        // Bottone "Save"
        JButton btnSave = new JButton("Save");
        formatButton(btnSave, 160, 40, Color.CYAN, Color.BLACK);
        btnSave.addActionListener(e -> onSave());
        leftContainer.add(btnSave);
        leftContainer.add(Box.createVerticalStrut(2));

        // Bottone "Load"
        JButton btnLoad = new JButton("Load");
        formatButton(btnLoad, 160, 40, Color.PINK, Color.BLACK);
        btnLoad.addActionListener(e -> onLoad());
        leftContainer.add(btnLoad);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Imposta Dimensioni"
        JButton btnImpostaDimensioni = new JButton("Imposta Dimensioni");
        formatButton(btnImpostaDimensioni, 160, 40, Color.WHITE, Color.BLACK);
        btnImpostaDimensioni.addActionListener(e -> cambiaDimensioniCelle());
        leftContainer.add(btnImpostaDimensioni);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Nuovo Personaggio"
        JButton btnNuovoPersonaggio = new JButton("Nuovo Personaggio");
        formatButton(btnNuovoPersonaggio, 160, 40, Color.LIGHT_GRAY, Color.BLACK);
        btnNuovoPersonaggio.addActionListener(e -> creaNuovoPersonaggio());
        leftContainer.add(btnNuovoPersonaggio);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Cancella (singola)"
        JButton btnCancellaSingola = new JButton("Cancella (singola)");
        formatButton(btnCancellaSingola, 160, 40, SAND, Color.BLACK);
        btnCancellaSingola.addActionListener(e -> {
            // Seleziona sabbia per pittura
            selectedColor = SAND;
            selectedCharacter = null;
            // Disattiviamo highlight
            isHighlighting = false;
        });
        leftContainer.add(btnCancellaSingola);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Cancella Tutto"
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

        // Bottone "Parete"
        JButton btnParete = new JButton("Parete");
        formatButton(btnParete, 160, 40, PARETE, Color.WHITE);
        btnParete.addActionListener(e -> {
            selectedColor = PARETE;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btnParete);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "3/4 Copertura"
        JButton btn34Cop = new JButton("3/4 Copertura");
        formatButton(btn34Cop, 160, 40, COPERTURA_3_4, Color.WHITE);
        btn34Cop.addActionListener(e -> {
            selectedColor = COPERTURA_3_4;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btn34Cop);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "1/2 Copertura"
        JButton btn12Cop = new JButton("1/2 Copertura");
        formatButton(btn12Cop, 160, 40, COPERTURA_1_2, Color.BLACK);
        btn12Cop.addActionListener(e -> {
            selectedColor = COPERTURA_1_2;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btn12Cop);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Nemico"
        JButton btnNemico = new JButton("Nemico");
        formatButton(btnNemico, 160, 40, NEMICO, Color.BLACK);
        btnNemico.addActionListener(e -> {
            selectedColor = NEMICO;
            selectedCharacter = null;
            isHighlighting = false;
        });
        leftContainer.add(btnNemico);
        leftContainer.add(Box.createVerticalStrut(5));

        // Bottone "Evidenzia"
        JButton btnEvidenzia = new JButton("Evidenzia");
        formatButton(btnEvidenzia, 160, 40, Color.WHITE, Color.WHITE);
        btnEvidenzia.addActionListener(e -> {
            // Disattiviamo pittura e cancellazione
            selectedColor = null;
            selectedCharacter = null;
            isPainting = false;
            isErasing = false;
            // Attiviamo highlight
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
        JScrollPane leftScrollPane = new JScrollPane(
                leftContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        leftScrollPane.getViewport().setBackground(LOCANDA_LIGHT);
        leftScrollPane.setPreferredSize(new Dimension(180, 0));
        getContentPane().add(leftScrollPane, BorderLayout.WEST);

        // -------------------------
        // PANNELLO CENTRALE: Griglia
        // -------------------------
        centerPanel = new ScrollablePanel(new GridLayout(rows, cols, gap, gap));
        centerPanel.setBackground(LOCANDA_LIGHT);

        int totalWidth  = cols * cellSize + (cols - 1) * gap;
        int totalHeight = rows * cellSize + (rows - 1) * gap;
        centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

        // Creiamo le celle
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new CellButton(r, c);
                centerPanel.add(grid[r][c]);
            }
        }

        centerScrollPane = new JScrollPane(
                centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        // Aumentiamo lo spessore delle barre
        centerScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(30, 0));
        centerScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 30));

        // Personalizziamo i colori delle scrollbar
        centerScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(139, 69, 19); // marrone scuro
                this.trackColor = new Color(222, 184, 135); // marrone più chiaro
            }
        });
        centerScrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(139, 69, 19);
                this.trackColor = new Color(222, 184, 135);
            }
        });
        centerScrollPane.getViewport().setBackground(LOCANDA_LIGHT);
        getContentPane().add(centerScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * Bottone "Imposta Dimensioni": cambia la dimensione delle celle senza perdere i dati.
     */
    private void cambiaDimensioniCelle() {
        String input = JOptionPane.showInputDialog(
                this,
                "Inserisci la nuova dimensione delle celle (es. 50):",
                cellSize // valore di default
        );
        if (input == null) return; // utente annulla
        input = input.trim();
        if (input.isEmpty()) return;
        try {
            int newSize = Integer.parseInt(input);
            if (newSize < 1 || newSize > 200) {
                JOptionPane.showMessageDialog(this, "Valore non valido (1-200).");
                return;
            }
            // Aggiorniamo la cellSize
            cellSize = newSize;

            // Aggiorniamo ciascuna cella
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    grid[r][c].aggiornaDimensione(cellSize);
                }
            }

            // Ricalcoliamo la dimensione preferita del centerPanel
            int totalWidth  = cols * cellSize + (cols - 1) * gap;
            int totalHeight = rows * cellSize + (rows - 1) * gap;
            centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

            // Revalidate e repaint
            centerPanel.revalidate();
            centerPanel.repaint();
            centerScrollPane.revalidate();
            centerScrollPane.repaint();

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
            // Numero personaggi
            pw.println(charactersList.size());
            // Elenco nomi personaggi
            for (String name : charactersList) {
                pw.println(name);
            }
            pw.println("STARTGRID");
            // Griglia 100x100
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
        button.setOpaque(true); // importante su macOS con cross LAF
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
    // Celle
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
                    // Se la modalità è "evidenzia", saltiamo pittura e cancellazione
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
                    // Se la modalità è "evidenzia", facciamo solo l'hover
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
                    // Se "evidenzia", non facciamo nulla
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

        public void aggiornaDimensione(int newSize) {
            setDimensioniCella(newSize);
        }

        private void paintCell() {
            if (occupantCharacter != null) {
                characterPositions.remove(occupantCharacter);
                occupantCharacter = null;
                setText("");
            }
            setBaseColor(selectedColor);
        }

        private void eraseCell() {
            if (occupantCharacter != null) {
                characterPositions.remove(occupantCharacter);
                occupantCharacter = null;
                setText("");
            }
            setBaseColor(SAND);
        }

        private void placeCharacter() {
            if (characterPositions.containsKey(selectedCharacter)) {
                Point oldPos = characterPositions.get(selectedCharacter);
                if (oldPos != null) {
                    grid[oldPos.x][oldPos.y].setOccupantCharacter(null);
                }
            }
            characterPositions.put(selectedCharacter, new Point(row, col));
            setOccupantCharacter(selectedCharacter);
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
            // Forziamo il Look & Feel cross-platform: su macOS i bottoni rispetteranno i colori custom
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(Mappa::new);
    }
}
