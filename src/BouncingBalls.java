import javax.swing.JFrame;

public class BouncingBalls {
    public static void main(String[] args) {
        // 创建一个窗口
        JFrame frame = new JFrame("Bouncing Balls");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(800, 600); // 设置窗口大小
        BallPanel ballPanel = new BallPanel(); // 创建画布
        frame.add(ballPanel);                  // 将画布添加到窗口中
        frame.setVisible(true);  // 显示窗口
    }
}
