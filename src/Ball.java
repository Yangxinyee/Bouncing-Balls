import java.awt.Color;
import java.util.List;
import java.util.Random;

/**
 * The Ball class represents a ball with position, velocity, radius, and color.
 * It can move in a 2D space and detect collisions with other balls.
 */
public class Ball {
    private int x, y;        // Current position of the ball (x, y)
    private int radius;      // Radius of the ball
    private int dx, dy;      // Velocity of the ball in the x and y directions
    private Color color;     // Color of the ball
    private static final double GRAVITY = 1; // Constant for gravitational acceleration

    /**
     * Constructor to initialize the ball with a position and radius.
     * The initial velocity and color are set randomly.
     *
     * @param x      Initial x position of the ball
     * @param y      Initial y position of the ball
     * @param radius Radius of the ball
     */
    public Ball(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        Random rand = new Random();
        this.dx = rand.nextInt(5); // Random initial horizontal velocity
        this.dy = rand.nextInt(5); // Random initial vertical velocity
        this.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); // Random color
    }

    /**
     * Checks for a collision with another ball and handles the collision response.
     *
     * @param other The other ball to check for collision
     */
    public void checkCollision(Ball other) {
        // Calculate the distance between the two balls
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // If the distance is less than the sum of their radii, a collision has occurred
        if (distance <= this.radius + other.radius) {
            // Separate the balls to avoid overlap
            double angle = Math.atan2(dy, dx);
            double overlap = (this.radius + other.radius) - distance;
            this.x -= Math.cos(angle) * overlap / 2;
            this.y -= Math.sin(angle) * overlap / 2;
            other.x += Math.cos(angle) * overlap / 2;
            other.y += Math.sin(angle) * overlap / 2;

            // Swap the velocity components to simulate elastic collision
            int tempDx = this.dx; // Store current velocity
            int tempDy = this.dy;
            this.dx = (int)(other.dx * 0.9); // Dampen velocity
            this.dy = (int)(other.dy * 0.9);
            other.dx = tempDx; // Assign the previous values
            other.dy = tempDy;
        }
    }

    /**
     * Updates the ball's position based on its velocity and checks for collisions.
     *
     * @param width  Width of the containing area
     * @param height Height of the containing area
     * @param balls  List of other balls to check for collisions
     */
    public void move(int width, int height, List<Ball> balls) {
        // Update position based on velocity
        x += dx;
        y += dy;

        // Boundary collision detection
        if (x < radius || x > width - radius) {
            dx = -(int)(dx * 0.9); // Reverse direction with energy loss
            x = Math.max(radius, Math.min(x, width - radius));
        }
        if (y < radius || y > height - radius) {
            dy = -(int)(dy * 0.9); // Reverse direction with energy loss
            y = Math.max(radius, Math.min(y, height - radius));
        }

        // Apply gravity effect
        dy += GRAVITY; // Accelerate downwards

        // Check for collisions with other balls
        for (Ball other : balls) {
            if (other != this) { // Ensure not to collide with itself
                checkCollision(other);
            }
        }
    }

    // Getters and setters

    /**
     * Gets the x-coordinate of the ball.
     *
     * @return x-coordinate of the ball
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the ball.
     *
     * @return y-coordinate of the ball
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the radius of the ball.
     *
     * @return Radius of the ball
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the ball.
     *
     * @param radius New radius for the ball
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Gets the color of the ball.
     *
     * @return Color of the ball
     */
    public Color getColor() {
        return color;
    }
}
