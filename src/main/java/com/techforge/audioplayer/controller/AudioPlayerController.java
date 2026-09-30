package com.techforge.audioplayer.controller;

import com.techforge.audioplayer.core.Player;
import com.techforge.audioplayer.data.Audio;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AudioPlayerController {

    private final File audioFile;

    @FXML
    private Button backButton;

    @FXML
    private Label audioNameLabel;

    @FXML
    private StackPane metadataContainer;

    @FXML
    private Label metadataLabel;

    @FXML
    private ImageView coverImageView;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Slider progressSlider;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private FontIcon playPauseIcon;

    private boolean playing = true;

    private Player player;

    private Timeline marquee;

    public AudioPlayerController(File audioFile) {
        this.audioFile = audioFile;
        player = new Player(audioFile);
    }

    public void backButtonAction(){
        backButton.setOnAction(e->{
            player.stop();
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(AudioPlayerController.class.getResource("/com/techforge/audioplayer/fxml/home-view.fxml"));
            try {
                stage.setScene(new Scene(loader.load(), stage.getWidth(), stage.getHeight()));
                stage.show();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @FXML
    public void initialize(){
        audioNameLabel.setText(audioFile.getName());
        Audio data = player.getAudio();
        progressSlider.setMax(data.getDuration());
        metadataLabel.setText(data.toString());
        coverImageView.setImage(new Image(new ByteArrayInputStream(data.getImage())));
        totalTimeLabel.setText(formatTime(data.getDuration()));
        Platform.runLater(this::setupMarquee);
        backButtonAction();
        player.setPositionListener((position->{
            Platform.runLater(()->{
                if(!progressSlider.isValueChanging()){
                    progressSlider.setValue(position);
                }
                currentTimeLabel.setText(formatTime(position));
            });
            progressSlider.setOnMouseReleased(e->{
                player.startFrom((int) progressSlider.getValue());
            });
        }));
        player.startFrom(0);
    }

    private void setupMarquee() {

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(metadataContainer.widthProperty());
        clip.heightProperty().bind(metadataContainer.heightProperty());

        metadataContainer.setClip(clip);

        metadataLabel.setTextOverrun(OverrunStyle.CLIP);

        Platform.runLater(() -> {

            metadataLabel.applyCss();
            Text text = new Text(metadataLabel.getText());

            text.setFont(metadataLabel.getFont());

            double textWidth = text.getLayoutBounds().getWidth();
            double containerWidth = metadataContainer.getWidth();
            if (textWidth <= containerWidth) {
                metadataLabel.setTranslateX(0);
                return;
            }
            metadataLabel.setMinWidth(textWidth);
            metadataLabel.setPrefWidth(textWidth);
            metadataLabel.setMaxWidth(textWidth);

            double startX = containerWidth;
            double endX = -textWidth;

            metadataLabel.setTranslateX(startX);

            marquee = new Timeline(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(
                                    metadataLabel.translateXProperty(),
                                    startX
                            )
                    ),

                    new KeyFrame(
                            Duration.seconds(8),
                            new KeyValue(
                                    metadataLabel.translateXProperty(),
                                    endX
                            )
                    )
            );

            marquee.setOnFinished(event -> {

                PauseTransition pause =
                        new PauseTransition(
                                Duration.seconds(1)
                        );

                pause.setOnFinished(e -> {

                    metadataLabel.setTranslateX(startX);

                    marquee.playFromStart();
                });

                pause.play();
            });

            marquee.play();
        });
    }

    @FXML
    private void togglePlayPause() {
        playing = !playing;

        playPauseIcon.setIconLiteral(
                playing ? "fas-pause" : "fas-play"
        );
        player.pauseOrResume();
    }

    private String formatTime(long duration){
        return String.format("%d:%02d", duration / 60, duration %60);
    }
}
