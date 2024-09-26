import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.Random;

public class BallPanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener {
    private List<Ball> balls;  // 小球列表
    private Timer timer;       // 定时器用于更新和重绘
    private Ball resizingBall = null; // 当前正在调整大小的小球
    private Timer resizeTimer;        // 定时器用于定时调整大小
    private boolean isMouseInside; // 标志变量，表示鼠标是否在窗口内
    private boolean increaseSize = false;  // 标志变量，表示是否增大
    private boolean decreaseSize = false;  // 标志变量，表示是否减小
    private Random random; // 随机数生成器

    public BallPanel() {
        balls = new ArrayList<>(); // 初始化小球列表
        random = new Random(); // 初始化随机数生成器
        setBackground(Color.WHITE); // 设置背景为白色

        // 创建定时器，每隔 10 毫秒更新一次面板
        timer = new Timer(8, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBalls();  // 更新小球位置
                repaint();    // 重绘面板
            }
        });
        timer.start(); // 启动定时器

        // 用于定时调整大小的定时器
        resizeTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resizingBall != null) {
                    if (increaseSize) {
                        resizingBall.setRadius(resizingBall.getRadius() + 3); // 增加半径
                    } else if (decreaseSize) {
                        resizingBall.setRadius(Math.max(5, resizingBall.getRadius() - 3)); // 减少半径，最小为5
                    }
                    repaint(); // 更新显示
                }
            }
        });

        // 添加鼠标监听器，点击时创建新小球
        addMouseListener(this); // 注册 MouseListener
        addMouseMotionListener(this); // 添加鼠标移动监听器
        isMouseInside = true; // 初始状态为鼠标在窗口内
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMouseInside) { // 仅当鼠标在窗口内时更新小球位置
            moveBalls(); // 更新小球的位置
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        isMouseInside = true; // 鼠标移动到窗口内
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isMouseInside = false; // 鼠标离开窗口
        timer.stop(); // 停止定时器
        resizeTimer.stop(); // 停止调整大小的定时器
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isMouseInside = true; // 鼠标进入窗口
        timer.start(); // 重启定时器
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Ball newBall = new Ball(e.getX(), e.getY(), 30);
            balls.add(newBall); // 将新小球添加到列表
        } else if (SwingUtilities.isRightMouseButton(e)) {
            // 随机删除一个小球
            if (!balls.isEmpty()) {
                int index = random.nextInt(balls.size()); // 生成一个随机索引
                balls.remove(index); // 删除随机索引对应的小球
                }
            }
        }

    @Override
    public void mousePressed(MouseEvent e) {
        resizingBall = getBallAt(e.getX(), e.getY());
            if (resizingBall != null) {
                if (SwingUtilities.isLeftMouseButton(e)) {  // 长按左键调整大小
                    increaseSize = true;
                    resizeTimer.start(); // 启动调整大小的定时器
                } else if (SwingUtilities.isRightMouseButton(e)) {  // 长按右键缩小大小
                    decreaseSize = true;
                    resizeTimer.start(); // 启动调整大小的定时器
                }
            }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        increaseSize = false; // 停止增大
        decreaseSize = false; // 停止缩小
        resizeTimer.stop(); // 停止调整大小
        resizingBall = null; // 重置当前调整的小球
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        // 可以根据需要添加拖动逻辑
    }
    // 获取点击位置的小球
    private Ball getBallAt(int x, int y) {
        for (Ball ball : balls) {
            int dx = ball.getX() - x;
            int dy = ball.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < ball.getRadius()) {
                return ball;
            }
        }
        return null;
    }

    // 删除指定位置的小球
    public void removeBall(int x, int y) {
        Ball ballToRemove = null;
        for (Ball ball : balls) {
            int dx = ball.getX() - x;
            int dy = ball.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < ball.getRadius()) {
                ballToRemove = ball;
                break;
            }
        }
        if (ballToRemove != null) {
            balls.remove(ballToRemove);
        }
    }

    // 添加新小球
    public void addBall(int x, int y) {
        balls.add(new Ball(x, y, 30)); // 创建一个半径为30的小球并添加到列表中
    }

    private void checkCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball1 = balls.get(i);
                Ball ball2 = balls.get(j);
                ball1.checkCollision(ball2);
            }
        }
    }

    // 更新所有小球的位置
    private void moveBalls() {
        for (Ball ball : balls) {
            ball.move(getWidth(), getHeight(), balls); // 更新每个小球的位置
        }
        checkCollisions(); // 每次移动后检查碰撞
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Ball ball : balls) {
            g.setColor(ball.getColor());
            g.fillOval(ball.getX() - ball.getRadius(), ball.getY() - ball.getRadius(),
                       ball.getRadius() * 2, ball.getRadius() * 2);
        }
    }
}
