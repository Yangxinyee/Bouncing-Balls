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
import java.util.concurrent.atomic.AtomicInteger;
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
    private JLabel processingTimeLabel;       // 显示处理时间的标签
    private long totalProcessingTime;         // 累加的处理时间
    private Map<Ball, BallTask> ballTasks = new HashMap<>();
    private int currentBallIndex;
    private int initialBallCount;
    private long lastFrameTime = System.currentTimeMillis();  // 上一次重绘的时间
    private AtomicInteger frameCount = new AtomicInteger(0);  // 帧计数器
    private JLabel fpsLabel;  // 显示FPS的标签

    /**
     * Constructor to initialize the BallPanel.
     */
    public BallPanel() {
        balls = new ArrayList<>();        // Initialize the list of balls
        setBackground(Color.WHITE);        // Set the background color to white

        initialBallCount = 20;
        int maxThreads = 40;

        this.executorService = Executors.newFixedThreadPool(maxThreads);
        this.ballTasks = new HashMap<>();
        currentBallIndex = 0;
        isMouseInside = true;                // Initially, the mouse is inside the panel
        // 初始化生成一定数量的小球
        initializeBalls(initialBallCount);
        activeThreadsLabel = new JLabel("Active Threads: 0");
        ballCountLabel = new JLabel("Ball Count: 0");
        maxThreadsLabel = new JLabel("Max Threads: " + ((ThreadPoolExecutor) executorService).getMaximumPoolSize());
        // 初始化显示处理时间的标签
        processingTimeLabel = new JLabel("Processing Time: 0 ms");
        fpsLabel = new JLabel("TPS: 0");  // 初始化FPS标签
        setLayout(new FlowLayout());
        // add(activeThreadsLabel);
        add(ballCountLabel);
        add(maxThreadsLabel);
        add(fpsLabel);  // 将FPS标签添加到面板
        // add(processingTimeLabel);  // 将标签添加到面板中
        // Timer for updating the ball positions and repainting the panel
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isMouseInside) {
                    scheduleBallTasks();
                    // moveBalls();                // Update the positions of the balls
                    // repaint();                  // Repaint the panel
                    drawBalls();            // Draw the balls
                    SwingUtilities.invokeLater(() -> {
                        ballCountLabel.setText("Balls: " + balls.size());  // 更新标签内容
                    });
                    // SwingUtilities.invokeLater(() -> {
                    //     processingTimeLabel.setText("Processing Time: " +  totalProcessingTime / balls.size() + " ms");  // 更新标签内容
                    // });
                }
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
    }
    /**
     * 计算并显示当前FPS
     */
    private void calculateAndDisplayFPS() {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        frameCount.incrementAndGet();  // 增加帧数

        // 如果超过1秒（1000毫秒），计算一次FPS
        if (currentTime - lastFrameTime >= 1000) {
            int fps = frameCount.get();  // 在这一秒内更新的帧数
            fpsLabel.setText("TPS: " + fps);  // 更新FPS显示

            // 重置计数器和时间
            frameCount.set(0);
            lastFrameTime = currentTime;
        }
    }
    /**
     * 初始化一定数量的小球。
     */
    private void initializeBalls(int count) {
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.sqrt(Math.random()) * getWidth());  // 随机生成x坐标
            int y = (int) (Math.sqrt(Math.random()) * getHeight()); // 随机生成y坐标
            int radius = 20 + (int) (Math.random() * 30); // 随机生成半径
            // int dx = (int) (Math.random() * 4) + 1;      // 随机生成水平速度
            // int dy = (int) (Math.random() * 4) + 1;      // 随机生成垂直速度
            // Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());  // 随机生成颜色

            // 创建新小球并添加到列表中
            Ball ball = new Ball(x, y, radius);
            addBall(ball);
        }
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
            // 如果当前索引超过小球数量，重置索引
            if (currentBallIndex >= balls.size()) {
                currentBallIndex = 0;
            }
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
    private void scheduleBallTasks() {
        totalProcessingTime = 0;  // 重置总处理时间
        for (Ball ball : balls) {
            executorService.submit(() -> {
                ball.move(getWidth(), getHeight(), balls); // 移动小球
                // 模拟耗时操作，例如睡眠一段时间
                try {
                    Thread.sleep(10);  // 每个小球任务执行时等待50毫秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // 恢复中断状态
                }
                calculateAndDisplayFPS();  // 计算并显示FPS
                // 使用 SwingUtilities.invokeLater 来确保更新 UI 在事件调度线程上运行
                SwingUtilities.invokeLater(this::repaint); // 重绘小球
                // activeThreadsLabel.setText("Active Threads: " + ((ThreadPoolExecutor) executorService).getActiveCount()); // 更新活跃线程数量
                });
            }
        // System.out.println("balls.size() = " + balls.size());
        // if (!balls.isEmpty()) {
        //     System.out.println("Start scheduling ball tasks");
        //     // 每次处理一个小球，并交给线程池
        //     Ball ball = balls.get(currentBallIndex);
        //     executorService.submit(() -> {
        //         // System.out.println("Moving ball at " + ball.getX() + ", " + ball.getY());
        //         ball.move(getWidth(), getHeight(), balls);
        //         SwingUtilities.invokeLater(() -> {
        //             checkCollisions();
        //             repaint();
        //             activeThreadsLabel.setText("Active Threads: " + ((ThreadPoolExecutor) executorService).getActiveCount()); // 更新活跃线程数量
        //         });
        //     });
        //     // 更新索引，轮询处理下一个小球
        //     currentBallIndex = (currentBallIndex + 1) % balls.size();
        //     System.out.println("Current ball index: " + currentBallIndex);
        // }
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
        // 计算和显示当前的FPS
        calculateAndDisplayFPS();
    }
    public List<Ball> getBalls() {
        return balls;
    }

    public void addBall(Ball ball) {
        balls.add(ball);
        // ballCountLabel.setText("Ball Count: " + balls.size());
        BallTask task = new BallTask(ball, this);
        ballTasks.put(ball, task);
        // executorService.submit(task);
    }

    public void shutdown() {
        for (BallTask task : ballTasks.values()) {
            task.stop();
        }
        executorService.shutdown();
    }
}