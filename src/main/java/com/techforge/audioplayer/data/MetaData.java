package com.techforge.audioplayer.data;

public record MetaData(String title, String artist, String album, String year, String genre, String track, byte[] image) {
    public String toString(){
        return String.format("%s, %s, %s", artist, album, year);
    }
}
