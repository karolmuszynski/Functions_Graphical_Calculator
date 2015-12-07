package individual;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    final private double windowMinHeight = 810;
    final private double windowMinWidth = 1000;

    @Override
    public void start(Stage primaryStage) throws Exception{

        URL location = getClass().getResource("mainWindow.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = (Parent) fxmlLoader.load(location.openStream());
        Controller controller = fxmlLoader.getController();

        primaryStage.setTitle("CalcF Application");
        primaryStage.setMinHeight(windowMinHeight + 10);
        primaryStage.setMinWidth(windowMinWidth + 10);
        primaryStage.setScene(new Scene(root, windowMinWidth, windowMinHeight));
        primaryStage.getIcons().add(new Image("./img/calculator.png"));
        controller.init(primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
