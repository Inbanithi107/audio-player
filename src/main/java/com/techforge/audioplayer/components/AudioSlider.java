package com.techforge.audioplayer.components;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;

public class AudioSlider extends Slider {

    private final ObjectProperty<Color> activeColor =
            new SimpleObjectProperty<>(Color.DODGERBLUE);

    private final ObjectProperty<Color> inactiveColor =
            new SimpleObjectProperty<>(Color.LIGHTGRAY);

    public AudioSlider() {
        this(0, 100, 50);
    }

    public AudioSlider(double min, double max, double value) {
        super(min, max, value);

        setPrefWidth(250);
        setShowTickMarks(false);
        setShowTickLabels(false);

        valueProperty().addListener((obs, oldVal, newVal) -> updateTrack());
        activeColor.addListener((obs, oldVal, newVal) -> updateTrack());
        inactiveColor.addListener((obs, oldVal, newVal) -> updateTrack());

        skinProperty().addListener((obs, oldSkin, newSkin) -> updateTrack());

        widthProperty().addListener((obs, oldVal, newVal) -> updateTrack());

        sceneProperty().addListener((obs, oldScene, newScene) -> updateTrack());

        setSkin(new SliderSkin(this));
        updateTrack();
    }

    public Color getActiveColor() {
        return activeColor.get();
    }

    public void setActiveColor(Color color) {
        activeColor.set(color);
    }

    public ObjectProperty<Color> activeColorProperty() {
        return activeColor;
    }

    public Color getInactiveColor() {
        return inactiveColor.get();
    }

    public void setInactiveColor(Color color) {
        inactiveColor.set(color);
    }

    public ObjectProperty<Color> inactiveColorProperty() {
        return inactiveColor;
    }

    private void updateTrack() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::updateTrack);
            return;
        }

        Platform.runLater(() -> {
            if (getScene() == null || getSkin() == null) {
                return;
            }

            applyCss();

            Region thumb = (Region) lookup(".track");

            String active = toHex(getActiveColor());
            String inactive = toHex(getInactiveColor());

            if (thumb != null) {
                thumb.setVisible(true);
                thumb.setManaged(true);

                thumb.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 50%;" +
                                "-fx-border-color: " + active + ";" +
                                "-fx-border-width: 3px;" +
                                "-fx-border-radius: 50%;" +
                                "-fx-pref-width: 20px;" +
                                "-fx-pref-height: 20px;" +
                                "-fx-min-width: 20px;" +
                                "-fx-min-height: 20px;" +
                                "-fx-max-width: 20px;" +
                                "-fx-max-height: 20px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 5, 0.2, 0, 2);"
                );
            }

            double range = getMax() - getMin();
            double percentage = range <= 0
                    ? 0
                    : (getValue() - getMin()) / range * 100;

            percentage = Math.max(0, Math.min(100, percentage));



            assert thumb != null;
            thumb.setStyle(
                    "-fx-background-color: linear-gradient(to right, " +
                            active + " 0%, " +
                            active + " " + percentage + "%, " +
                            inactive + " " + percentage + "%, " +
                            inactive + " 100%);"
            );
        });
    }

    private String toHex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
        );
    }
}
