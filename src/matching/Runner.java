package matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Runner extends Application {

	private final int[] ROWSIZE = { 4, 6, 8, 10, 12 };
	private final int WIDTH = 600, HEIGHT = 700;

	private enum Difficulty {
		Easy, Medium, Hard, Debug
	}

	private Difficulty difficulty;

	private int numPairs = 0;
	private int clickCount = 2;
	private int gameSize = 0;

	private static Stage mainStage;
	private Scene titleScene;
	private Scene gameScene;
	private Tile selected = null;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		mainStage = stage;

		titleScene = new Scene(getTitleContent(), WIDTH, HEIGHT);

		mainStage.setTitle("Matching Game");
		mainStage.setScene(titleScene);
		mainStage.show();
	}

	private Parent getTitleContent() {
		VBox root = new VBox();
		root.setSpacing(20);
		root.setAlignment(Pos.CENTER);

		Label title = new Label("Matching Game");
		title.setFont(Font.font("Roboto", 20));

		Label size = new Label("Select which difficulty you want to play with.");
		size.setFont(Font.font("Roboto", 20));

		ComboBox<Integer> boardSizes = new ComboBox<Integer>();
		boardSizes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> arg0, Integer arg1, Integer arg2) {
				gameSize = arg2;
				gameScene = new Scene(createContent());
			}
		});

		ComboBox<Difficulty> difficultyLevels = new ComboBox<>();
		difficultyLevels.getItems().setAll(Difficulty.values());
		difficultyLevels.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Difficulty>() {
			@Override
			public void changed(ObservableValue<? extends Difficulty> arg0, Difficulty arg1, Difficulty arg2) {
				difficulty = arg2;
				boardSizes.getItems().clear();
				switch (difficulty) {
				case Easy:
					for (int i = 0; i < ROWSIZE.length / 2; i++) {
						boardSizes.getItems().add(ROWSIZE[i]);
					}
					break;
				case Medium:
					for (int i : ROWSIZE) {
						boardSizes.getItems().add(i);
					}
					break;
				case Hard:
					for (int i = ROWSIZE.length / 2; i < ROWSIZE.length; i++) {
						boardSizes.getItems().add(ROWSIZE[i]);
					}
					break;
				case Debug:
					mainStage.setScene(new Scene(getWinContent(), WIDTH, HEIGHT));
					break;
				default:
					gameSize = 0;
					break;
				}
				gameScene = new Scene(createContent());
			}
		});

		Button start = new Button("Start");
		start.setOnAction(e -> {
			if (gameSize != 0) {
				mainStage.setScene(gameScene);
			}
		});

		Button close = new Button("Close");
		close.setOnAction(e -> Platform.exit());

		Button help = new Button("Help");
		help.setOnAction(e -> {
			Alert alert = new Alert(AlertType.NONE,
					"Click on the icons and match each item to another that looks similar.", ButtonType.OK);
			alert.showAndWait();

			if (alert.getResult() == ButtonType.OK) {
				alert.close();
			}
		});

		HBox buttons = new HBox();
		buttons.getChildren().addAll(start, help, close);
		buttons.setSpacing(20);
		buttons.setAlignment(Pos.CENTER);

		root.getChildren().addAll(title, size, difficultyLevels, boardSizes, buttons);

		return root;
	}

	private Parent getWinContent() {
		// TODO Implement
		VBox root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.setSpacing(15);
		Label winText = new Label("Congratulations, you win!");
		winText.setFont(Font.font(25));
		Label timePassed = new Label("Add time here");
		timePassed.setFont(Font.font(20));
		Button playAgain = new Button("Play Again"); 
		Button exit = new Button("Exit");
		playAgain.setOnAction(e -> mainStage.setScene(titleScene));
		exit.setOnAction(e -> Platform.exit());
		root.getChildren().addAll(winText, timePassed, playAgain, exit);
		return root;
	}

	private Parent createContent() {
		char c = (char) 33;
		List<Tile> tiles = new ArrayList<>();
		for (int i = 0; i < (gameSize * gameSize) / 2; i++) {
			tiles.add(new Tile(String.valueOf(c)));
			tiles.add(new Tile(String.valueOf(c)));
			c++;
		}

		Collections.shuffle(tiles);

		GridPane root = new GridPane();
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(40));
		int index = 0;
		for (int i = 0; i < gameSize; i++) {
			for (int j = 0; j < gameSize; j++) {
				Tile tile = tiles.get(index);
				GridPane.setRowIndex(tile, i);
				GridPane.setColumnIndex(tile, j);
				root.getChildren().add(tile);
				index++;
			}
		}

		return root;
	}

	private class Tile extends StackPane {

		private Text text = new Text();
		private final int BOXSIZE = 50;

		public Tile(String value) {
			Rectangle border = new Rectangle(BOXSIZE, BOXSIZE);
			border.setStrokeWidth(3);
			border.setFill(null);
			border.setStroke(Color.color(Math.random(), Math.random(), Math.random()));

			text.setText(value);
			text.setFont(Font.font("Comic Sans MS", 30));

			setAlignment(Pos.CENTER);
			getChildren().addAll(border, text);

			setOnMouseClicked(this::handleMouseClick);
			close();
		}

		public void handleMouseClick(MouseEvent event) {
			if (isOpen() || clickCount == 0)
				return;

			clickCount--;

			if (selected == null) {
				selected = this;
				open(() -> {
				});
			} else {
				open(() -> {
					if (!hasSameValue(selected)) {
						selected.close();
						this.close();
					} else {
						numPairs++;
						if (numPairs == (gameSize * gameSize) / 2) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							mainStage.setScene(new Scene(getWinContent(), WIDTH, HEIGHT));
						}
					}
					selected = null;
					clickCount = 2;
				});
			}
		}

		public boolean isOpen() {
			return text.getOpacity() == 1;
		}

		public void open(Runnable action) {
			FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
			ft.setToValue(1);
			ft.setOnFinished(e -> action.run());
			ft.play();
		}

		public void close() {
			FadeTransition ft = new FadeTransition(Duration.seconds(0.5), text);
			ft.setToValue(0);
			ft.play();
		}

		public boolean hasSameValue(Tile other) {
			if (this != other)
				return text.getText().equals(other.text.getText());
			return false;
		}
	}

	public static void defaultCursor() {
		mainStage.getScene().setCursor(Cursor.DEFAULT);
	}

	public static void clickCursor() {
		mainStage.getScene().setCursor(Cursor.HAND);
	}
}
