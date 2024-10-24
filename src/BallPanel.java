import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    private Random random;               // Random number generator
    private ExecutorService executorService; // 线程池
    private JLabel activeThreadsLabel; // 当前激活的线程数量标签
    private JLabel ballCountLabel;      // 当前球的数量标签
    private JLabel maxThreadsLabel;     // 最大线程数量标签

    /**
     * Constructor to initialize the BallPanel.
     */
    public BallPanel() {

        balls = new ArrayList<>();        // Initialize the list of balls
        random = new Random();             // Initialize the random number generator
        setBackground(Color.WHITE);        // Set the background color to white\
        int maxThreads = 5; // 最大线程数
        executorService = Executors.newFixedThreadPool(maxThreads); // 创建一个固定大小的线程池
        // 初始化标签
        activeThreadsLabel = new JLabel("Active Threads: 0");
        ballCountLabel = new JLabel("Ball Count: 0");
        maxThreadsLabel = new JLabel("Max Threads: " + ((ThreadPoolExecutor) executorService).getMaximumPoolSize());

        // 设置面板布局
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
                drawBalls();                // 通过 EDT 更新面板
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
            Ball newBall = new Ball(e.getX(), e.getY(), 30); // 创建一个新球
            addBall(newBall);
            ballCountLabel.setText("Ball Count: " + balls.size()); // 更新球的数量
            // executorService.submit(newBall);
        }
        // Handle right-click to remove a random ball
        else if (SwingUtilities.isRightMouseButton(e)) {
            if (!balls.isEmpty()) {
                int index = random.nextInt(balls.size()); // Generate a random index
                balls.remove(index);                   // Remove the ball at the random index
                ballCountLabel.setText("Ball Count: " + balls.size()); // 更新球的数量
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
        Ball ballToRemove = null;
        for (Ball ball : balls) {
            int dx = ball.getX() - x;              // Difference in x coordinates
            int dy = ball.getY() - y;              // Difference in y coordinates
            double distance = Math.sqrt(dx * dx + dy * dy); // Calculate the distance

            // Check if within radius
            if (distance < ball.getRadius()) {
                ballToRemove = ball;                 // Found the ball to remove
                break;
            }
        }
        if (ballToRemove != null) {
            balls.remove(ballToRemove);              // Remove the ball from the list
        }
    }

    // /**
    //  * Adds a new ball at the specified (x, y) coordinates.
    //  *
    //  * @param x The x-coordinate
    //  * @param y The y-coordinate
    //  */
    // public void addBall(int x, int y) {
    //     balls.add(new Ball(x, y, 30));              // Create a ball with radius 30 and add it to the list
    // }

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
        // 更新球的数量
        ballCountLabel.setText("Ball Count: " + balls.size());
        // 提交每个小球的移动任务到线程池
        for (Ball ball : balls) {
            executorService.submit(() -> {
                // 移动小球
                System.out.println("Moving ball at " + ball.getX() + ", " + ball.getY());
                ball.move(getWidth(), getHeight(), balls); // 更新每个小球的位置

                // 确保在事件调度线程中绘制
                SwingUtilities.invokeLater(() -> {
                    checkCollisions(); // 在移动后检查碰撞
                    repaint(); // 重绘面板
                    activeThreadsLabel.setText("Active Threads: " + ((ThreadPoolExecutor) executorService).getActiveCount()); // 更新活跃线程数量
                });
            });
    }
    }
    private void drawBalls() {
        SwingUtilities.invokeLater(this::repaint); // 确保在 EDT 中执行 repaint()
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                 // Call the superclass method
        for (Ball ball : balls) {
            ball.draw(g);
        }
    }
    public List<Ball> getBalls() {
        return balls; // 提供小球列表以供线程使用
    }

    public void addBall(Ball ball) {
        balls.add(ball);
        // 将 BallTask 提交给线程池
        executorService.submit(new BallTask(ball, this));
    }

    public void shutdown() {
        executorService.shutdown(); // 关闭线程池
    }
}