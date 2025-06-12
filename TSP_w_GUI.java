import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class TSP_w_GUI extends JFrame {

    static double[][] memo;
    static int[][] bestPath;
    static String[] cityNames;
    static double INF = 1e9;

    private GraphPanel graphPanel;
    private JLabel resultLabel;
    private JTextArea routeArea;
    private java.util.List<Integer> optimalRoute;
    private double totalCost;

    private Point[] nodePositions = {
        new Point(100, 300),
        new Point(300, 150),
        new Point(400, 300),
        new Point(300, 450),
        new Point(600, 150),
        new Point(500, 450),
        new Point(700, 300)
    };

    public TSP_w_GUI() {
        setTitle("TSP Graph Visualization - Surakarta Fruit Stores");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cityNames = new String[]{
            "Pasar Gede", "Rendra Buah", "Raja Buah",
            "Istana Buah Srikandi", "Kios Buah Segar",
            "Istana Buah Sukoharjo", "Toko Buah ABC Karanganyar"
        };

        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(800, 600));

        JPanel controlPanel = new JPanel(new BorderLayout());

        JButton solveButton = new JButton("Solve TSP");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveTSP();
                graphPanel.repaint();
                updateResults();
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optimalRoute = null;
                totalCost = 0;
                graphPanel.repaint();
                resultLabel.setText("Total Cost: -");
                routeArea.setText("");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(solveButton);
        buttonPanel.add(resetButton);

        resultLabel = new JLabel("Total Cost: -");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));

        routeArea = new JTextArea(8, 40);
        routeArea.setEditable(false);
        routeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(routeArea);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(resultLabel, BorderLayout.CENTER);
        controlPanel.add(scrollPane, BorderLayout.SOUTH);

        add(graphPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    static double totalCostRecursive(int mask, int pos, int n, double[][] cost) {
        if (mask == (1 << n) - 1) return cost[pos][0] == INF ? INF : cost[pos][0];
        if (memo[pos][mask] != -1) return memo[pos][mask];

        double ans = INF;
        int nextCity = -1;

        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0 && cost[pos][i] != INF) {
                int newMask = mask | (1 << i);
                double temp = cost[pos][i] + totalCostRecursive(newMask, i, n, cost);
                if (temp < ans) {
                    ans = temp;
                    nextCity = i;
                }
            }
        }

        memo[pos][mask] = ans;
        bestPath[pos][mask] = nextCity;
        return ans;
    }

    static java.util.List<Integer> reconstructPath(int n) {
        java.util.List<Integer> path = new ArrayList<>();
        int mask = 1, pos = 0, steps = 0;
        path.add(pos);

        while (true) {
            int next = bestPath[pos][mask];
            if (next == -1 || steps++ > n) break;
            path.add(next);
            mask |= (1 << next);
            pos = next;
        }

        path.add(0);
        return path;
    }

    private void solveTSP() {
        double[][] cost = {
            {0, 4.1, 7.4, 2.7, INF, INF, INF},
            {4.1, 0, 8.6, INF, 7.9, INF, INF},
            {7.4, 8.6, 0, 5.7, 11.0, 11.0, 9.1},
            {2.7, INF, 5.7, 0, INF, 11.2, INF},
            {INF, 7.9, 11.0, INF, 0, INF, 8.2},
            {INF, INF, 11.0, 11.2, INF, 0, 18.3},
            {INF, INF, 9.1, INF, 8.2, 18.3, 0}
        };

        int n = cost.length;
        memo = new double[n][1 << n];
        bestPath = new int[n][1 << n];
        for (double[] row : memo) Arrays.fill(row, -1);
        for (int[] row : bestPath) Arrays.fill(row, -1);

        totalCost = totalCostRecursive(1, 0, n, cost);
        optimalRoute = reconstructPath(n);
    }

    private void updateResults() {
        if (optimalRoute != null) {
            resultLabel.setText(String.format("Total Cost: %.2f km", totalCost));
            StringBuilder sb = new StringBuilder();
            sb.append("Optimal Route:\n");
            for (int i = 0; i < optimalRoute.size(); i++) {
                sb.append(cityNames[optimalRoute.get(i)]);
                if (i != optimalRoute.size() - 1) sb.append(" → ");
            }
            sb.append("\n\nDetail Perjalanan:\n");

            double[][] cost = {
                {0, 4.1, 7.4, 2.7, INF, INF, INF},
                {4.1, 0, 8.6, INF, 7.9, INF, INF},
                {7.4, 8.6, 0, 5.7, 11.0, 11.0, 9.1},
                {2.7, INF, 5.7, 0, INF, 11.2, INF},
                {INF, 7.9, 11.0, INF, 0, INF, 8.2},
                {INF, INF, 11.0, 11.2, INF, 0, 18.3},
                {INF, INF, 9.1, INF, 8.2, 18.3, 0}
            };

            for (int i = 0; i < optimalRoute.size() - 1; i++) {
                int a = optimalRoute.get(i);
                int b = optimalRoute.get(i + 1);
                sb.append(cityNames[a]).append(" - ")
                        .append(String.format("%.2f", cost[a][b])).append(" km - ")
                        .append(cityNames[b]).append("\n");
            }

            routeArea.setText(sb.toString());
        }
    }

    class GraphPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            setBackground(Color.WHITE);
            drawAllEdges(g2d);
            if (optimalRoute != null) drawOptimalRoute(g2d);
            drawNodes(g2d);
            drawLabels(g2d);
        }

        private void drawAllEdges(Graphics2D g2d) {
            double[][] cost = {
                {0, 4.1, 7.4, 2.7, INF, INF, INF},
                {4.1, 0, 8.6, INF, 7.9, INF, INF},
                {7.4, 8.6, 0, 5.7, 11.0, 11.0, 9.1},
                {2.7, INF, 5.7, 0, INF, 11.2, INF},
                {INF, 7.9, 11.0, INF, 0, INF, 8.2},
                {INF, INF, 11.0, 11.2, INF, 0, 18.3},
                {INF, INF, 9.1, INF, 8.2, 18.3, 0}
            };

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1));

            for (int i = 0; i < cost.length; i++) {
                for (int j = i + 1; j < cost[i].length; j++) {
                    if (cost[i][j] != INF) {
                        Point p1 = nodePositions[i], p2 = nodePositions[j];
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                        int midX = (p1.x + p2.x) / 2, midY = (p1.y + p2.y) / 2;
                        g2d.setColor(Color.GRAY);
                        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                        g2d.drawString(String.format("%.1f", cost[i][j]), midX - 10, midY - 5);
                        g2d.setColor(Color.LIGHT_GRAY);
                    }
                }
            }
        }

        private void drawOptimalRoute(Graphics2D g2d) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));

            for (int i = 0; i < optimalRoute.size() - 1; i++) {
                Point p1 = nodePositions[optimalRoute.get(i)];
                Point p2 = nodePositions[optimalRoute.get(i + 1)];
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                drawArrow(g2d, p1, p2);
            }
        }

        private void drawArrow(Graphics2D g2d, Point from, Point to) {
            double dx = to.x - from.x, dy = to.y - from.y;
            double angle = Math.atan2(dy, dx);
            int len = 10;
            double arrowAngle = Math.PI / 6;
            double dist = Math.sqrt(dx * dx + dy * dy);
            double tipX = from.x + (dist - 20) * Math.cos(angle);
            double tipY = from.y + (dist - 20) * Math.sin(angle);
            double x1 = tipX - len * Math.cos(angle - arrowAngle);
            double y1 = tipY - len * Math.sin(angle - arrowAngle);
            double x2 = tipX - len * Math.cos(angle + arrowAngle);
            double y2 = tipY - len * Math.sin(angle + arrowAngle);
            g2d.drawLine((int) tipX, (int) tipY, (int) x1, (int) y1);
            g2d.drawLine((int) tipX, (int) tipY, (int) x2, (int) y2);
        }

        private void drawNodes(Graphics2D g2d) {
            for (int i = 0; i < nodePositions.length; i++) {
                Point p = nodePositions[i];
                g2d.setColor(Color.RED);
                g2d.fillOval(p.x - 10, p.y - 10, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(p.x - 10, p.y - 10, 20, 20);
            }
        }

        private void drawLabels(Graphics2D g2d) {
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(Color.BLACK);

            String[] shortNames = {
                "Pasar gede", "rendra buah", "raja buah",
                "istana buah\nsrikandi", "kios buah segar",
                "istana buah\nsukoharjo", "toko buah abc\nkaranganyar"
            };

            int[] offsetX = {-40, -35, -25, -45, -35, -45, -50};
            int[] offsetY = {-25, -25, -25, 40, -25, 40, 40};

            for (int i = 0; i < nodePositions.length; i++) {
                Point p = nodePositions[i];
                String[] lines = shortNames[i].split("\\n");
                for (int j = 0; j < lines.length; j++) {
                    g2d.drawString(lines[j], p.x + offsetX[i], p.y + offsetY[i] + (j * 12));
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new TSP_w_GUI().setVisible(true);
            }
        });
    }
}
