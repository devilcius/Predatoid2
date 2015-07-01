package com.predatum.predatoid.audio;

import android.util.Log;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

/**
 * @author Marcos
 */
public class SongExtraInfo {

    private AudioFile audioFile;
    private MP3AudioHeader mp3AudioHeader;
    private final String LAME_ID = "LAME";

    public SongExtraInfo(File file) {
        try {
            audioFile = AudioFileIO.read(file);
            mp3AudioHeader = new MP3AudioHeader(file);

        } catch (Exception exception) {
            Log.e(getClass().getName(), "", exception);
        }

    }

    public int getTrackNumber() {

        Tag tag = audioFile.getTag();

        return tag.getFirst(FieldKey.TRACK) == null ? 0 : Integer.parseInt(tag.getFirst(FieldKey.TRACK));
    }

    public String getSongGenre() {

        Tag tag = audioFile.getTag();

        return tag.getFirst(FieldKey.GENRE);

    }

    public String getYear() {

        return audioFile.getTag().getFirst(FieldKey.YEAR);
    }

    public Long getBitrate() {

        return mp3AudioHeader.getBitRateAsNumber();

    }

    public boolean isLameEncoded() {

        String encoder = mp3AudioHeader.getEncoder();

        return encoder.length() >= 4 && encoder.substring(0, 4).equals(LAME_ID);
    }

    public String getLamePreset() {
        String preset = null;
        if (this.isLameEncoded()) {
            if (mp3AudioHeader.isVariableBitRate()) {
                preset = mp3AudioHeader.getPreset();
            } else {
                preset = mp3AudioHeader.getBitRate();
            }

        }
        return preset;
    }
}
