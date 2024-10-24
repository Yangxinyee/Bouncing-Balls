import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * The BallPanel class represents a drawing area where balls can be created,
 * moved, resized, and detected for collisions. It handles mouse events and
 * updates the panel at regular intervals.
 */
public class BallPanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener {
    private List<Ball> balls;           // List of balls
    private Timer timer;                // Timer for updating and repainting
    private Ball resizingBall = null;    // The ball currently being resized
    private Timer resizeTimer;           // Timer for periodic resizing
    private boolean isMouseInside;       // Flag indicating if the mouse is within the panel
    private boolean increaseSize = false; // Flag indicating if the ball is increasing in size
    private boolean decreaseSize = false; // Flag indicating if the ball is decreasing in size
    private ExecutorService executorService;
    private JLabel activeThreadsLabel;
    private JLabel ballCountLabel;
    private JLabel maxThreadsLabel;
    private Map<Ball, BallTask> ballTasks = new HashMap<>();

    /**
     * Constructor to initialize the BallPanel.
     */
    public BallPanel() {
        balls = new ArrayList<>();        // Initialize the list of balls
        setBackground(Color.WHITE);        // Set the background color to white\
        int maxThreads = 10;
        this.executorService = Executors.newFixedThreadPool(maxThreads);
        this.ballTasks = new HashMap<>();

        activeThreadsLabel = new JLabel("Active Threads: 0");
        ballCountLabel = new JLabel("Ball Count: 0");
        maxThreadsLabel = new JLabel("Max Threads: " + ((ThreadPoolExecutor) executorService).getMaximumPoolSize());

        setLayout(new FlowLayout());
        add(activeThreadsLabel);
        add(ballCountLabel);
        add(maxThreadsLabel);
        // Timer for updating the ball positions and repainting the panel
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBalls();                // Update the positions of the balls
                // repaint();                  // Repaint the panel
                drawBalls();            // Draw the balls
            }
        });
        timer.start();                      // Start the timer

        // Timer for resizing the selected ball
        resizeTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resizingBall != null) {
                    // Increase or decrease the radius of the resizing ball
                    if (increaseSize) {
                        resizingBall.setRadius(resizingBall.getRadius() + 3); // Increase radius
                    } else if (decreaseSize) {
                        resizingBall.setRadius(Math.max(5, resizingBall.getRadius() - 3)); // Decrease radius, minimum 5
                    }
                    repaint();              // Update display
                }
            }
        });

        // Add mouse listeners for interaction
        addMouseListener(this);              // Register mouse listener
        addMouseMotionListener(this);        // Register mouse motion listener
        isMouseInside = true;                // Initially, the mouse is inside the panel
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMouseInside) {                  // Only update positions if the mouse is within the panel
            moveBalls();                     // Update the ball positions
        }
        // repaint();                            // Repaint the panel
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        isMouseInside = true;                 // Mouse is within the panel
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isMouseInside = false;                // Mouse has exited the panel
        timer.stop();                         // Stop the timer
        resizeTimer.stop();                   // Stop the resize timer
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isMouseInside = true;                 // Mouse has entered the panel
        timer.start();                        // Restart the timer
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Handle left-click to add a new ball
        if (SwingUtilities.isLeftMouseButton(e)) {
            Ball newBall = new Ball(e.getX(), e.getY(), 30); // Create a new ball at the click position
            addBall(newBall);
            // ballCountLabel.setText("Ball Count: " + balls.size());
            // executorService.submit(newBall);
        }
        // Handle right-click to remove a random ball
        else if (SwingUtilities.isRightMouseButton(e)) {
            if (!balls.isEmpty()) {
                removeBall(e.getX(), e.getY()); // Remove the ball at the click position
                // int index = random.nextInt(balls.size()); // Generate a random index
                // balls.remove(index);                   // Remove the ball at the random index
                // ballCountLabel.setText("Ball Count: " + balls.size());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        resizingBall = getBallAt(e.getX(), e.getY()); // Get the ball at the click position
        if (resizingBall != null) {
            // Start resizing based on mouse button pressed
            if (SwingUtilities.isLeftMouseButton(e)) {
                increaseSize = true;                  // Start increasing size
                resizeTimer.start();                   // Start the resize timer
            } else if (SwingUtilities.isRightMouseButton(e)) {
                decreaseSize = true;                  // Start decreasing size
                resizeTimer.start();                   // Start the resize timer
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Stop resizing
        increaseSize = false;
        decreaseSize = false;
        resizeTimer.stop();                       // Stop the resize timer
        resizingBall = null;                      // Reset the resizing ball
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Add logic for dragging if necessary
    }

    /**
     * Gets the ball at the specified (x, y) coordinates.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return The ball at the specified coordinates, or null if none exists
     */
    private Ball getBallAt(int x, int y) {
        for (Ball ball : balls) {
            int dx = ball.getX() - x;              // Difference in x coordinates
            int dy = ball.getY() - y;              // Difference in y coordinates
            double distance = Math.sqrt(dx * dx + dy * dy); // Calculate the distance
            if (distance < ball.getRadius()) {      // Check if within radius
                return ball;                        // Return the found ball
            }
        }
        return null;                                // No ball found at the coordinates
    }

    /**
     * Removes a ball at the specified (x, y) coordinates if it exists.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public void removeBall(int x, int y) {
        if (!balls.isEmpty()) {
            int index = (int) (Math.random() * balls.size());
            Ball ballToRemove = balls.get(index);
            System.out.println("Attempting to remove ball: " + ballToRemove);

        BallTask task = ballTasks.get(ballToRemove);
        if (task != null) {
            task.stop();
            ballTasks.remove(ballToRemove);
        } else {
            System.out.println("No task found for the ball to remove: " + ballToRemove);
        }
        if (ballToRemove != null) {
            balls.remove(ballToRemove);              // Remove the ball from the list
        }
        ballTasks.remove(ballToRemove);
        ballCountLabel.setText("Ball Count: " + balls.size());
    }
}

    /**
     * Checks for collisions between all balls in the panel.
     */
    private void checkCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball1 = balls.get(i);
                Ball ball2 = balls.get(j);
                ball1.checkCollision(ball2);        // Check for collision between the two balls
            }
        }
    }

    /**
     * Moves all the balls in the panel.
     */
    private void moveBalls() {
        ballCountLabel.setText("Ball Count: " + balls.size());

        for (Ball ball : balls) {
            executorService.submit(() -> {
                // System.out.println("Moving ball at " + ball.getX() + ", " + ball.getY());
                ball.move(getWidth(), getHeight(), balls);
                SwingUtilities.invokeLater(() -> {
                    checkCollisions();
                    repaint();
                    activeThreadsLabel.setText("Active Threads: " + ((ThreadPoolExecutor) executorService).getActiveCount()); // 更新活跃线程数量
                });
            });
    }
    }
    private void drawBalls() {
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                 // Call the superclass method
        for (Ball ball : balls) {
            ball.draw(g);
        }
    }
    public List<Ball> getBalls() {
        return balls;
    }

    public void addBall(Ball ball) {
        balls.add(ball);
        ballCountLabel.setText("Ball Count: " + balls.size());
        BallTask task = new BallTask(ball, this);
        ballTasks.put(ball, task);
        executorService.submit(task);
    }

    public void shutdown() {
        for (BallTask task : ballTasks.values()) {
            task.stop();
        }
        executorService.shutdown();
    }
}