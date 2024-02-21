package com.example.tischtennissimulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TischtennisSimulation extends Application {

    public void setDifficultyParameters() {
        switch (Difficulty) {
            case "Very Easy":
                BatSizeParameter = 80;
                BallSpeedParameter = -4;
                break;
            case "Easy":
                BatSizeParameter = 40;
                BallSpeedParameter = -2;
                break;
            case "Normal":
                BatSizeParameter = 0;
                BallSpeedParameter = 0;
                break;
            case "Hard":
                BatSizeParameter = -60;
                BallSpeedParameter = 2;
                break;
            case "Very Hard":
                BatSizeParameter = -90;
                BallSpeedParameter = 4;
                break;
        }

        BAT_W = 120 + BatSizeParameter;
        bat.setWidth(BAT_W);
    }

    private TextField inputField;
    public String Difficulty;

    private Stage stage;
    private Scene scene1;
    private Scene scene2;
    private Scene scene3;


    private enum UserAction {
        NONE, LEFT, RIGHT
    }
    private UserAction action = UserAction.NONE;


    private static final int APP_W = 600;
    private static final int APP_H = 600;
    Random random = new Random();
    private int amountOfBalls ;
    private static final int BALL_RADIUS = 10;
    private int BallSpeedParameter = 0;
    private int BatSizeParameter;
    private int BAT_W = (120 + BatSizeParameter);
    private static final int BAT_H = 20;
    private Rectangle bat = new Rectangle(BAT_W, BAT_H);

    private List<Circle> balls = new ArrayList<>();
    private List<Circle> activeBalls = new ArrayList<>();



    private Timeline timeline = new Timeline();
    private int totalSurvivalTime = 0;
    private int amountOfTries = 0;
    private boolean running = true;
    private int timeCounter = 0;

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(APP_W, 630);

        Line line = new Line(0, APP_H , APP_W, APP_H );
        line.setStroke(Color.BLACK);

        bat.setTranslateX((Math.random() * (APP_W - BAT_W) - (0 + BAT_W)) + (0 + BAT_W));
        bat.setTranslateY(APP_H - BAT_H);
        bat.setFill(Color.GREEN);

        Label timestamp = new Label("Average Survival Time : 0 seconds");
        timestamp.setTranslateX(20);
        timestamp.setTranslateY(APP_H - timestamp.getHeight());

        Timeline timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int averageSurvivalTime = 0;
            if(amountOfTries != 0){
                averageSurvivalTime = totalSurvivalTime / amountOfTries;
            }
            timestamp.setText("Average Survival Time : " + averageSurvivalTime + " seconds");
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();

        Button button1 = new Button("Give Up");
        button1.setTranslateX(APP_W  - 75);
        button1.setTranslateY(APP_H);
        button1.setOnAction(e -> {
            stopGame();
        });



        for (int i = 0; i < amountOfBalls; i++) {
            Circle ball = new Circle(BALL_RADIUS, Color.RED);
            ball.setTranslateX((Math.random() * ((APP_W - (2 * BALL_RADIUS)) - (0 + (2 * BALL_RADIUS)))) + (0 + (2 * BALL_RADIUS)));
            ball.setTranslateY((Math.random() * ((APP_H - BAT_H - (2 * BALL_RADIUS)) - (0 + (2 * BALL_RADIUS)))) + (0 + (2 * BALL_RADIUS)));
            root.getChildren().add(ball);
            balls.add(ball);
            activeBalls.add(ball);
            setRandomBallDirection(ball);
        }

        KeyFrame frame = new KeyFrame(Duration.seconds(0.016), event -> {
            if (!running)
                return;
            switch (action) {
                case LEFT:
                    if (bat.getTranslateX() - 5 >= 0)
                        bat.setTranslateX(bat.getTranslateX() - 5);
                    break;
                case RIGHT:
                    if (bat.getTranslateX() + BAT_W + 5 <= APP_W)
                        bat.setTranslateX(bat.getTranslateX() + 5);
                    break;
                case NONE:
                    break;
            }

            for (Circle ball : activeBalls) {
                ball.setTranslateX(ball.getTranslateX() + (getBallDirectionX(ball) * (5 + BallSpeedParameter)));
                ball.setTranslateY(ball.getTranslateY() + (getBallDirectionY(ball) * (5 + BallSpeedParameter)));

                if (ball.getTranslateX() - BALL_RADIUS <= 0 || ball.getTranslateX() + BALL_RADIUS >= APP_W)
                    reverseBallDirectionX(ball);

                if (ball.getTranslateY() - BALL_RADIUS <= 0) {
                    reverseBallDirectionY(ball);
                } else if (ball.getTranslateY() + BALL_RADIUS >= APP_H - BAT_H) {
                    if (ball.getTranslateX() + BALL_RADIUS >= bat.getTranslateX() &&
                            ball.getTranslateX() - BALL_RADIUS <= bat.getTranslateX() + BAT_W) {
                        setRandomBallDirection(ball);
                    } else {
                        root.getChildren().remove(ball);
                        balls.remove(ball);
                    }
                }
            }
            if (balls.isEmpty()) {
                stopGame();
            }
            timeCounter++;
            if (timeCounter % 625 == 0) {
                updateBallSpeed(1.1);
            }
        });
        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        root.getChildren().addAll(line,bat,button1,timestamp);

        return root;
    }


    private void stopGame() {
        running = false;
        timeline.stop();
        switchScenes(scene3);
    }

    private void startGame() {
        bat.setTranslateX((Math.random() * (APP_W - BAT_W) - (0 + BAT_W)) + (0 + BAT_W));
        timeline.play();
        running = true;
    }

    private Scene createSceneOne() {
        Label label = new Label("Select Difficulty:");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.getItems().addAll("Very Easy", "Easy", "Normal", "Hard", "Very Hard");
        choice.getSelectionModel().select(2);
        choice.setStyle("-fx-font-size: 14px;");

        Label label2 = new Label("Number of Balls :");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        inputField = new TextField();
        inputField.setMaxWidth(65);
        inputField.setText("1");
        inputField.setStyle("-fx-font-size: 14px;");

        Button button1 = new Button("Play!");
        button1.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");
        button1.setOnAction(e -> {
            Difficulty = choice.getValue();
            setDifficultyParameters();
            amountOfBalls = Integer.parseInt(inputField.getText());
            switchScenes(scene2);
            updateBalls();
            startGame();
        });

        VBox vbox = new VBox(10, label, choice,label2, inputField, button1);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(100));
        vbox.setBackground(new Background(new BackgroundFill(Color.LIGHTSTEELBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        scene1 = new Scene(vbox, 600, 600);

        return scene1;
    }

    private Scene createSceneTwo() {
        Scene scene2 = new Scene(createContent());
        scene2.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case A:
                    action = UserAction.LEFT;
                    break;
                case D:
                    action = UserAction.RIGHT;
                    break;
                case ESCAPE:
                    stopGame();
                    break;
            }
        });

        scene2.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case A:
                case D:
                    action = UserAction.NONE;
                    break;
            }
        });

        switchScenes(scene2);
        return scene2;
    }

    private Scene createSceneThree() {
        Label label = new Label("Game Over");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label timerLabel = new Label("Survival Time: 0 seconds");
        timerLabel.setStyle("-fx-font-size: 14px;");

        Button button1 = new Button("Restart");
        button1.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");
        button1.setOnAction(e -> {
            totalSurvivalTime += (timeCounter / 60);
            amountOfTries++;
            timeCounter = 0;
            switchScenes(scene1);
        });

        Button button2 = new Button("Exit");
        button2.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");
        button2.setOnAction(e -> {
            Platform.exit();
        });

        VBox vbox = new VBox(10, label, timerLabel, button1,button2);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(100));
        vbox.setBackground(new Background(new BackgroundFill(Color.LIGHTSTEELBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        scene3 = new Scene(vbox, 600, 600);
        Timeline timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int survivalTime = timeCounter / 60;
            timerLabel.setText("Time Survived : " + survivalTime + " seconds");
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();

        return scene3;
    }

    public void switchScenes(Scene scene) {
        stage.setScene(scene);
    }

    private void updateBalls() {
        Pane root = (Pane) scene2.getRoot();
        root.getChildren().removeAll(balls);
        balls.clear();

        for (int i = 0; i < amountOfBalls; i++) {
            Circle ball = new Circle(BALL_RADIUS, Color.RED);
            ball.setTranslateX((Math.random() * ((APP_W - (2 * BALL_RADIUS)) - (0 + (2 * BALL_RADIUS)))) + (0 + (2 * BALL_RADIUS)));
            ball.setTranslateY((Math.random() * ((APP_H - BAT_H - (2 * BALL_RADIUS)) - (0 + (2 * BALL_RADIUS)))) + (0 + (2 * BALL_RADIUS)));

            setRandomBallDirection(ball);

            root.getChildren().add(ball);
            balls.add(ball);
            activeBalls.add(ball);
        }
    }

    private void setRandomBallDirection(Circle ball) {
        double randomAngle = Math.toRadians(180 + random.nextDouble() * 180);
        double directionX = Math.cos(randomAngle);
        double directionY = Math.sin(randomAngle);
        ball.setUserData(new double[]{directionX, directionY});
    }

    private double getBallDirectionX(Circle ball) {
        double[] direction = (double[]) ball.getUserData();
        return direction[0];
    }

    private double getBallDirectionY(Circle ball) {
        double[] direction = (double[]) ball.getUserData();
        return direction[1];
    }

    private void reverseBallDirectionX(Circle ball) {
        double[] direction = (double[]) ball.getUserData();
        direction[0] *= -1;
    }

    private void reverseBallDirectionY(Circle ball) {
        double[] direction = (double[]) ball.getUserData();
        direction[1] *= -1;
    }

    private void updateBallSpeed(double multiplier) {
        BallSpeedParameter *= multiplier;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        scene1 = createSceneOne();
        scene2 = createSceneTwo();
        scene3 = createSceneThree();

        primaryStage.setTitle("Tischtennis Simulation");
        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}