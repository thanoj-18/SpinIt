import javax.swing.*;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Handles the spin animation:
 * - chooses a random target player,
 * - computes total rotation (5‑9 revolutions + delta),
 * - runs a Swing Timer for ~5 s with quintic ease‑out,
 * - calls back with the winner name when finished.
 */
public class SpinEngine {

    private final GamePanel panel;
    private final Consumer<String> onComplete;
    private final Random random = new Random();

    private Timer timer;
    private long startNanos;
    private double startAngle;
    private double totalRotation;
    private final double DURATION_NS = 5_000_000_000L; // 5 seconds
    private boolean spinning = false;

    public SpinEngine(GamePanel panel, Consumer<String> onComplete) {
        this.panel = panel;
        this.onComplete = onComplete;
    }

    /** Returns true if a spin animation is currently running. */
    public boolean isSpinning() {
        return spinning;
    }

    /** Starts a new spin; does nothing if already spinning or <2 players. */
    public void startSpin() {
        if (spinning) return;
        int playerCount = panel.getPlayerCount();
        if (playerCount < 2) return; // GameFrame should disable button anyway

        spinning = true;
        startAngle = panel.getBottleAngle();

        // Choose a random target player index
        int targetIndex = random.nextInt(playerCount);
        double targetAngle = computeTargetAngle(targetIndex, playerCount);

        // Total rotation = 5‑9 full revolutions + forward delta to target
        int fullRevs = 5 + random.nextInt(5); // 5‑9 inclusive
        double delta = normalizeAngle(targetAngle - startAngle);
        if (delta < 0) delta += 2 * Math.PI;
        totalRotation = fullRevs * 2 * Math.PI + delta;

        startNanos = System.nanoTime();
        timer = new Timer(16, e -> animationStep());
        timer.start();
    }

    private void animationStep() {
        long elapsed = System.nanoTime() - startNanos;
        double progress = Math.min(1.0, (double) elapsed / DURATION_NS);
        // quintic ease‑out
        double eased = 1 - Math.pow(1 - progress, 5);
        double currentAngle = startAngle + totalRotation * eased;
        panel.setBottleAngle(currentAngle);

        if (progress >= 1.0) {
            timer.stop();
            spinning = false;
            // Determine winner from final angle (should match targetIndex)
            int winnerIndex = angleToPlayerIndex(panel.getBottleAngle(), panel.getPlayerCount());
            String winner = panel.getPlayerName(winnerIndex);
            onComplete.accept(winner);
        }
    }

    /** Normalizes angle to [-π, π]. */
    private static double normalizeAngle(double a) {
        while (a <= -Math.PI) a += 2 * Math.PI;
        while (a > Math.PI) a -= 2 * Math.PI;
        return a;
    }

    /** Compute the absolute angle for a given player index. */
    private double computeTargetAngle(int index, int n) {
        // angle_i = -π/2 + (2π * i / n)
        return -Math.PI / 2 + (2 * Math.PI * index / n);
    }

    /** Convert a bottle angle back to the nearest player index. */
    private int angleToPlayerIndex(double angle, int n) {
        // Normalize to [0, 2π)
        double a = angle % (2 * Math.PI);
        if (a < 0) a += 2 * Math.PI;
        // Player zero is at -π/2 (i.e., 3π/2 in positive range)
        // So shift by +π/2
        double shifted = (a + Math.PI / 2) % (2 * Math.PI);
        int idx = (int) Math.round(shifted / (2 * Math.PI) * n) % n;
        return idx;
    }
}