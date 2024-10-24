import javax.swing.JFrame;

/**
 * The BouncingBalls class is the entry point for the Bouncing Balls application.
 * It initializes the main application window and adds the BallPanel where the balls will be rendered.
 */
public class BouncingBalls {
    public static void main(String[] args) {
        // Create a new JFrame window
        JFrame frame = new JFrame("Bouncing Balls");

        // Set the default close operation to exit the application when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the size of the window
        frame.setSize(800, 600);

        // Create an instance of BallPanel to manage the ball rendering and interactions
        BallPanel ballPanel = new BallPanel();

        // Add the BallPanel to the JFrame
        frame.add(ballPanel);

        // Set the frame's visibility to true to display the window
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                ballPanel.shutdown();
                System.exit(0);
            }
        });
    }
}