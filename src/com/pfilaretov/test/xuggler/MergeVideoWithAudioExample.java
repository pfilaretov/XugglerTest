package com.pfilaretov.test.xuggler;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Pasban
 */
public class MergeVideoWithAudioExample extends JDialog {

    Image image;
    private double videoDuration = 0.00001, videoRead = 0, audioDuration = 0.00001, audioRead = 0;

    public static void main(String[] args) {
        final MergeVideoWithAudioExample merge = new MergeVideoWithAudioExample();
        Thread thread = new Thread() {

            @Override
            public void run() {
                merge.perform("C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/VID_123.mp4",
                    "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/C1.mp3",
                    "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/result.mp4");
                merge.setVisible(false);
                System.exit(0);
            }
        };
        thread.start();
    }

    public void perform(String videoPath, String audioPath, String outputPath) {

        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        // check files are readable
        if (containerVideo.open(videoPath, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cant find " + videoPath);
        }

        if (containerAudio.open(audioPath, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cant find " + audioPath);
        }

        // read video file and create stream
        int numVideoStreams = containerVideo.getNumStreams();
        IStreamCoder coderVideo = null;
        for (int i = 0; i < numVideoStreams; i++) {
            IStream stream = containerVideo.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                coderVideo = coder;
            }
        }

        if (coderVideo == null) {
            throw new RuntimeException("Cannot find video stream");
        }

        if (coderVideo.open(null, null) < 0) {
            throw new RuntimeException("Cant open video coder");
        }

        int width = coderVideo.getWidth();
        int height = coderVideo.getHeight();

        this.setSize(width, height);
        this.setLocationRelativeTo(null);
        this.setVisible(true);


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
            throw new RuntimeException("Cannot find audio stream");
        }

        if (coderAudio.open(null, null) < 0) {
            throw new RuntimeException("Cant open audio coder");
        }

        IPacket packet = IPacket.make();

        IMediaWriter writer = ToolFactory.makeWriter(outputPath);
        writer.addAudioStream(1, 0, coderAudio.getChannels(), coderAudio.getSampleRate());
        writer.addVideoStream(0, 0, width, height);

        videoDuration =
            0.000001 + (containerVideo.getDuration() == Global.NO_PTS ? 0 : (containerVideo.getDuration() / 1000.0));
        audioDuration =
            0.000001 + (containerAudio.getDuration() == Global.NO_PTS ? 0 : (containerAudio.getDuration() / 1000.0));

        while (containerVideo.readNextPacket(packet) >= 0) {
            videoRead = packet.getTimeStamp() * packet.getTimeBase().getDouble() * 1000;

            // video packet
            IVideoPicture picture = IVideoPicture.make(coderVideo.getPixelType(), width, height);
            coderVideo.decodeVideo(picture, packet, 0);
            if (picture.isComplete()) {
                writer.encodeVideo(0, picture);
                IConverter converter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, picture);
                this.setImage(converter.toImage(picture));
                this.setProgress(videoDuration, videoRead, audioDuration, audioRead);
            }

            // audio packet
            containerAudio.readNextPacket(packet);
            audioRead = packet.getTimeStamp() * packet.getTimeBase().getDouble() * 1000;
            IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            coderAudio.decodeAudio(samples, packet, 0);
            if (samples.isComplete()) {
                writer.encodeAudio(1, samples);
                this.setProgress(videoDuration, videoRead, audioDuration, audioRead);
            }

        }

        //write the remaining audio, if your audio is longer than your video
        while (containerAudio.readNextPacket(packet) >= 0) {
            audioRead = packet.getTimeStamp() * packet.getTimeBase().getDouble() * 1000;
            IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            coderAudio.decodeAudio(samples, packet, 0);
            if (samples.isComplete()) {
                writer.encodeAudio(1, samples);
                this.setProgress(videoDuration, videoRead, audioDuration, audioRead);
            }
        }

        coderAudio.close();
        coderVideo.close();
        containerAudio.close();
        containerVideo.close();

        writer.close();
    }

    public MergeVideoWithAudioExample() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void setImage(final Image image) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                MergeVideoWithAudioExample.this.image = image;
                repaint();
            }
        });
    }

    @Override
    public synchronized void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    public static String convertDurationHMSm(double time) {
        long elapsed = (long) (time * 1000);
        long duration = elapsed / 1000;
        long ms = elapsed % 1000;
        return String.format("%02d:%02d:%02d.%02d", duration / 3600, (duration % 3600) / 60, (duration % 60), ms / 10);
    }

    private void setProgress(double videoDuration, double videoRead, double audioDuration, double audioRead) {
        this.setTitle(
            "Video: " + (int) (100 * videoRead / videoDuration) + "%, Audio " + (int) (100 * audioRead / audioDuration)
                + "%");
    }
}