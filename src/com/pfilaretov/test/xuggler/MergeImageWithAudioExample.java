package com.pfilaretov.test.xuggler;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import java.awt.Dimension;
import java.awt.Toolkit;
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
    private static final String C1_AUDIO_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/C1.mp3";
    private static final String BACKGROUND_PATH = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/img/video-background-2.jpg";

    public static void main(String[] args) {
        IContainer containerAudio = IContainer.make();

        // check files are readable
        if (containerAudio.open(C1_AUDIO_PATH, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cant find " + C1_AUDIO_PATH);
        }
        // read audio file and create stream
        int numAudioStreams = containerAudio.getNumStreams();
        IStreamCoder coderAudio = null;
        for (int i = 0; i < numAudioStreams; i++) {
            IStream stream = containerAudio.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                coderAudio = coder;
            }
        }

        if (coderAudio == null) {
            throw new IllegalArgumentException("Cannot find audio stream");
        }

        if (coderAudio.open(null, null) < 0) {
            throw new IllegalArgumentException("Cant open audio coder");
        }

        // make a IMediaWriter to write the file.
        final IMediaWriter writer = ToolFactory.makeWriter(OUTPUT_FILENAME);

        Dimension screenBounds = Toolkit.getDefaultToolkit().getScreenSize();

        // take the background image and convert to the right image type
        BufferedImage backgroundImage = getBackgroundImage();
        backgroundImage = convertToType(backgroundImage, BufferedImage.TYPE_3BYTE_BGR);

        // We tell it we're going to add one video stream, with id 0,
        // at position 0, and that it will have a fixed frame rate of FRAME_RATE.
        int videoInputIndex = 0;
        int videoStreamId = 0;
        int videoStreamIndex = writer.addVideoStream(videoInputIndex, videoStreamId, ID.CODEC_ID_MPEG4,
            screenBounds.width / 2, screenBounds.height / 2);

        int audioInputIndex = 1;
        // TODO - audioStreamId is the same as videoStreamId???
        int audioStreamId = 0;
        int channelCount = 1; //coderAudio.getChannels()
        int sampleRate = coderAudio.getSampleRate();
        int audioStreamIndex = writer.addAudioStream(audioInputIndex, audioStreamId, channelCount, sampleRate);


        IPacket packet = IPacket.make();
        long startTime = System.nanoTime();

        while (containerAudio.readNextPacket(packet) >= 0) {
            writer.encodeVideo(videoStreamIndex, backgroundImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

            IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            coderAudio.decodeAudio(samples, packet, 0);
            if (samples.isComplete()) {
                writer.encodeAudio(audioStreamIndex, samples);
            }
        }

        coderAudio.close();
        containerAudio.close();

        // tell the writer to close and write the trailer if needed
        writer.close();
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