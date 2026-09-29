package com.techforge.audioplayer.core;

import com.techforge.audioplayer.data.Audio;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Player {

    private final File file;

    private Audio audio;

    private Bitstream audioStream;

    private boolean initialized;

    private volatile boolean stopped = true;

    private volatile boolean paused;

    private volatile boolean audioInitialization;

    private final Object lock = new Object();

    private SourceDataLine line;

    private Thread playbackThread;

    public Player(File file) {
        this.file = file;
    }

    public Audio getAudio(){
        if(audio!=null){
            return audio;
        }
        try {
            AudioFile file = AudioFileIO.read(this.file);
            Tag tag = file.getTag();
            Artwork artwork = tag.getFirstArtwork();
            audio = new Audio(tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getFirst(FieldKey.YEAR),
                    tag.getFirst(FieldKey.GENRE),
                    tag.getFirst(FieldKey.TRACK),
                    artwork.getBinaryData()
                    );
            initAudio();
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        }
        return audio;
    }

    private void initAudio() {
        audio.setDuration(getDuration());
    }

    private String getDuration() {
        init(false);
        long frames = 0;
        try {
            Header header = audioStream.readFrame();
            double ms_per_frame = header.ms_per_frame();
            while (audioStream.readFrame()!=null){
                frames++;
                audioStream.closeFrame();
            }
            long duration = (long) ((frames * ms_per_frame) / 1000);
            audio.setTotalFrames(frames);
            return String.format("%d:%02d", duration/60, duration%60);
        } catch (BitstreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(boolean forceInit){
        if(!initialized || forceInit) {
            try {
                audioStream = new Bitstream(new FileInputStream(file));
                initialized = true;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startFrom(int seconds){
        playbackThread = new Thread(()->{
            if(!stopped){
                stop();
            }
            init(true);
            Decoder decoder = new Decoder();
            initAudioDevice();
            Header header;
            long frameToStart = getFrameForSeconds(seconds);
            long framesPlayed = 0;
            while (!stopped){
                synchronized (lock){
                    while (paused && !stopped){
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if(stopped){
                    break;
                }
                try {
                    if (((header = audioStream.readFrame())!=null)) {
                        if(framesPlayed < frameToStart){
                            audioStream.closeFrame();
                            framesPlayed++;
                            continue;
                        }
                        SampleBuffer buffer = (SampleBuffer) decoder.decodeFrame(header, audioStream);
                        short[] buff = buffer.getBuffer();
                        int buffLength = buffer.getBufferLength();
                        byte[] pcm = new byte[buffLength*2];
                        for(int i=0;i<buffLength;i++){
                            pcm[i*2] = (byte) (buff[i] & 0xFF);
                            pcm[i*2+1] = (byte) ((buff[i] >> 8) & 0xFF);
                        }
                        line.write(pcm, 0, pcm.length);
                        framesPlayed++;
                        audioStream.closeFrame();
                    }else {
                        stop();
                    }
                } catch (BitstreamException | DecoderException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        playbackThread.start();
    }

    private void initAudioDevice(){
        synchronized (lock){
            audioInitialization = true;
            stopped = false;
        }
        try {
            Header header = audioStream.readFrame();
            int sampleRate = header.frequency();
            int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
        } catch (BitstreamException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        if(stopped){
            return;
        }
        if(!audioInitialization){
            return;
        }
        synchronized (lock){
            stopped = true;
            lock.notify();
            line.stop();
            line.flush();
            line.close();
        }

    }

    public void pauseOrResume(){
        synchronized (lock) {
            paused = !paused;
            if (paused) {
                line.stop();
            } else {
                line.start();
            }
            lock.notify();
        }
    }

    private long getFrameForSeconds(int seconds){
        double framesPerSecond = (double) 44100 / 1152;
        return (long) (framesPerSecond * seconds);
    }

}
