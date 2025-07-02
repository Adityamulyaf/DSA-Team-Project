import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles the graphical user interface and orchestrates search operations.
 */
public class GUI extends JFrame { // Change MainTreeGUI to GUI and extend JFrame directly
    private JTextField dirField, fileField;
    private JCheckBox findAllCheck;
    private JRadioButton bfsButton, dfsButton;
    private JButton searchButton, clearButton;
    private VerticalTreePanel treePanel;
    private JScrollPane treeScroll; // Make JScrollPane a member to access its scrollbar
    private JEditorPane resultPane;
    private JLabel statusLabel, timeLabel;

    // Data for search and visualization
    private Map<String, TreeNodeData> nodeMap;
    private Set<String> foundFiles;
    private Set<String> visitedPaths;
    private List<String> traversalOrder;
    private TreeNodeData rootNode;
    private long searchStartTime;

    // Layout constants
    private static final int NODE_WIDTH = 120;
    private static final int NODE_HEIGHT = 60;
    private static final int SIBLING_SPACING = 30;
    private static final int LEVEL_SPACING = 80;

    public GUI() { // Constructor
        super("Folder Crawler"); // Set frame title
        nodeMap = new HashMap<>();
        foundFiles = new HashSet<>();
        visitedPaths = new HashSet<>();
        traversalOrder = new ArrayList<>();
        initializeGUI();
    }

    private void initializeGUI() {
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        treePanel = new VerticalTreePanel();
        treeScroll = new JScrollPane(treePanel);
        treeScroll.setBorder(BorderFactory.createTitledBorder("Vertical Directory Tree"));
        add(treeScroll, BorderLayout.CENTER);

        JPanel resultPanel = createResultPanel();
        add(resultPanel, BorderLayout.SOUTH);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready to search...");
        timeLabel = new JLabel("Runtime: 0 ms");
        timeLabel.setForeground(Color.BLUE);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Search Controls"));
        controlPanel.setPreferredSize(new Dimension(300, 0));

        controlPanel.add(new JLabel("Root Directory:"));
        dirField = new JTextField(System.getProperty("user.home"));
        controlPanel.add(dirField);
        controlPanel.add(Box.createVerticalStrut(10));

        controlPanel.add(new JLabel("File Pattern:"));
        fileField = new JTextField("*.txt");
        controlPanel.add(fileField);
        controlPanel.add(Box.createVerticalStrut(10));

        findAllCheck = new JCheckBox("Find All Occurrences", true);
        controlPanel.add(findAllCheck);
        controlPanel.add(Box.createVerticalStrut(10));

        controlPanel.add(new JLabel("Search Algorithm:"));
        bfsButton = new JRadioButton("BFS (Breadth-First)", true);
        dfsButton = new JRadioButton("DFS (Depth-First)");
        ButtonGroup methodGroup = new ButtonGroup();
        methodGroup.add(bfsButton);
        methodGroup.add(dfsButton);
        controlPanel.add(bfsButton);
        controlPanel.add(dfsButton);
        controlPanel.add(Box.createVerticalStrut(15));

        searchButton = new JButton("🔍 Start Search");
        clearButton = new JButton("🗑 Clear Tree");
        JButton zoomInButton = new JButton("🔍+ Zoom In");
        JButton zoomOutButton = new JButton("🔍- Zoom Out");

        searchButton.setPreferredSize(new Dimension(250, 30));
        clearButton.setPreferredSize(new Dimension(250, 30));
        zoomInButton.setPreferredSize(new Dimension(250, 30));
        zoomOutButton.setPreferredSize(new Dimension(250, 30));

        controlPanel.add(searchButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(zoomInButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(zoomOutButton);

        controlPanel.add(Box.createVerticalStrut(20));
        JPanel perfPanel = new JPanel();
        perfPanel.setBorder(BorderFactory.createTitledBorder("Performance"));
        perfPanel.setLayout(new BoxLayout(perfPanel, BoxLayout.Y_AXIS));

        JLabel bfsInfo = new JLabel("<html><b>BFS:</b> Level by level<br>Memory: O(w) width</html>");
        JLabel dfsInfo = new JLabel("<html><b>DFS:</b> Depth first<br>Memory: O(h) height</html>");
        perfPanel.add(bfsInfo);
        perfPanel.add(dfsInfo);
        controlPanel.add(perfPanel);

        controlPanel.add(Box.createVerticalStrut(10));
        JPanel legendPanel = new JPanel();
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));

        JLabel rootLabel = new JLabel("● Root (Orange)");
        rootLabel.setForeground(Color.ORANGE);
        legendPanel.add(rootLabel);

        JLabel folderLabel = new JLabel("● Folder - Visited (Green)");
        folderLabel.setForeground(Color.GREEN);
        legendPanel.add(folderLabel);

        JLabel fileLabel = new JLabel("● File - Visited (Blue)");
        fileLabel.setForeground(Color.BLUE);
        legendPanel.add(fileLabel);

        JLabel foundLabel = new JLabel("● Target Found (Red)");
        foundLabel.setForeground(Color.RED);
        legendPanel.add(foundLabel);

        JLabel notVisitedLabel = new JLabel("● Not Visited (Gray)");
        notVisitedLabel.setForeground(Color.GRAY);
        legendPanel.add(notVisitedLabel);

        controlPanel.add(legendPanel);

        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearResults());
        zoomInButton.addActionListener(e -> { treePanel.zoomIn(); });
        zoomOutButton.addActionListener(e -> { treePanel.zoomOut(); });

        return controlPanel;
    }

    private JPanel createResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Search Results & Performance Analysis"));
        resultPanel.setPreferredSize(new Dimension(0, 200));

        resultPane = new JEditorPane("text/html", "<html><body><i>Search results will appear here...</i></body></html>");
        resultPane.setEditable(false);
        resultPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().open(new File(e.getDescription()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cannot open file: " + ex.getMessage());
                }
            }
        });

        JScrollPane resultScroll = new JScrollPane(resultPane);
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        return resultPanel;
    }

    private void performSearch() {
        String rootDir = dirField.getText().trim();
        String targetFile = fileField.getText().trim();
        boolean findAll = findAllCheck.isSelected();

        if (rootDir.isEmpty() || targetFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both directory and file pattern to search!");
            return;
        }

        File rootDirFile = new File(rootDir);
        if (!rootDirFile.exists() || !rootDirFile.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid directory path!");
            return;
        }

        nodeMap.clear();
        foundFiles.clear();
        visitedPaths.clear();
        traversalOrder.clear();

        statusLabel.setText("Building tree structure...");
        searchButton.setEnabled(false);

        searchStartTime = System.currentTimeMillis();

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Build the complete tree structure first
                buildCompleteTree(rootDir);

                // Initialize search components for algorithm
                Consumer<String> statusUpdater = (msg) -> {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(msg);
                        treePanel.repaint();
                    });
                };
                
                searchStartTime = System.currentTimeMillis(); // Reset start time for actual search duration

                if (bfsButton.isSelected()) {
                    BFS.search(rootDir, targetFile, findAll, nodeMap, visitedPaths, traversalOrder, foundFiles, statusUpdater, searchStartTime);
                } else {
                    DFS.search(new File(rootDir), targetFile, findAll, nodeMap, visitedPaths, traversalOrder, foundFiles, statusUpdater, searchStartTime);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                long currentTime = System.currentTimeMillis() - searchStartTime;
                timeLabel.setText("Runtime: " + currentTime + " ms");
            }

            @Override
            protected void done() {
                long totalTime = System.currentTimeMillis() - searchStartTime;
                searchButton.setEnabled(true);
                statusLabel.setText("Search completed. Found " + foundFiles.size() + " matches. Visited " + visitedPaths.size() + " paths.");
                timeLabel.setText("Runtime: " + totalTime + " ms");
                updateResultsDisplay(totalTime);
                treePanel.repaint();
            }
        };
        worker.execute();
    }

    private void buildCompleteTree(String rootDir) {
        rootNode = new TreeNodeData(new File(rootDir), 0, 0);
        nodeMap.put(rootDir, rootNode);
        buildTreeRecursive(rootNode, new File(rootDir), 1);

        SwingUtilities.invokeLater(() -> {
            calculateNodePositions();
            treePanel.setRootNode(rootNode); // Call setRootNode after positions are calculated
        });
    }

    private void buildTreeRecursive(TreeNodeData parentNode, File parentFile, int level) {
        if (!parentFile.isDirectory() || level > 5) return;

        File[] children = parentFile.listFiles();
        if (children == null) return;

        Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        int maxChildren = Math.min(children.length, 10);

        for (int i = 0; i < maxChildren; i++) {
            File child = children[i];
            TreeNodeData childNode = new TreeNodeData(child, level, i);
            parentNode.addChild(childNode);
            nodeMap.put(child.getAbsolutePath(), childNode);

            if (child.isDirectory()) {
                buildTreeRecursive(childNode, child, level + 1);
            }
        }
    }

    private void calculateNodePositions() {
        if (rootNode == null) return;

        // Step 1: Calculate subtree widths bottom-up
        calculateSubtreeWidths(rootNode);

        // Step 2: Position nodes using improved algorithm
        positionNodes(rootNode, 0, 0); // Start with relative 0,0

        // Calculate max dimensions for scrollable panel
        int maxX = 0;
        int maxY = 0;
        int minX = 0;
        for(TreeNodeData node : nodeMap.values()){
            maxX = Math.max(maxX, node.x + NODE_WIDTH / 2);
            maxY = Math.max(maxY, node.y + NODE_HEIGHT / 2);
            minX = Math.min(minX, node.x - NODE_WIDTH / 2);
        }
        
        int totalWidthNeeded = maxX - minX;
        int totalHeightNeeded = maxY + 100; // Add some padding at bottom

        // Set preferred size for the scroll pane to adapt
        treePanel.setPreferredSize(new Dimension(totalWidthNeeded + 100, totalHeightNeeded + 100)); // Add extra padding
        treePanel.revalidate();
    }

    private int calculateSubtreeWidths(TreeNodeData node) {
        if (node.isLeaf()) {
            node.subtreeWidth = NODE_WIDTH + SIBLING_SPACING; // Lebar node + sedikit jarak untuk saudara
            return node.subtreeWidth;
        }

        int totalChildWidth = 0;
        for (TreeNodeData child : node.children) {
            totalChildWidth += calculateSubtreeWidths(child);
        }
        // Remove the last SIBLING_SPACING as it's not needed after the last child
        if (!node.children.isEmpty()) {
            totalChildWidth -= SIBLING_SPACING;
        }
        
        node.subtreeWidth = Math.max(NODE_WIDTH, totalChildWidth);
        // Add SIBLING_SPACING back if node itself is a parent, for consistency
        if (!node.isLeaf()) {
            node.subtreeWidth += SIBLING_SPACING;
        }

        return node.subtreeWidth;
    }

    private void positionNodes(TreeNodeData node, int startX, int startY) {
        // Position current node at center of its allocated width
        node.x = startX + node.subtreeWidth / 2 - (SIBLING_SPACING/2); // Adjust for sibling spacing calculation
        node.y = startY;

        // Position children
        if (!node.isLeaf()) {
            // Children start from the left edge of parent's combined subtree width, then center each child
            int currentChildX = startX;
            int childY = startY + LEVEL_SPACING;

            for (TreeNodeData child : node.children) {
                positionNodes(child, currentChildX, childY);
                currentChildX += child.subtreeWidth;
            }
        }
    }

    private void updateResultsDisplay(long executionTime) {
        StringBuilder htmlResults = new StringBuilder("<html><body>");
        htmlResults.append("<h3>Search Results</h3>");

        if (foundFiles.isEmpty()) {
            htmlResults.append("<p><i>No files found matching the search criteria.</i></p>");
        } else {
            htmlResults.append("<p><b>Found ").append(foundFiles.size()).append(" file(s):</b></p>");
            htmlResults.append("<ol>");
            for (String path : foundFiles) {
                File file = new File(path);
                htmlResults.append("<li><a href='").append(path).append("'>")
                        .append(file.getName()).append("</a><br><small>")
                        .append(path).append("</small></li>");
            }
            htmlResults.append("</ol>");
        }

        htmlResults.append("<hr><h3>Performance Analysis</h3>");
        htmlResults.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        htmlResults.append("<tr><th align='left'>Metric</th><th align='left'>Value</th></tr>");
        htmlResults.append("<tr><td><b>Algorithm Used</b></td><td>").append(bfsButton.isSelected() ? "BFS (Breadth-First Search)" : "DFS (Depth-First Search)").append("</td></tr>");
        htmlResults.append("<tr><td><b>Execution Time</b></td><td>").append(executionTime).append(" ms</td></tr>");
        htmlResults.append("<tr><td><b>Nodes Visited</b></td><td>").append(visitedPaths.size()).append("</td></tr>");
        htmlResults.append("<tr><td><b>Total Nodes in Tree</b></td><td>").append(nodeMap.size()).append("</td></tr>");
        htmlResults.append("<tr><td><b>Files Found</b></td><td>").append(foundFiles.size()).append("</td></tr>");
        
        // --- START PERUBAHAN UNTUK SEARCH EFFICIENCY ---
        if (nodeMap.size() > 0) {
            if (findAllCheck.isSelected()) {
                htmlResults.append("<tr><td><b>Search Efficiency</b></td><td>0.0%</td></tr>"); // Diatur menjadi 0.0% jika Find All diaktifkan
            } else {
                double efficiencyPercentage = 0.0;
                if (foundFiles.isEmpty()) {
                    // If no file found, and we searched the entire relevant tree, it's 100% efficient at confirming absence
                    efficiencyPercentage = 100.0;
                } else {
                    // If file found (and not findAll), efficiency is inverse of visited ratio
                    efficiencyPercentage = (1.0 - (double) visitedPaths.size() / nodeMap.size()) * 100.0;
                    efficiencyPercentage = Math.max(0.0, efficiencyPercentage); // Cap at 0% minimum
                }
                htmlResults.append("<tr><td><b>Search Efficiency</b></td><td>").append(String.format("%.1f%%", efficiencyPercentage)).append("</td></tr>");
            }
        }
        // --- AKHIR PERUBAHAN UNTUK SEARCH EFFICIENCY ---
        
        htmlResults.append("</table>");

        htmlResults.append("<hr><h3>Traversal Order (").append(bfsButton.isSelected() ? "BFS" : "DFS").append(")</h3>");
        htmlResults.append("<p><small>Order: ");
        for (int i = 0; i < Math.min(traversalOrder.size(), 15); i++) {
            String path = traversalOrder.get(i);
            File file = new File(path);
            if (i > 0) htmlResults.append(" → ");
            htmlResults.append("<span style='color: blue;'>").append(file.getName()).append("</span>");
        }
        if (traversalOrder.size() > 15) {
            htmlResults.append(" → <i>... and ").append(traversalOrder.size() - 15).append(" more</i>");
        }
        htmlResults.append("</small></p>");

        htmlResults.append("</body></html>");
        resultPane.setText(htmlResults.toString());
    }

    private void clearResults() {
        nodeMap.clear();
        foundFiles.clear();
        visitedPaths.clear();
        traversalOrder.clear();
        rootNode = null;

        treePanel.setRootNode(null); // This will also reset panOffset
        treePanel.setPreferredSize(new Dimension(treePanel.getWidth(), treePanel.getHeight())); // Maintain current size for consistent scrollbar behavior
        treePanel.revalidate();
        treePanel.repaint();
        resultPane.setText("<html><body><i>Results cleared. Ready for new search...</i></body></html>");
        statusLabel.setText("Ready to search...");
        timeLabel.setText("Runtime: 0 ms");
    }
    
    // VerticalTreePanel (inner class of GUI)
    private class VerticalTreePanel extends JPanel {
        private TreeNodeData rootNode;
        private double scale = 1.0;
        private final Point panOffset = new Point(0, 0);
        private Point dragStartPoint;

        public VerticalTreePanel() {
            setBackground(Color.WHITE);

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    dragStartPoint = e.getPoint();
                }

                public void mouseReleased(java.awt.event.MouseEvent e) {
                    dragStartPoint = null;
                }
            });

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    if (dragStartPoint != null) {
                        Point currentPoint = e.getPoint();
                        panOffset.x += currentPoint.x - dragStartPoint.x;
                        panOffset.y += currentPoint.y - dragStartPoint.y;
                        dragStartPoint = currentPoint;
                        repaint();
                    }
                }
            });
        }

        public void setRootNode(TreeNodeData rootNode) {
            this.rootNode = rootNode;
            if (rootNode != null) {
                int minTreeX = Integer.MAX_VALUE;
                int maxTreeX = Integer.MIN_VALUE;
                if (nodeMap.isEmpty() && rootNode != null) { // Special case for a single root
                    minTreeX = rootNode.x - NODE_WIDTH / 2;
                    maxTreeX = rootNode.x + NODE_WIDTH / 2;
                } else {
                    for (TreeNodeData node : nodeMap.values()) {
                        minTreeX = Math.min(minTreeX, node.x - NODE_WIDTH / 2);
                        maxTreeX = Math.max(maxTreeX, node.x + NODE_WIDTH / 2);
                    }
                }
                
                int treeTotalContentWidth = maxTreeX - minTreeX;
                
                panOffset.x = (int) ((getWidth() - (treeTotalContentWidth * scale)) / 2.0);
                panOffset.x -= (int) (minTreeX * scale);

                panOffset.y = 50; // Keep a fixed vertical offset from the top
            } else {
                // Reset panOffset when rootNode is null (tree cleared)
                panOffset.x = 0;
                panOffset.y = 0;
            }
            revalidate();
            repaint();
        }

        public void zoomIn() {
            Point oldCenter = new Point((int)((-panOffset.x + getWidth() / 2.0) / scale), (int)((-panOffset.y + getHeight() / 2.0) / scale));
            scale = Math.min(scale * 1.2, 3.0);
            panOffset.x = (int) (getWidth() / 2.0 - oldCenter.x * scale);
            panOffset.y = (int) (getHeight() / 2.0 - oldCenter.y * scale);
            repaint();
        }

        public void zoomOut() {
            Point oldCenter = new Point((int)((-panOffset.x + getWidth() / 2.0) / scale), (int)((-panOffset.y + getHeight() / 2.0) / scale));
            scale = Math.max(scale / 1.2, 0.3);
            panOffset.x = (int) (getWidth() / 2.0 - oldCenter.x * scale);
            panOffset.y = (int) (getHeight() / 2.0 - oldCenter.y * scale);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (rootNode == null) {
                g.setColor(Color.GRAY);
                g.drawString("No tree to display. Click 'Start Search' to begin.", 50, 50);
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.translate(panOffset.x, panOffset.y);
            g2d.scale(scale, scale);

            drawAllEdges(g2d, rootNode);
            drawAllNodes(g2d, rootNode);

            g2d.dispose();
        }

        private void drawAllEdges(Graphics2D g2d, TreeNodeData node) {
            if (node == null) return;

            g2d.setStroke(new BasicStroke(2.0f));
            g2d.setColor(new Color(100, 100, 100, 150)); // Semi-transparent gray

            for (TreeNodeData child : node.children) {
                g2d.drawLine(node.x, node.y + (NODE_HEIGHT / 2) - 10, child.x, child.y - (NODE_HEIGHT / 2) + 10);
                drawAllEdges(g2d, child);
            }
        }

        private void drawAllNodes(Graphics2D g2d, TreeNodeData node) {
            if (node == null) return;

            drawSingleNode(g2d, node);

            for (TreeNodeData child : node.children) {
                drawAllNodes(g2d, child);
            }
        }

        private void drawSingleNode(Graphics2D g2d, TreeNodeData node) {
            String path = node.file.getAbsolutePath();
            Color nodeColor;

            if (foundFiles.contains(path)) {
                nodeColor = new Color(220, 20, 20); // Bright red for found files
            } else if (visitedPaths.contains(path)) {
                nodeColor = node.file.isDirectory() ?
                        new Color(34, 139, 34) :     // Forest green for visited directories
                        new Color(70, 130, 180);      // Steel blue for visited files
            } else {
                nodeColor = new Color(180, 180, 180); // Light gray for unvisited
            }

            if (node.level == 0) {
                nodeColor = new Color(255, 165, 0); // Orange for root
            }

            int circleDiameter = 40; // Adjusted for better visual size
            int circleRadius = circleDiameter / 2;
            int circleDrawX = node.x - circleRadius;
            int circleDrawY = node.y - circleRadius;

            g2d.setColor(nodeColor);
            g2d.fillOval(circleDrawX, circleDrawY, circleDiameter, circleDiameter);

            g2d.setColor(nodeColor.darker());
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawOval(circleDrawX, circleDrawY, circleDiameter, circleDiameter);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String levelText = "L" + node.level;
            FontMetrics fm = g2d.getFontMetrics();
            int textX = node.x - fm.stringWidth(levelText) / 2;
            int textY = node.y + fm.getAscent() / 2 - 2;
            g2d.drawString(levelText, textX, textY);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = node.file.getName();
            if (label.isEmpty()) {
                label = node.file.getPath(); 
                if (label.length() > 15) {
                    label = label.substring(0, 13) + "...";
                }
            } else if (label.length() > 15) {
                label = label.substring(0, 13) + "...";
            }

            fm = g2d.getFontMetrics();
            int labelX = node.x - fm.stringWidth(label) / 2;
            int labelY = node.y + circleRadius + fm.getAscent() + 5;
            g2d.drawString(label, labelX, labelY);
        }
    }
}