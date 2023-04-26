package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {

  // размер одной ячейки на игровом поле
  private static final int TILE_SIZE = 20;

  // ширина и высота игрового поля в ячейках
  private static final int WIDTH = 40;
  private static final int HEIGHT = 30;

  private int START_LEN_SNAKE = 4;

  // направление движения змейки
  private int direction = 1;

  long startTime = System.currentTimeMillis();
  int foodCounter = 0;

  // список, который хранит все точки, занимаемые змейкой
  private final List<Point> snake = new ArrayList<>();

  // точка, которую должна съесть змейка, чтобы вырасти
  private Point food = new Point(0, 0);

  private final Random random = new Random();

  // объект для рисования на холсте
  private GraphicsContext graphics;

  // сцена, на которой происходит игра
  private Scene gameScene;

  // сцена, которая появляется, когда игра заканчивается
  private Scene gameOverScene;

  // флаг, определяющий, закончилась ли игра
  private boolean isGameOver = false;

  public static void main(String[] args) {
    // запускает приложение
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    // создает корневой узел для группы элементов
    Group root = new Group();

    // создает холст для рисования
    Canvas canvas = new Canvas(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

    // получает объект GraphicsContext для рисования на холсте
    graphics = canvas.getGraphicsContext2D();

    // добавляет холст в корневой узел
    root.getChildren().add(canvas);

    // создает элемент управления для отображения времени
    Label timeLabel = new Label("Time: 0");
    timeLabel.setFont(Font.font("Arial", 18));
    timeLabel.setTextFill(Color.BLUE);

    // создает элемент управления для отображения количества подобранной еды
    Label foodLabel = new Label("Food: 0");
    foodLabel.setFont(Font.font("Arial", 18));
    foodLabel.setTextFill(Color.BLUE);

    // создает контейнер для элементов управления с отступами
    VBox vbox = new VBox(10);
    vbox.setAlignment(Pos.TOP_LEFT);
    vbox.setPadding(new Insets(10));
    vbox.getChildren().addAll(timeLabel, foodLabel);

    // добавляет контейнер в корневой узел
    root.getChildren().add(vbox);

    // создает сцену для игры
    gameScene = new Scene(root);

    // создает сцену для экрана с сообщением об окончании игры
    gameOverScene = createGameOverScene(primaryStage);

    // обработчик нажатий клавиш на клавиатуре
    gameScene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.UP && direction != 2) {
        direction = 0;
      }
      if (event.getCode() == KeyCode.RIGHT && direction != 3) {
        direction = 1;
      }
      if (event.getCode() == KeyCode.DOWN && direction != 0) {
        direction = 2;
      }
      if (event.getCode() == KeyCode.LEFT && direction != 1) {
        direction = 3;
      }
    });

    // задаем начальную длину змейки
    if (START_LEN_SNAKE <= 0 || START_LEN_SNAKE >= WIDTH * HEIGHT) {
      snake.add(new Point(WIDTH / 2, HEIGHT / 2));
    }
    else {
      for (int i = 0; i < START_LEN_SNAKE; i++) {
        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
      }
    }

    food = createFood();

    new AnimationTimer() {
      long lastUpdate = 0;

      @Override
      public void handle(long now) {
        if (isGameOver) {
          return;
        }

        if (now - lastUpdate >= 100_000_000) {
          lastUpdate = now;
          moveSnake(primaryStage);
          draw();
        }

        // обновляем время игры
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        timeLabel.setText("Time: " + elapsedTime);

        // обновляем количество подобранной еды
        foodLabel.setText("Food: " + foodCounter);
      }
    }.start();

    primaryStage.setScene(gameScene);
    primaryStage.show();
  }

  private Point createFood() {
    int x = random.nextInt(WIDTH);
    int y = random.nextInt(HEIGHT);
    return new Point(x, y);
  }

  private void moveSnake(Stage primaryStage) {
    // создаем объект типа Point, который содержит координаты головы змейки.
    Point head = snake.get(0);

    // проверяет значение направления змейки
    switch (direction) {
      case 0 -> snake.add(0, new Point(head.x, head.y - 1));
      case 1 -> snake.add(0, new Point(head.x + 1, head.y));
      case 2 -> snake.add(0, new Point(head.x, head.y + 1));
      case 3 -> snake.add(0, new Point(head.x - 1, head.y));
    }

    // проверяем, совпадает ли координата головы змейки с координатой еды
    if (snake.get(0).equals(food)) {
      food = createFood();
      foodCounter++;
    } else {
      snake.remove(snake.size() - 1);
    }
    // проверяем, вышла ли змейка за границы игрового поля.
    if (snake.get(0).x < 0 || snake.get(0).x >= WIDTH || snake.get(0).y < 0 || snake.get(0).y >= HEIGHT) {
      isGameOver = true;
      primaryStage.setScene(gameOverScene);
    }

    // проверяем, совпадает ли координата головы змейки с координатой какой-либо другой точки змейки
    for (int i = 1; i < snake.size(); i++) {
      if (snake.get(0).equals(snake.get(i))) {
        isGameOver = true;
        primaryStage.setScene(gameOverScene);
        break;
      }
    }

    if (snake.size() == WIDTH * HEIGHT) {
      isGameOver = true;
      primaryStage.setScene(gameOverScene);
    }
  }

  private void draw() {
    graphics.clearRect(0, 0, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

    graphics.setFill(Color.GREEN);
    for (Point p : snake) {
      graphics.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    graphics.setFill(Color.RED);
    graphics.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
  }

  private Scene createGameOverScene(Stage primaryStage) {
    VBox gameOverRoot = new VBox();
    gameOverRoot.setAlignment(Pos.CENTER);
    gameOverRoot.setSpacing(10);

    Label gameOverLabel = new Label("Game Over");
    gameOverLabel.setTextFill(Color.RED);
    gameOverLabel.setFont(new Font("Arial", 48));

    Button restartButton = new Button("Restart");
    restartButton.setOnAction(event -> {
      snake.clear();
      if (START_LEN_SNAKE <= 0 || START_LEN_SNAKE >= WIDTH * HEIGHT) {
        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
      }
      else {
        for (int i = 0; i < START_LEN_SNAKE; i++) {
          snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        }
      }
      food = createFood();
      direction = 1;
      isGameOver = false;
      primaryStage.setScene(gameScene);
    });

    gameOverRoot.getChildren().addAll(gameOverLabel, restartButton);

    return new Scene(gameOverRoot, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
  }

  private static class Point {
    int x;
    int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Point other) {
        return x == other.x && y == other.y;
      }
      return false;
    }
  }
}
