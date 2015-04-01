package com.predatum.predatoid.audio;

import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * The first frame can sometimes contain a LAME frame at the end of the Xing frame
 * <p/>
 * <p>This useful to the library because it allows the encoder to be identified, full specification
 * can be found at http://gabriel.mp3-tech.org/mp3infotag.html
 * <p/>
 * Summarized here:
 * 4 bytes:LAME
 * 5 bytes:LAME Encoder Version
 * 1 bytes:VNR Method
 * 1 bytes:Lowpass filter value
 * 8 bytes:Replay Gain
 * 1 byte:Encoding Flags
 * 1 byte:minimal byte rate
 * 3 bytes:extra samples
 * 1 byte:Stereo Mode
 * 1 byte:MP3 Gain
 * 2 bytes:Surround Dound
 * 4 bytes:MusicLength
 * 2 bytes:Music CRC
 * 2 bytes:CRC Tag
 */
public class LameFrame {
    public static final int LAME_HEADER_BUFFER_SIZE = 36;
    public static final int ENCODER_SIZE = 9;   //Includes LAME ID
    public static final int LAME_ID_SIZE = 4;
    public static final String LAME_ID = "LAME";
    private String encoder;
    private String preset;

    /**
     * Initilise a Lame Mpeg Frame
     *
     * @param lameHeader
     */
    private LameFrame(ByteBuffer lameHeader) {
        encoder = Utils.getString(lameHeader, 0, ENCODER_SIZE, TextEncoding.CHARSET_ISO_8859_1);
        setPreset(lameHeader);
    }

    /**
     * Parse frame
     *
     * @param bb
     * @return frame or null if not exists
     */
    public static LameFrame parseLameFrame(ByteBuffer bb) {
        ByteBuffer lameHeader = bb.slice();
        String id = Utils.getString(lameHeader, 0, LAME_ID_SIZE, TextEncoding.CHARSET_ISO_8859_1);
        lameHeader.rewind();
        if (id.equals(LAME_ID)) {
            LameFrame lameFrame = new LameFrame(lameHeader);
            return lameFrame;
        }
        return null;
    }

    /**
     * @return encoder
     */
    public String getEncoder() {
        return encoder;
    }


    /**
     * @return encoder preset
     */
    public String getPreset() {
        return preset;
    }

    /**
     * @return encoder preset
     */
    private void setPreset(ByteBuffer lameHeader) {
        int vbrMethod = lameHeader.get(9) & 0xF;
        int lowpass = lameHeader.get(10) & 0xFF;
        int athType = lameHeader.get(19) & 0xF;
        int headerPreset = (lameHeader.get(27) + lameHeader.get(28)) & 0x1FF;
        List<Integer> presetList = Arrays.asList(410, 420, 430, 440, 450, 460,
                470, 480, 490, 500);
        if (presetList.contains(headerPreset)) {
            preset = "V" + ((500 - headerPreset) / 10);
        } else {
            if (vbrMethod == 3) {
                if (lowpass == 195 || lowpass == 196) {
                    if (athType == 2 || athType == 4) {
                        preset = "APE";
                    }
                } else if (lowpass == 190 && athType == 4) {
                    preset = "APS";
                } else if (lowpass == 180 && athType == 4) {
                    preset = "APM";
                }
            } else if (vbrMethod == 4) {
                if (lowpass == 195 || lowpass == 196) {
                    if (athType == 2 || athType == 4) {
                        preset = "APFE";
                    } else if (athType == 3) {
                        preset = "R3MIX";
                    }
                } else if (lowpass == 190 && athType == 4) {
                    preset = "APFS";
                } else if (lowpass == 180 && athType == 4) {
                    preset = "APFM";
                }
            }
        }
    }

}
