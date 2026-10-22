import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Custom canvas that paints:
 *  - the circular table,
 *  - player name badges evenly spaced around the circle,
 *  - the bottle at its current rotation angle.
 */
public class GamePanel extends JPanel {

    private List<String> players = List.of();
    private double bottleAngle = 0.0; // radians, 0 points to the right
    
    // Winner highlight animation
    private int winnerIndex = -1;
    private float highlightProgress = 0f;
    private Timer highlightTimer;

    public GamePanel() {
        setBackground(new Color(240, 235, 225));
        setPreferredSize(new Dimension(600, 600));
        setFocusable(true);
    }

    /** Called by GameFrame when the player list changes. */
    public void setPlayers(List<String> players) {
        this.players = players;
        repaint();
    }

    /** Called by SpinEngine each animation frame. */
    public void setBottleAngle(double angle) {
        this.bottleAngle = angle;
        repaint();
    }

    /** Current bottle angle (radians). */
    public double getBottleAngle() {
        return bottleAngle;
    }

    /** Number of players currently displayed. */
    public int getPlayerCount() {
        return players.size();
    }

    /** Returns the player name at the given index, or empty string if out of bounds. */
    public String getPlayerName(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return "";
    }

    /** Triggers a winner highlight animation for the given player index. */
    public void highlightWinner(int index) {
        this.winnerIndex = index;
        this.highlightProgress = 0f;
        
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.stop();
        }
        
        highlightTimer = new Timer(16, e -> {
            highlightProgress += 0.02f; // ~50 FPS, completes in ~2 seconds
            if (highlightProgress >= 1.0f) {
                highlightProgress = 1.0f;
                highlightTimer.stop();
                winnerIndex = -1;
            }
            repaint();
        });
        highlightTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2 + 30;  // shift table down by 30px to avoid title overlap
        int radius = Math.min(w, h) / 3;

        // 1️⃣ Draw table surface (wood-like)
        drawTableSurface(g2, cx, cy, radius);

        // 2️⃣ Draw center decoration
        drawCenterDecoration(g2, cx, cy, radius);

        // 3️⃣ Draw player badges
        drawPlayerBadges(g2, cx, cy, radius);

        // 4️⃣ Draw bottle (rotated)
        drawBottle(g2, cx, cy, radius);

        // 5️⃣ Draw title text
        drawTitle(g2, cx);

        g2.dispose();
    }

    private void drawTableSurface(Graphics2D g2, int cx, int cy, int radius) {
        // Table base - dark wood
        GradientPaint woodGradient = new GradientPaint(
            cx - radius, cy - radius, new Color(101, 67, 33),
            cx + radius, cy + radius, new Color(139, 90, 43)
        );
        g2.setPaint(woodGradient);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Wood grain lines
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            int x1 = cx + (int) ((radius - 5) * Math.cos(angle));
            int y1 = cy + (int) ((radius - 5) * Math.sin(angle));
            int x2 = cx + (int) ((radius - 25) * Math.cos(angle));
            int y2 = cy + (int) ((radius - 25) * Math.sin(angle));
            g2.setColor(new Color(80, 50, 20, 80));
            g2.drawLine(x1, y1, x2, y2);
        }

        // Table edge ring
        g2.setColor(new Color(60, 40, 20));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        
        // Inner highlight ring
        g2.setColor(new Color(160, 110, 60, 100));
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(cx - radius + 3, cy - radius + 3, radius * 2 - 6, radius * 2 - 6);
    }

    private void drawCenterDecoration(Graphics2D g2, int cx, int cy, int radius) {
        int centerRadius = radius / 4;
        
        // Center circle - darker
        GradientPaint centerGrad = new GradientPaint(
            cx - centerRadius, cy - centerRadius, new Color(80, 50, 20),
            cx + centerRadius, cy + centerRadius, new Color(120, 80, 40)
        );
        g2.setPaint(centerGrad);
        g2.fillOval(cx - centerRadius, cy - centerRadius, centerRadius * 2, centerRadius * 2);
        
        // Center ring
        g2.setColor(new Color(60, 40, 20));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(cx - centerRadius, cy - centerRadius, centerRadius * 2, centerRadius * 2);
        
        // "SPIN" text in center
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(200, 180, 140));
        FontMetrics fm = g2.getFontMetrics();
        String spinText = "SPIN";
        int tx = cx - fm.stringWidth(spinText) / 2;
        int ty = cy + fm.getAscent() / 2;
        g2.drawString(spinText, tx, ty);
    }

    private void drawTitle(Graphics2D g2, int cx) {
        // Title at top - fixed position from top of panel (not relative to table)
        g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 32));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Truth or Dare";
        int tx = cx - fm.stringWidth(title) / 2;
        int ty = 45;  // fixed distance from top of panel
        
        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawString(title, tx + 2, ty + 2);
        // Main text
        g2.setColor(new Color(101, 67, 33));
        g2.drawString(title, tx, ty);
    }

    private void drawPlayerBadges(Graphics2D g2, int cx, int cy, int radius) {
        int n = players.size();
        if (n == 0) return;

        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + (2 * Math.PI * i / n);
            int badgeRadius = 22;  // slightly larger
            int x = (int) (cx + (radius + 35) * Math.cos(angle)) - badgeRadius;
            int y = (int) (cy + (radius + 35) * Math.sin(angle)) - badgeRadius;

            boolean isWinner = (i == winnerIndex);
            float progress = isWinner ? highlightProgress : 0f;

            if (isWinner && progress > 0) {
                drawWinnerGlow(g2, x + badgeRadius, y + badgeRadius, badgeRadius, progress);
            }

            // Badge background - WHITE FILL with subtle gradient
            GradientPaint badgeGrad = new GradientPaint(
                x, y, Color.WHITE,
                x + badgeRadius * 2, y + badgeRadius * 2, new Color(245, 245, 245)
            );
            g2.setPaint(badgeGrad);
            g2.fillOval(x, y, badgeRadius * 2, badgeRadius * 2);

            // Badge border
            if (isWinner) {
                g2.setColor(new Color(255, 180, 0));
                g2.setStroke(new BasicStroke(3 + progress * 4));
            } else {
                g2.setColor(new Color(180, 180, 180));
                g2.setStroke(new BasicStroke(2));
            }
            g2.drawOval(x, y, badgeRadius * 2, badgeRadius * 2);

            // Player number (small, subtle)
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(new Color(150, 150, 150));
            String num = String.valueOf(i + 1);
            FontMetrics fm = g2.getFontMetrics();
            int nx = x + badgeRadius - fm.stringWidth(num) / 2;
            int ny = y + badgeRadius - fm.getAscent() / 2;
            g2.drawString(num, nx, ny);

            // Name - BOLD, centered
            String name = players.get(i);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            fm = g2.getFontMetrics();
            int nameX = x + badgeRadius - fm.stringWidth(name) / 2;
            int nameY = y + badgeRadius + fm.getAscent() + 5;
            
            if (isWinner) {
                g2.setColor(new Color(180, 120, 0));
            } else {
                g2.setColor(new Color(40, 40, 40));
            }
            g2.drawString(name, nameX, nameY);
        }
    }

    private void drawWinnerGlow(Graphics2D g2, int centerX, int centerY, int badgeRadius, float progress) {
        // Pulsing glow effect - expands and fades
        float pulse = (float) Math.sin(progress * Math.PI * 4) * 0.5f + 0.5f; // 0-1 pulsing
        int glowRadius = badgeRadius + (int) (15 * progress * pulse);
        float alpha = (1.0f - progress) * 0.6f * pulse;
        
        Color glowColor = new Color(255, 215, 0, (int) (alpha * 255));
        g2.setColor(glowColor);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(centerX - glowRadius, centerY - glowRadius, glowRadius * 2, glowRadius * 2);
        
        // Inner glow ring
        int innerRadius = badgeRadius + (int) (8 * progress);
        alpha = (1.0f - progress) * 0.3f;
        glowColor = new Color(255, 235, 100, (int) (alpha * 255));
        g2.setColor(glowColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
        
        // Starburst particles
        for (int i = 0; i < 8; i++) {
            double pAngle = i * Math.PI / 4 + progress * Math.PI * 2;
            int px = centerX + (int) ((badgeRadius + 25 * progress) * Math.cos(pAngle));
            int py = centerY + (int) ((badgeRadius + 25 * progress) * Math.sin(pAngle));
            int pSize = (int) (4 * (1 - progress));
            if (pSize > 0) {
                float pAlpha = (1 - progress) * 0.8f;
                g2.setColor(new Color(255, 220, 50, (int) (pAlpha * 255)));
                g2.fillOval(px - pSize/2, py - pSize/2, pSize, pSize);
            }
        }
    }

    private void drawBottle(Graphics2D g2, int cx, int cy, int tableRadius) {
        // Bottle lies horizontally at EXACT CENTER of table
        // Rotates around its own midpoint (center of bottle)
        // Neck initially points right (0 radians)
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dimensions - WIDER bottle
        int bottleLength = (int) (1.5 * tableRadius);  // 0.75 * diameter = 0.75 * (2*radius) = 1.5 * radius
        int bodyWidth = 26;
        int neckWidth = 14;
        int capHeight = 16;

        // Pivot = bottle center = TABLE CENTER (cx, cy)
        // Bottle extends equally in both directions from center
        g.translate(cx, cy);
        g.rotate(bottleAngle);

        int halfLen = bottleLength / 2;

        // Body - green glass with gradient (centered at origin)
        GradientPaint bodyGrad = new GradientPaint(
            -halfLen, -bodyWidth/2, new Color(20, 100, 20),
            -halfLen, bodyWidth/2, new Color(40, 140, 40)
        );
        g.setPaint(bodyGrad);
        g.fillRoundRect(-halfLen, -bodyWidth / 2, bottleLength - capHeight, bodyWidth, 10, 10);

        // Glass highlight (left side)
        GradientPaint highlightGrad = new GradientPaint(
            -halfLen + 4, -bodyWidth/2 + 4, new Color(100, 220, 100, 120),
            -halfLen + 4, bodyWidth/2 - 4, new Color(100, 220, 100, 30)
        );
        g.setPaint(highlightGrad);
        g.fillRoundRect(-halfLen + 4, -bodyWidth / 2 + 4, bottleLength - capHeight - 8, bodyWidth / 2 - 4, 6, 6);

        // Glass reflection (right side)
        GradientPaint reflectGrad = new GradientPaint(
            halfLen - capHeight - 6, -bodyWidth/2 + 2, new Color(255, 255, 255, 60),
            halfLen - capHeight - 6, bodyWidth/2 - 2, new Color(255, 255, 255, 10)
        );
        g.setPaint(reflectGrad);
        g.fillRoundRect(halfLen - capHeight - 6, -bodyWidth / 2 + 2, 6, bodyWidth - 4, 3, 3);

        // Neck (extends to the right from body)
        GradientPaint neckGrad = new GradientPaint(
            halfLen - capHeight, -neckWidth/2, new Color(30, 110, 30),
            halfLen - capHeight, neckWidth/2, new Color(50, 150, 50)
        );
        g.setPaint(neckGrad);
        g.fillRect(halfLen - capHeight, -neckWidth / 2, capHeight, neckWidth);

        // Neck highlight
        g.setColor(new Color(100, 220, 100, 80));
        g.fillRect(halfLen - capHeight + 1, -neckWidth / 2 + 1, capHeight - 2, 2);

        // Cap - gold/metallic (at far right = neck end)
        GradientPaint capGrad = new GradientPaint(
            halfLen - capHeight, -neckWidth/2 - 2, new Color(220, 180, 20),
            halfLen - capHeight, neckWidth/2 + 2, new Color(180, 130, 10)
        );
        g.setPaint(capGrad);
        g.fillRect(halfLen - capHeight, -neckWidth / 2 - 2, 5, neckWidth + 4);

        // Cap highlight
        g.setColor(new Color(255, 240, 150, 180));
        g.fillRect(halfLen - capHeight + 1, -neckWidth / 2 - 1, 2, neckWidth + 2);

        // Cap top
        g.setColor(new Color(160, 120, 10));
        g.fillRect(halfLen - capHeight, -neckWidth / 2 - 3, 5, 2);

        g.dispose();
    }
}