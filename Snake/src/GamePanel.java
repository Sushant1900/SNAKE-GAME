import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 100;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];

    int bodyParts = 6;
    int applesEaten;
    int appleX, appleY;
    int goldenAppleX, goldenAppleY;
    boolean hasGoldenApple = false;
    long goldenAppleStartTime;

    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            drawApple(g);
            drawSnake(g);
            drawScore(g);
        } else {
            gameOver(g);
        }
    }

    private void drawApple(Graphics g) {
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
        if (hasGoldenApple) {
            g.setColor(Color.yellow);
            g.fillOval(goldenAppleX, goldenAppleY, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void drawSnake(Graphics g) {
        Color snakeColor = getSnakeColor();
        for (int i = 0; i < bodyParts; i++) {
            g.setColor(i == 0 ? Color.green : snakeColor);
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }
    }

    private Color getSnakeColor() {
        switch (applesEaten / 5) {
            case 1 -> { return Color.blue; }
            case 2 -> { return Color.magenta; }
            case 3 -> { return Color.orange; }
            default -> { return new Color(45, 180, 0); }
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("TimesRoman", Font.BOLD, 40));
        String scoreText = "SCORE: " + applesEaten;
        g.drawString(scoreText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(scoreText)) / 2, 50);

        if (hasGoldenApple) {
            g.setColor(Color.yellow);
            g.setFont(new Font("TimesRoman", Font.BOLD, 30));
            String specialAppleText = "Golden Apple Active!";
            g.drawString(specialAppleText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(specialAppleText)) / 2, 90);
        }
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

        if (applesEaten % 7 == 0 && applesEaten > 0) {
            goldenAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            goldenAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            hasGoldenApple = true;
            goldenAppleStartTime = System.currentTimeMillis();
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
        }

        if (hasGoldenApple && x[0] == goldenAppleX && y[0] == goldenAppleY) {
            bodyParts += 3;
            applesEaten += 5;
            hasGoldenApple = false;
        }

        if (hasGoldenApple && System.currentTimeMillis() - goldenAppleStartTime > 5000) {
            hasGoldenApple = false;
        }
    }

    public void checkCollisions() {

        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
                break;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        drawScore(g);

        g.setColor(Color.red);
        g.setFont(new Font("TimesRoman", Font.BOLD, 50));
        String gameOverText = "Game Over";
        g.drawString(gameOverText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(gameOverText)) / 2, SCREEN_HEIGHT / 2);

        String restartText = "Press R to Restart";
        g.setFont(new Font("TimesRoman", Font.BOLD, 30));
        g.drawString(restartText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(restartText)) / 2, SCREEN_HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
                case KeyEvent.VK_R -> { if (!running) restartGame(); }
            }
        }
    }

    private void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        running = true;
        hasGoldenApple = false;
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        startGame();
    }
}