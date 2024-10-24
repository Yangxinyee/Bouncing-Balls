import javax.swing.SwingUtilities;

/**
 * The BallTask class is responsible for moving the ball and updating the BallPanel.
 * It implements the Runnable interface to be used as a task in a separate thread.
 */
public class BallTask implements Runnable {
    private Ball ball;
    private BallPanel panel;
    private volatile boolean running = true;

    public BallTask(Ball ball, BallPanel panel) {
        this.ball = ball;
        this.panel = panel;
    }
    public void stop() {
        running = false;
        System.out.println("Stopping task for ball: " + ball);
    }
    @Override
    public void run() {
        while (running) {
            ball.move(panel.getWidth(), panel.getHeight(), panel.getBalls());
            SwingUtilities.invokeLater(() -> panel.repaint());
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Task stopped for ball: " + ball);
    }
}