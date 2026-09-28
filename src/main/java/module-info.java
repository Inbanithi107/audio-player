module com.techforge.audioplayer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires jaudiotagger;

    opens com.techforge.audioplayer.controller to javafx.fxml;
    exports com.techforge.audioplayer;
}