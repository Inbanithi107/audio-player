package com.techforge.audioplayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class HomeViewController {

    @FXML
    private Label fileNameLabel;

    @FXML
    private Button playButton;

    private File file;

    @FXML
    protected void chooseFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Audio File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files",
                "*.mp3",
                "*.mpeg",
                "*.wav",
                "*.m4a",
                "*.aac",
                "*.flac",
                "*.ogg"));
        file = fileChooser.showOpenDialog(playButton.getScene().getWindow());
        if(file==null){
            return;
        }
        fileNameLabel.setText(file.getName().substring(0, file.getName().lastIndexOf(".")));
        playButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        playButton.setDisable(false);
    }

    @FXML
    private void initialize(){
        playButton.setOnMouseClicked(e-> {
            FXMLLoader loader = new FXMLLoader(HomeViewController.class.getResource("/com/techforge/audioplayer/fxml/AudioPlayerView.fxml"));
            loader.setController(new AudioPlayerController(file));
            Stage stage = (Stage) playButton.getScene().getWindow();
            try {
                stage.setScene(new Scene(loader.load(), stage.getWidth(), stage.getHeight()));
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
