package com.techforge.audioplayer.core;

import com.techforge.audioplayer.data.MetaData;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;

public class Player {

    private final File file;

    public Player(File file) {
        this.file = file;
    }

    public MetaData getMetadata(){
        try {
            AudioFile file = AudioFileIO.read(this.file);
            Tag tag = file.getTag();
            Artwork artwork = tag.getFirstArtwork();
            return new MetaData(tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getFirst(FieldKey.YEAR),
                    tag.getFirst(FieldKey.GENRE),
                    tag.getFirst(FieldKey.TRACK),
                    artwork.getBinaryData()
                    );
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        }
    }

}
