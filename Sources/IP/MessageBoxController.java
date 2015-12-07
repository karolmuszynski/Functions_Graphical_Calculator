package individual;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class MessageBoxController  {

    @FXML
    private Text infoText;

    Stage stage;

    public void init(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void okButtonHandler() {
        stage.hide();
    }

    public void setMessage(String text) {
        infoText.setText(text);
    }
}
