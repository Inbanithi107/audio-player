package com.techforge.audioplayer.core;

@FunctionalInterface
public interface PositionListener {

    void updatePosition(long position);

}
