package ui;

import models.GradeRecord;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GradeDistributionChartPanel extends JPanel {
    private final Map<String, Integer> gradeCounts = new LinkedHashMap<>();
    private int totalRecords = 0;

    public GradeDistributionChartPanel() {
        gradeCounts.put("A+", 0);
        gradeCounts.put("A", 0);
        gradeCounts.put("B", 0);
        gradeCounts.put("C", 0);
        gradeCounts.put("F", 0);
        setPreferredSize(new Dimension(500, 300));
        setBackground(Color.WHITE);
    }

    public void updateData(List<GradeRecord> records) {
        gradeCounts.put("A+", 0);
        gradeCounts.put("A", 0);
        gradeCounts.put("B", 0);
        gradeCounts.put("C", 0);
        gradeCounts.put("F", 0);
        totalRecords = 0;

        if (records != null) {
            for (GradeRecord r : records) {
                String g = r.getGrade();
                if (gradeCounts.containsKey(g)) {
                    gradeCounts.put(g, gradeCounts.get(g) + 1);
                    totalRecords++;
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        int bottomPadding = 50;

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(40, 50, 70));
        g2.drawString("Relative Grade Distribution Chart", padding, 25);

        int chartWidth = width - (2 * padding);
        int chartHeight = height - padding - bottomPadding;

        // Draw Axes
        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(padding, height - bottomPadding, width - padding, height - bottomPadding);

        int numBars = gradeCounts.size();
        if (numBars == 0) return;

        int barGap = 20;
        int barWidth = (chartWidth - (barGap * (numBars + 1))) / numBars;

        int maxVal = 1;
        for (int count : gradeCounts.values()) {
            if (count > maxVal) maxVal = count;
        }

        // Palette for grades
        Map<String, Color> colors = new HashMap<>();
        colors.put("A+", new Color(76, 175, 80));   // Vibrant Green
        colors.put("A", new Color(33, 150, 243));   // Bright Blue
        colors.put("B", new Color(0, 188, 212));    // Cyan
        colors.put("C", new Color(255, 152, 0));   // Orange
        colors.put("F", new Color(244, 67, 54));    // Red

        int x = padding + barGap;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
            String grade = entry.getKey();
            int count = entry.getValue();

            int barHeight = (int) (((double) count / maxVal) * (chartHeight - 30));
            int y = height - bottomPadding - barHeight;

            // Bar background shadow
            g2.setColor(colors.getOrDefault(grade, Color.GRAY));
            g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Bar border
            g2.setColor(g2.getColor().darker());
            g2.drawRoundRect(x, y, barWidth, barHeight, 8, 8);

            // Label count and percentage above bar
            g2.setColor(new Color(50, 50, 50));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String countText = String.valueOf(count);
            double pct = totalRecords > 0 ? ((double) count / totalRecords) * 100.0 : 0.0;
            String pctText = String.format("%.0f%%", pct);

            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(countText, x + (barWidth - fm.stringWidth(countText)) / 2, Math.max(y - 15, padding + 15));
            
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            g2.drawString(pctText, x + (barWidth - fm.stringWidth(pctText)) / 2, Math.max(y - 3, padding + 27));

            // Grade Label below axis
            g2.setColor(new Color(30, 30, 30));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            fm = g2.getFontMetrics();
            g2.drawString(grade, x + (barWidth - fm.stringWidth(grade)) / 2, height - bottomPadding + 22);

            x += barWidth + barGap;
        }
    }
}
