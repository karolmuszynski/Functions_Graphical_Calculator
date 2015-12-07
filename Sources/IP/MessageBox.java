package individual;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class MessageBox {
    Stage stage;
    MessageBoxController controller;

    public MessageBox(Stage scene) {
        Stage stage = new Stage();
        URL location = getClass().getResource("MessageBox.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = null;
        try {
            root = (Parent) fxmlLoader.load(location.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.controller = fxmlLoader.getController();

        stage.setScene(new Scene(root));
        stage.setTitle("Info");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(scene.getScene().getWindow());
        stage.getIcons().add(new Image("./img/calculator.png"));
        this.stage = stage;
        controller.init(stage);
    }

    public void show(String message) {
        controller.setMessage(message);
        stage.show();
    }
}
