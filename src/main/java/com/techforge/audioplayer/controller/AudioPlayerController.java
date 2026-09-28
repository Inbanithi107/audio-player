package com.techforge.audioplayer.controller;

import com.techforge.audioplayer.core.Player;
import com.techforge.audioplayer.data.MetaData;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;

public class AudioPlayerController {

    private final File audioFile;

    @FXML
    private Label audioNameLabel;

    @FXML
    private StackPane metadataContainer;

    @FXML
    private Label metadataLabel;

    @FXML
    private ImageView coverImageView;

    private Player player;

    private Timeline marquee;

    public AudioPlayerController(File audioFile) {
        this.audioFile = audioFile;
        player = new Player(audioFile);
    }

    @FXML
    public void initialize(){
        audioNameLabel.setText(audioFile.getName());
        MetaData data = player.getMetadata();
        metadataLabel.setText(data.toString());
        coverImageView.setImage(new Image(new ByteArrayInputStream(data.image())));
        Platform.runLater(this::setupMarquee);
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
}
