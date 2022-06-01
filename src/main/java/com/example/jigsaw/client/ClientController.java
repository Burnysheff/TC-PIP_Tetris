package com.example.jigsaw.client;

import com.example.jigsaw.Draggable;
import com.example.jigsaw.Groups;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * A controller for the elements
 * */
public class ClientController {
    private Stage stage;

    private String textTime;

    private final List<Long> thisData = new ArrayList<>();

    private boolean single = true;

    public boolean rivalReady = false;

    private ClientSocket socket;

    private String name;

    private String serverName;

    private int moves = 0;

    public boolean[][] grid = new boolean[9][9];

    private final Draggable draggable = new Draggable();

    public long timerSeconds = 0;

    private long timerMinutes = 0;

    @FXML
    public Group group = new Group();

    @FXML
    private Label error = new Label();

    @FXML
    private Label time = new Label();

    @FXML
    private Label timeResult = new Label();

    @FXML
    public Label movesCount = new Label();

    @FXML
    private Label namesLabel = new Label();

    @FXML
    private Label overallResult = new Label();

    @FXML
    private Label top = new Label();

    @FXML
    private Button stop = new Button();

    @FXML
    private Button exit = new Button();

    @FXML
    private Button seeTop = new Button();

    @FXML
    private Button restart = new Button();

    @FXML
    public GridPane gridPane = new GridPane();

    @FXML
    public Label waitName = new Label();

    @FXML
    public TextField textArea = new TextField();

    @FXML
    public Button ready = new Button();

    @FXML
    /**
     * Initializing the main this, like the grid and showing welcome labels and buttons
     * */
    public void initialize() {
        top.setFont(Font.font(10));
        seeTop.setText("See Top 10 games!");
        overallResult.setVisible(false);
        socket = new ClientSocket(this);
        socket.receiveMessage();
        error.setFont(Font.font(15));
        error.setTextFill(Color.RED);
        timeResult.setVisible(false);
        moves = 0;
        gridPane.setBorder(new Border(new BorderStroke(Color.valueOf("#9E9E9E"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths.DEFAULT)));
        draggable.setDraggable(time);
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                grid[i][j] = false;
            }
        }
        exit.setVisible(false);
        exit.setText("Exit the game");
        restart.setVisible(false);
        restart.setText("Restart the game");
        stop.setVisible(false);
        error.setVisible(false);
        error.setText("Choose another place!");
        movesCount.setVisible(false);
        ready.setText("Go further");
        waitName.setText("Specify your name");
        textArea.setVisible(true);
        ready.setVisible(true);
        ready.setDisable(true);
        textArea.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                ready.setDisable(textArea.getText().length() == 0);
            }
        });
    }

    @FXML
    public void sendReady() {
        top.setVisible(false);
        seeTop.setVisible(false);
        new Thread(window).start();
        name = textArea.getText();
        ready.setVisible(false);
        textArea.setVisible(false);
        waitName.setText("Waiting for friend");
        socket.sendMessage("NAME" + name);
    }

    public void getName(String nameGot) {
        nameGot = nameGot.substring(4);
        waitName.setVisible(false);
        serverName = nameGot;
        single = false;
        this.init();
    }

    public void single() {
        top.setVisible(false);
        seeTop.setVisible(false);
        waitName.setVisible(false);
        namesLabel.setText(this.name  + " is the only player");
        this.init();
    }

    @FXML
    /**
     * Method, performing when stopping the game
     * */
    public void stop() {
        top.setVisible(true);
        seeTop.setVisible(true);
        top.setText("");
        time.setVisible(false);
        stop.setVisible(false);
        group.setVisible(false);
        gridPane.setVisible(false);
        error.setVisible(false);
        timeResult.setVisible(true);
        timeResult.setText("Your time is " + time.getText());
        timeResult.setFont(Font.font(25));
        timeResult.setTranslateX(215);
        timeResult.setTranslateY(250);
        movesCount.setText("You have placed " + moves + " figures!");
        movesCount.setVisible(true);
        movesCount.setFont(Font.font(23));
        socket.sendMessage("OverMov" + moves);
        socket.sendMessage("OverMin" + timerMinutes);
        socket.sendMessage("OverSec" + timerSeconds);
        if (single) {
            restart.setVisible(true);
            exit.setVisible(true);
        } else {
            if (!this.rivalReady) {
                this.thisData.add((long) moves);
                this.thisData.add(timerMinutes);
                this.thisData.add(timerSeconds);
                textTime = time.getText();
                waitName.setVisible(true);
                waitName.setTranslateY(550);
                waitName.setText("Waiting for other player to finish...");
                rivalReady = true;
            } else {
                this.friendFinal();
            }
        }
    }

    public void friendFinal() {
        top.setVisible(true);
        seeTop.setVisible(true);
        restart.setVisible(true);
        exit.setVisible(true);
        waitName.setVisible(false);
        time.setVisible(false);
        stop.setVisible(false);
        group.setVisible(false);
        gridPane.setVisible(false);
        error.setVisible(false);
        timeResult.setVisible(true);
        timeResult.setText("Your time is " + textTime +
                "\nTime of " + serverName + " is " + socket.serverMin + " minutes " + socket.serverSec + " seconds");
        timeResult.setFont(Font.font(25));
        timeResult.setTranslateX(215);
        timeResult.setTranslateY(250);
        movesCount.setText("You have placed " + moves + " figures!\n" + serverName + " has placed " + socket.serverMov + " figures!");
        movesCount.setVisible(true);
        movesCount.setFont(Font.font(23));
        overallResult.setVisible(true);
        if (moves > socket.serverMov) {
            overallResult.setText("You are the winner!!!");
        } else {
            if (moves == socket.serverMov) {
                if (thisData.get(1) < socket.serverMin) {
                    overallResult.setText("You are the winner!!!");
                } else {
                    if (thisData.get(2) < socket.serverSec) {
                        overallResult.setText("You are the winner!!!");
                    } else {
                        if (thisData.get(2) == socket.serverSec && thisData.get(1) == socket.serverMin) {
                            overallResult.setText("Wow! It's draw here!");
                        } else {
                            overallResult.setText("Congratulations, you are in second place!");
                        }
                    }
                }
            } else {
                overallResult.setText("Congratulations, you are in second place!");
            }
        }
        this.thisData.clear();
    }

    @FXML
    /**
     * Method for exiting the game
     * */
    public void exit() {
        top.setVisible(false);
        seeTop.setVisible(false);
        socket.sendMessage("Close");
        socket.closeEverything();
        timeResult.setVisible(false);
        movesCount.setText("Thanks for the game!");
        movesCount.setTranslateX(290);
        exit.setVisible(false);
        restart.setVisible(false);
        stop.setVisible(false);
    }

    public void friendLeft() {
        restart.setVisible(true);
        restart.setText("Play alone");
        overallResult.setVisible(false);
        timeResult.setVisible(false);
        movesCount.setTranslateX(290);
        stop.setVisible(false);
        waitName.setVisible(true);
        waitName.setText("Your friend has left the game...");
        if (gridPane.isVisible()) {
            single = true;
            waitName.setText("Your friend has left the game, you are the winner!");
            this.stop();
        }
    }

    public void restartGo() {
        top.setVisible(false);
        seeTop.setVisible(false);
        waitName.setVisible(false);
        rivalReady = false;
        moves = 0;
        timerSeconds = 0;
        timerMinutes = 0;
        time.setVisible(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new Thread(task).start();
            }
        });
        gridPane.setVisible(true);
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                grid[i][j] = false;
            }
        }
        ObservableList<Node> rectangles = gridPane.getChildren();
        for (int i = 0 ; i < rectangles.size(); ++i) {
            if (i % 2 == 0) {
                ((Rectangle)rectangles.get(i)).setFill(Color.SNOW);
            } else {
                ((Rectangle)rectangles.get(i)).setFill(Color.LIGHTGREY);
            }
        }
        gridPane.setVisible(true);
        stop.setVisible(true);
        socket.sendMessage(String.valueOf(moves));
        group.setVisible(true);
        exit.setVisible(false);
        restart.setVisible(false);
        if (!single) {
            namesLabel.setText(this.name + " vs " + serverName);
        } else {
            namesLabel.setText(this.name  + " is the only player");
        }
    }

    @FXML
    /**
     * Method for restarting the game
     * */
    public void restart() {
        top.setVisible(false);
        seeTop.setVisible(false);
        socket.sendMessage("Shake");
        timeResult.setVisible(false);
        movesCount.setVisible(false);
        waitName.setVisible(true);
        waitName.setTranslateY(300);
        waitName.setText("Waiting for friend to restart...");
        overallResult.setVisible(false);
        socket.sendMessage("Restart");
    }

    @FXML
    /**
     * Initializing some other things
     * */
    public void init() {
        top.setVisible(false);
        seeTop.setVisible(false);
        if (!single) {
            namesLabel.setText(this.name + " vs " + serverName);
        } else {
            namesLabel.setText(this.name  + " is the only player");
        }
        namesLabel.setVisible(true);
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(50, 50);
                if ((i + j) % 2 == 0) {
                    rectangle.setFill(Color.SNOW);
                } else {
                    rectangle.setFill(Color.LIGHTGREY);
                }
                gridPane.add(rectangle, i, j);
            }
        }
        group.setVisible(true);
        stop.setText("Click for stop!");
        stop.setVisible(true);
        new Thread(task).start();
        socket.sendMessage(String.valueOf(moves));
        draggable.setDraggable(group);
        setPlace(group);
    }

    /**
     * Placing the figure to its place
     * */
    public void setPlace(Group group) {
        group.setOnMouseReleased(mouseEvent -> {
            ++moves;
            List<Node> rectangles = group.getChildren();
            List<Double> coordX = new ArrayList<>();
            List<Double> coordY = new ArrayList<>();
            for (Node node : rectangles) {
                Bounds bounds = node.getBoundsInLocal();
                Point2D coordinates = node.localToScene(bounds.getCenterX(), bounds.getCenterY());
                coordX.add(coordinates.getX());
                coordY.add(coordinates.getY());
            }
            List<Integer> prevX = new ArrayList<>();
            List<Integer> prevY = new ArrayList<>();
            for (int i = 0 ; i < coordX.size(); ++i) {
                if ((coordX.get(i) < 100 || coordX.get(i) > 550) || (coordY.get(i) < 100 || coordY.get(i) > 550)) {
                    error.setVisible(true);
                    --moves;
                    return;
                }
                long posLine = (Math.round(coordX.get(i)) - 100) / 50;
                prevX.add((int)posLine);
                long posColumn = (Math.round(coordY.get(i)) - 100) / 50;
                prevY.add((int)posColumn);
                if (posColumn == 9 || posLine == 9) {
                    error.setVisible(true);
                    --moves;
                    return;
                }
                if (grid[(int)posLine][(int)posColumn]) {
                    error.setVisible(true);
                    for (int j = 0; j < prevX.size() - 1; ++j) {
                        grid[prevX.get(j)][prevY.get(j)] = false;
                    }
                    --moves;
                    return;
                }
                grid[(int)posLine][(int)posColumn] = true;
            }
            error.setVisible(false);
            List<Node> children = gridPane.getChildren();
            for (int i = 0; i < prevX.size(); ++i) {
                ((Rectangle)children.get(9 * prevX.get(i) + prevY.get(i))).setFill(Color.NAVY);
            }
            socket.sendMessage(String.valueOf(moves));
            group.setTranslateX(600);
            group.setTranslateY(250);
        });
    }

    /**
     * Timer method
     * */
    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                Platform.runLater(() -> {
                    ++timerSeconds;
                    if (timerSeconds == 60) {
                        ++timerMinutes;
                        timerSeconds = 0;
                    }
                    time.setText(timerMinutes + " minutes " + timerSeconds + " seconds");
                });
                Thread.sleep(1000);
            }
        }
    };

    Task<Void> window = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (stage.isShowing()) {
                Thread.sleep(1000);
            }
            exit();
            return null;
        }
    };

    /**
     * Getting a random shape for the figure
     * */
    public void getShape(int key) {
        group.getChildren().clear();
        switch (key) {
            case 1 -> Groups.firstFig(group);
            case 2 -> Groups.secondFig(group);
            case 3 -> Groups.thirdFig(group);
            case 4 -> Groups.forthFig(group);
            case 5 -> Groups.fifthFig(group);
            case 6 -> Groups.sixthFig(group);
            case 7 -> Groups.seventhFig(group);
            case 8 -> Groups.eightFig(group);
            case 9 -> Groups.nineFig(group);
            case 10 -> Groups.tenFig(group);
            case 11 -> Groups.elevenFig(group);
            case 12 -> Groups.twelveFig(group);
            case 13 -> Groups.thirteenFig(group);
            case 14 -> Groups.forteenFig(group);
            case 15 -> Groups.fifteenFig(group);
            case 16 -> Groups.sixtennFig(group);
            case 17 -> Groups.seventeenFig(group);
            case 18 -> Groups.eighteenFig(group);
            case 19 -> Groups.nineteenFig(group);
            case 20 -> Groups.twentyFig(group);
            case 21 -> Groups.twOneFig(group);
            case 22 -> Groups.twTwoFig(group);
            case 23 -> Groups.twThreeFig(group);
            case 24 -> Groups.twFourFig(group);
            case 25 -> Groups.twFiveFig(group);
            case 26 -> Groups.twSixFig(group);
            case 27 -> Groups.twSevenFig(group);
            case 28 -> Groups.twEightFig(group);
            case 29 -> Groups.twNineFig(group);
            case 30 -> Groups.thEOneFig(group);
            case 31 -> Groups.thTwoFig(group);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void seeTop() {
        top.setVisible(true);
        top.setText("");
        socket.sendMessage("TOP");
    }

    public void top(String res) {
        if (res.length() == 0) {
            top.setText("There were no plays!");
        } else {
            top.setText(top.getText() + "\n" + res);
        }
    }
}