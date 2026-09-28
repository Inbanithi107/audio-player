package com.techforge.audioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;

public class AudioPlayerController {

    private final File audioFile;

    @FXML
    private Label audioNameLabel;

    public AudioPlayerController(File audioFile) {
        this.audioFile = audioFile;
    }

    @FXML
    public void initialize(){
        audioNameLabel.setText(audioFile.getName());
    }
}
