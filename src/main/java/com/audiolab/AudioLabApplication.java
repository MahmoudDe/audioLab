package com.audiolab;

import com.audiolab.i18n.I18n;
import com.audiolab.theme.AppFonts;
import com.audiolab.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
public class AudioLabApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        AppFonts.load();
        I18n.load(Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/MainView.fxml")));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setPrimaryStage(stage);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());

        stage.setTitle(I18n.get("app.title"));
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
    }
}
