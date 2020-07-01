package ru.pfilaretov.test.xuggler;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/**
 * IT WORKS!!!
 */
public class MergeImageWithAudioExample {

    private static final String OUTPUT_FILENAME = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/MergeImageWithAudioExample.mp4";
    private static final String BACKGROUND_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/img/video-background-2.jpg";
    private static final String C1_AUDIO_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/C1.mp3";
    private static final String D1_AUDIO_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/D1.mp3";
    private static final String E1_AUDIO_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/E1.mp3";

    // assume the number of packets in each audio
    private static final int PACKETS_COUNT = 99;

    public static void main(String[] args) {
        // C1
        IContainer c1AudioContainer = IContainer.make();

        // check files are readable
        if (c1AudioContainer.open(C1_AUDIO_PATH, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cannot find " + C1_AUDIO_PATH);
        }
        // read audio file and create stream
        IStreamCoder c1AudioCoder = null;
        int c1AudioStreamIndex = 0;
        for (int i = 0; i < c1AudioContainer.getNumStreams(); i++) {
            IStream stream = c1AudioContainer.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                c1AudioCoder = coder;
                c1AudioStreamIndex = i;
            }
        }

        if (c1AudioCoder == null) {
            throw new IllegalArgumentException("Cannot find audio stream for " + C1_AUDIO_PATH);
        }

        if (c1AudioCoder.open(null, null) < 0) {
            throw new IllegalArgumentException("Cannot open audio coder for " + C1_AUDIO_PATH);
        }

        // D1
        IContainer d1AudioContainer = IContainer.make();

        // check files are readable
        if (d1AudioContainer.open(D1_AUDIO_PATH, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cannot find " + D1_AUDIO_PATH);
        }
        // read audio file and create stream
        IStreamCoder d1AudioCoder = null;
        int d1AudioStreamIndex = 0;
        for (int i = 0; i < d1AudioContainer.getNumStreams(); i++) {
            IStream stream = d1AudioContainer.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                d1AudioCoder = coder;
                d1AudioStreamIndex = i;
            }
        }

        if (d1AudioCoder == null) {
            throw new IllegalArgumentException("Cannot find audio stream for " + D1_AUDIO_PATH);
        }

        if (d1AudioCoder.open(null, null) < 0) {
            throw new IllegalArgumentException("Cannot open audio coder for " + D1_AUDIO_PATH);
        }

        // E1
        IContainer e1AudioContainer = IContainer.make();

        // check files are readable
        if (e1AudioContainer.open(E1_AUDIO_PATH, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cannot find " + E1_AUDIO_PATH);
        }
        // read audio file and create stream
        IStreamCoder e1AudioCoder = null;
        int e1AudioStreamIndex = 0;
        for (int i = 0; i < e1AudioContainer.getNumStreams(); i++) {
            IStream stream = e1AudioContainer.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                e1AudioCoder = coder;
                e1AudioStreamIndex = i;
            }
        }

        if (e1AudioCoder == null) {
            throw new IllegalArgumentException("Cannot find audio stream for " + E1_AUDIO_PATH);
        }

        if (e1AudioCoder.open(null, null) < 0) {
            throw new IllegalArgumentException("Cannot open audio coder for " + E1_AUDIO_PATH);
        }

        // make a IMediaWriter to write the file.
        final IMediaWriter writer = ToolFactory.makeWriter(OUTPUT_FILENAME);

        // take the background image and convert to the right image type
        BufferedImage backgroundImage = getBackgroundImage();
        backgroundImage = convertToType(backgroundImage, BufferedImage.TYPE_3BYTE_BGR);

        // We tell it we're going to add one video stream, with id 0,
        // at position 0, and that it will have a fixed frame rate of FRAME_RATE.
        int videoInputIndex = 0;
        int videoStreamId = 0;
        // these are min values that works for background image, magic numbers...
        int width = 750;
        int height = 468;
        int videoStreamIndex = writer.addVideoStream(videoInputIndex, videoStreamId, ID.CODEC_ID_MPEG4, width, height);

        int audioInputIndex = 1;
        // TODO - audioStreamId is the same as videoStreamId???
        int audioStreamId = 0;
        int channelCount = 1; //c1AudioCoder.getChannels()
        int sampleRate = c1AudioCoder.getSampleRate();
        int audioStreamIndex = writer.addAudioStream(audioInputIndex, audioStreamId, channelCount, sampleRate);

        long startTime = System.nanoTime();

        // c1.4
        play(c1AudioContainer, c1AudioCoder, c1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 4);

        // d1.8 x2
        play(d1AudioContainer, d1AudioCoder, d1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 8);
        play(d1AudioContainer, d1AudioCoder, d1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 8);


        // e1.16 x4
        play(e1AudioContainer, e1AudioCoder, e1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 16);
        play(e1AudioContainer, e1AudioCoder, e1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 16);
        play(e1AudioContainer, e1AudioCoder, e1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 16);
        play(e1AudioContainer, e1AudioCoder, e1AudioStreamIndex, writer, videoStreamIndex, audioStreamIndex,
            backgroundImage, startTime, 16);


        c1AudioCoder.close();
        c1AudioContainer.close();

        d1AudioCoder.close();
        d1AudioContainer.close();

        e1AudioCoder.close();
        e1AudioContainer.close();

        // tell the writer to close and write the trailer if needed
        writer.close();
    }

    private static void play(IContainer audioContainer, IStreamCoder audioCoder, int audioStreamIndex,
        IMediaWriter writer, int videoStreamIndex, int videoAudioStreamIndex, BufferedImage backgroundImage, long startTime, int duration) {
        // scroll to the beginning of the audio file
        int seekFrameResult = audioContainer
            .seekKeyFrame(audioStreamIndex, audioContainer.getStartTime(), IContainer.SEEK_FLAG_ANY);
        if (seekFrameResult < 0) {
            throw new RuntimeException("Cannot seek key frame");
        }

        // TODO - pass as a parameter to save some memory?
        IPacket packet = IPacket.make();

        int iteration = 0;
        while (audioContainer.readNextPacket(packet) >= 0 && iteration != PACKETS_COUNT / duration) {
            writer.encodeVideo(videoStreamIndex, backgroundImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            IAudioSamples samples = IAudioSamples.make(512, audioCoder.getChannels(), IAudioSamples.Format.FMT_S32);
            audioCoder.decodeAudio(samples, packet, 0);
            if (samples.isComplete()) {
                writer.encodeAudio(videoAudioStreamIndex, samples);
            }

            iteration++;
        }
    }

    private static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        BufferedImage image;

        // if the source image is already the target type, return the source image
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        }
        // otherwise create a new image of the target type and draw the new image
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }

    private static BufferedImage getBackgroundImage() {
        try {
            return ImageIO.read(new File(BACKGROUND_PATH));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image by path: " + BACKGROUND_PATH, e);
        }

    }

}