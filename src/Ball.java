import java.awt.Color;
import java.util.List;
import java.util.Random;

public class Ball {
    private int x, y;        // 小球的当前位置
    private int radius;      // 小球的半径
    private int dx, dy;      // 小球在 x 和 y 方向上的速度
    private Color color;     // 小球的颜色
    private static final double GRAVITY = 0.1; // 重力加速度常量

    public Ball(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        Random rand = new Random();
        this.dx = rand.nextInt(2); // 初始水平速度随机
        this.dy = rand.nextInt(2); // 初始垂直速度随机
        this.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); // 随机颜色
    }
    public void checkCollision(Ball other) {
        // 计算两个小球之间的距离
        int dx = other.x - this.x;
        int dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // 如果距离小于两个小球的半径和，说明发生了碰撞
        if (distance <= this.radius + other.radius) {
            // 交换速度分量以模拟弹性碰撞
            int tempDx = this.dx;
            int tempDy = this.dy;
            this.dx = other.dx;
            this.dy = other.dy;
            other.dx = tempDx;
            other.dy = tempDy;
        }
    }
    // 更新小球的位置
    public void move(int width, int height, List<Ball> balls) {
        x += dx;
        y += dy;

    // 边界碰撞检测
    if (x < radius || x > width - radius) {
        dx = -dx; // 反转方向
        x = Math.max(radius, Math.min(x, width - radius));
    }
    if (y < radius || y > height - radius) {
        dy = -dy; // 反转方向
        y = Math.max(radius, Math.min(y, height - radius));
    }

    // 重力效果
    dy += 1; // 向下加速

    // 检查与其他小球的碰撞
    for (Ball other : balls) {
        if (other != this) { // 确保不与自己碰撞
            int dx = other.x - this.x;
            int dy = other.y - this.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < this.radius + other.radius) {
                // 将小球分开，避免重叠
                double angle = Math.atan2(dy, dx);
                double overlap = (this.radius + other.radius) - distance;

                this.x -= Math.cos(angle) * overlap / 2;
                this.y -= Math.sin(angle) * overlap / 2;
                other.x += Math.cos(angle) * overlap / 2;
                other.y += Math.sin(angle) * overlap / 2;

                // 交换速度
                int tempDx = this.dx;
                int tempDy = this.dy;
                this.dx = other.dx;
                this.dy = other.dy;
                other.dx = tempDx;
                other.dy = tempDy;
            }
        }
    }
}

    // 获取小球的 x 坐标
    public int getX() {
        return x;
    }

    // 获取小球的 y 坐标
    public int getY() {
        return y;
    }

    // 获取小球的半径
    public int getRadius() {
        return radius;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }
    // 获取小球的颜色
    public Color getColor() {
        return color;
    }
}
