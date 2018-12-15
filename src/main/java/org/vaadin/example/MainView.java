package org.vaadin.example;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.ui.Transport;
import org.vaadin.marcus.shortcut.Shortcut;
import org.vaadin.pekkam.Canvas;

import java.util.Arrays;

// The most efficient transport system
@Push(transport = Transport.WEBSOCKET)
@Route
public class MainView extends VerticalLayout {

    private static final int PAUSE_TIME_MS = 500;

    private static final long serialVersionUID = -152735180021558969L;

    // Tile size in pixels
    protected static final int TILE_SIZE = 30;

    // Playfield width in tiles
    private static final int PLAYFIELD_W = 10;

    // Playfield height in tiles
    private static final int PLAYFIELD_H = 20;

    // Playfield background color
    private static final String PLAYFIELD_COLOR = "#000";

    private Canvas canvas;
    protected boolean running;
    protected Game game;

    private Span scoreLabel;

    public MainView() {
        add(new About());

        // Button for moving left
        final Button leftBtn = new Button(VaadinIcon.ARROW_LEFT.create());
        leftBtn.addClickListener(e -> {
            game.moveLeft();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_LEFT, leftBtn::click);

        // Button for moving right
        final Button rightBtn = new Button(VaadinIcon.ARROW_RIGHT.create());
        rightBtn.addClickListener(e -> {
            game.moveRight();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_RIGHT, rightBtn::click);

        // Button for rotating clockwise
        final Button rotateCWBtn = new Button(VaadinIcon.ROTATE_RIGHT.create());
        rotateCWBtn.addClickListener(e -> {
            game.rotateCW();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_DOWN, rotateCWBtn::click);

        // Button for rotating counter clockwise
        final Button rotateCCWBtn = new Button(VaadinIcon.ARROW_UP.create());
        rotateCCWBtn.addClickListener(e -> {
            game.rotateCCW();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_UP, rotateCCWBtn::click);

        // Button for dropping the piece
        final Button dropBtn = new Button(VaadinIcon.ARROW_DOWN.create());
        dropBtn.addClickListener(e -> {
            game.drop();
            drawGameState();
        });
        Shortcut.add(this, Key.of("d"), dropBtn::click);

        // Button for restarting the game
        final Button restartBtn = new Button(VaadinIcon.PLAY.create());
        restartBtn.addClickListener(e -> {
            running = !running;
            if (running) {
                game = new Game(10, 20);
                startGameThread();
                restartBtn.setIcon(VaadinIcon.STOP.create());
                dropBtn.focus();
            } else {
                restartBtn.setIcon(VaadinIcon.PLAY.create());
                gameOver();
            }
        });

        // Canvas for the game
        canvas = new Canvas(TILE_SIZE * PLAYFIELD_W, TILE_SIZE * PLAYFIELD_H);

        // Label for score
        scoreLabel = new Span("");
        add(scoreLabel);
        add(canvas);

        styleControlButtons(restartBtn, leftBtn, rightBtn, rotateCCWBtn, rotateCWBtn, dropBtn);
        rotateCWBtn.addClassName("dominant-control");
        restartBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        final BorderLayout cursorsPanel = new BorderLayout();
        cursorsPanel.addNorth(rotateCCWBtn);
        cursorsPanel.addSouth(dropBtn);
        cursorsPanel.addEast(rightBtn);
        cursorsPanel.addWest(leftBtn);

        final HorizontalLayout controlsPanel = new HorizontalLayout(cursorsPanel, restartBtn, rotateCWBtn);
        controlsPanel.setAlignItems(Alignment.CENTER);
        controlsPanel.setSpacing(true);
        controlsPanel.setJustifyContentMode(JustifyContentMode.BETWEEN);
        controlsPanel.setWidth(canvas.getWidth());

        add(controlsPanel);
        setAlignItems(Alignment.CENTER);
    }

    private void styleControlButtons(Button... buttons) {
        Arrays.stream(buttons).forEach(button -> button.addClassName("control-button"));
    }

    /**
     * Start the game thread that updates the game periodically.
     *
     */
    protected synchronized void startGameThread() {
        UI ui = UI.getCurrent();
        Thread t = new Thread() {
            public void run() {

                // Continue until stopped or game is over
                while (running && !game.isOver()) {

                    // Draw the state
                    ui.access(() -> {
                        drawGameState();
                    }
                    );

                    // Pause for a while
                    try {
                        sleep(PAUSE_TIME_MS);
                    } catch (InterruptedException igmored) {
                    }

                    // Step the game forward and update score
                    game.step();
                    ui.access(() -> {
                        updateScore();
                    });

                }

                // Notify user that game is over
                ui.access(() -> {
                    gameOver();
                });
            }
        };
        t.start();

    }

    /**
     * Update the score display.
     *
     */
    protected synchronized void updateScore() {
        scoreLabel.setText("Score: " + game.getScore());
    }

    /**
     * Quit the game.
     *
     */
    protected synchronized void gameOver() {
        running = false;
        Notification.show("Game Over! Your score: " + game.getScore());
    }

    /**
     * Draw the current game state.
     *
     */
    protected synchronized void drawGameState() {

        // Reset and clear canvas
        canvas.getContext().clearRect(0, 0, TILE_SIZE * PLAYFIELD_W, TILE_SIZE * PLAYFIELD_H);
        canvas.getContext().setFillStyle(PLAYFIELD_COLOR);

        canvas.getContext().fillRect(0, 0, game.getWidth() * TILE_SIZE + 2, game.getHeight()
                * TILE_SIZE + 2);

        // Draw the tetrominoes
        Grid state = game.getCurrentState();
        for (int x = 0; x < state.getWidth(); x++) {
            for (int y = 0; y < state.getHeight(); y++) {

                int tile = state.get(x, y);
                if (tile > 0) {

                    String color = Tetromino.get(tile).getColor();
                    canvas.getContext().setFillStyle(color);
                    canvas.getContext().fillRect(x * TILE_SIZE + 1, y * TILE_SIZE + 1,
                            TILE_SIZE - 2, TILE_SIZE - 2);
                }

            }
        }
    }
}
