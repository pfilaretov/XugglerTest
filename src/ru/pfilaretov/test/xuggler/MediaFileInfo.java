package ru.pfilaretov.test.xuggler;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class MediaFileInfo {

    private static final String FILENAME = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/VID_123.mp4";
//    private static final String FILENAME = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/C1.mp3";
//    private static final String FILENAME = "C:/Users/Petr_Filaretov/IdeaProjects/XugglerTest/media/audio/C1.m4a";

    public static void main(String[] args) {

        // first we create a Xuggler container object
        IContainer container = IContainer.make();

        // we attempt to open up the container
        int result = container.open(FILENAME, IContainer.Type.READ, null);

        // check if the operation was successful
        if (result<0)
            throw new RuntimeException("Failed to open media file");

        // query how many streams the call to open found
        int numStreams = container.getNumStreams();

        // query for the total duration
        long duration = container.getDuration();

        // query for the file size
        long fileSize = container.getFileSize();

        // query for the bit rate
        long bitRate = container.getBitRate();

        System.out.println("Number of streams: " + numStreams);
        System.out.println("Duration (ms): " + duration);
        System.out.println("File Size (bytes): " + fileSize);
        System.out.println("Bit Rate: " + bitRate);

        // iterate through the streams to print their meta data
        for (int i=0; i<numStreams; i++) {

            // find the stream object
            IStream stream = container.getStream(i);

            // get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            System.out.println("*** Start of Stream Info ***");

            System.out.printf("\tstream %d; \n", i);
            System.out.printf("\ttype: %s; \n", coder.getCodecType());
            System.out.printf("\tcodec: %s; \n", coder.getCodecID());
            System.out.printf("\tduration: %s; \n", stream.getDuration());
            System.out.printf("\tstart time: %s; \n", container.getStartTime());
            System.out.printf("\ttimebase: %d/%d; \n",
                stream.getTimeBase().getNumerator(),
                stream.getTimeBase().getDenominator());
            System.out.printf("\tcoder tb: %d/%d; \n",
                coder.getTimeBase().getNumerator(),
                coder.getTimeBase().getDenominator());

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                System.out.printf("\tAUDIO sample rate: %d; \n", coder.getSampleRate());
                System.out.printf("\tAUDIO channels: %d; \n", coder.getChannels());
                System.out.printf("\tAUDIO format: %s\n", coder.getSampleFormat());
            }
            else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                System.out.printf("\tVIDEO width: %d; \n", coder.getWidth());
                System.out.printf("\tVIDEO height: %d; \n", coder.getHeight());
                System.out.printf("\tVIDEO format: %s; \n", coder.getPixelType());
                System.out.printf("\tVIDEO frame-rate: %5.2f; \n", coder.getFrameRate().getDouble());
            }

            System.out.println("*** End of Stream Info ***");
        }

    }

}