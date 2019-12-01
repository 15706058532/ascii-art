package lzf.code;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 写点注释
 *
 * @author Zhenfeng Li
 * @version 1.0
 * @date 2019-11-30 20:52
 */
public class Mp4Utils {

    public static List<BufferedImage> getTempPath(File file) {
        JTextArea textArea = MainFrame.mainFrame.getTextArea();
        System.out.println("开始解析[" + file.getName() + "]文件");
        List<BufferedImage> bufferedImages = new ArrayList<>();
        try {
            if (file.exists()) {
                FFmpegFrameGrabber ff = new FFmpegFrameGrabber(file);
                ff.start();
                int ftp = ff.getLengthInFrames();
                for (int flag = 0; flag <= ftp; flag++) {
                    //获取帧
                    Frame frame = ff.grabImage();
                    //过滤前3帧，避免出现全黑图片
                    if (flag < 10 || frame == null) {
                        continue;
                    }
                    BufferedImage bufferedImage = frameToBufferedImage(frame);
                    //获取缩放比例
                    double wr = 150 * 1.0 / bufferedImage.getWidth();
                    double hr = 80 * 1.0 / bufferedImage.getHeight();
                    AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
                    BufferedImage imgTemp = ato.filter(bufferedImage, null);
                    bufferedImages.add(imgTemp);
                    textArea.setText("视频转图片进度 " + (int) ((flag * 100.00) / ftp) + "%");
                }
                ff.stop();
                ff.close();
                System.out.println("视频转换图片完成");
                ff.start();
                sampleFormat = ff.getSampleFormat();
                printMusicInfo(ff);
                initSourceDataLine(ff);
                for (int flag = 0; flag <= ftp; flag++) {
                    //获取帧
                    Frame frame = ff.grabSamples();
                    if (frame == null) {
                        continue;
                    }
                    processAudio(frame.samples);
                    textArea.setText("音频解析进度 " + (int) ((flag * 100.00) / ftp) + "%");
                }
                ff.stop();
                ff.close();
                System.out.println("音频处理完成");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("文件[" + file.getName() + "]解析完成");
        return bufferedImages;
    }

    public static List<BufferedImage> getTempPath(String fileName, InputStream inputStream) {
        JTextArea textArea = MainFrame.mainFrame.getTextArea();
        System.out.println("开始解析[" + fileName + "]文件");
        List<BufferedImage> bufferedImages = new ArrayList<>();
        try {
            FFmpegFrameGrabber ff = new FFmpegFrameGrabber(inputStream);
            ff.start();
            int ftp = ff.getLengthInFrames();
            for (int flag = 0; flag <= ftp; flag++) {
                //获取帧
                Frame frame = ff.grabImage();
                //过滤前3帧，避免出现全黑图片
                if (flag < 10 || frame == null) {
                    continue;
                }
                BufferedImage bufferedImage = frameToBufferedImage(frame);
                //获取缩放比例
                double wr = 150 * 1.0 / bufferedImage.getWidth();
                double hr = 80 * 1.0 / bufferedImage.getHeight();
                AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
                BufferedImage imgTemp = ato.filter(bufferedImage, null);
                bufferedImages.add(imgTemp);
                textArea.setText("视频转图片进度 " + (int) ((flag * 100.00) / ftp) + "%");
            }
            ff.stop();
            System.out.println("视频转换图片完成");
            ff.start();
            sampleFormat = ff.getSampleFormat();
            printMusicInfo(ff);
            initSourceDataLine(ff);
            for (int flag = 0; flag <= ftp; flag++) {
                //获取帧
                Frame frame = ff.grabSamples();
                if (frame == null) {
                    continue;
                }
                processAudio(frame.samples);
                textArea.setText("音频解析进度 " + (int) ((flag * 100.00) / ftp) + "%");
            }
            ff.stop();
            ff.close();
            System.out.println("音频处理完成");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("文件[" + fileName + "]解析完成");
        return bufferedImages;
    }

    private static BufferedImage frameToBufferedImage(Frame frame) {
        //创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.getBufferedImage(frame);
    }

    public static List<String> imgStrings(File file) {
        JTextArea textArea = MainFrame.mainFrame.getTextArea();
        List<BufferedImage> bufferedImages = getTempPath(file);
        List<String> list = new ArrayList<>();
        final int[] len = {0};
        bufferedImages.forEach(bufferedImage -> {
            String base = "\"@#&$%*o!;.";// 字符串由复杂到简单
            //输出到指定文件中
            StringBuilder builder = new StringBuilder();
            for (int y = 0; y < bufferedImage.getHeight(); y += 2) {
                for (int x = 0; x < bufferedImage.getWidth(); x += 1) {
                    final int pixel = bufferedImage.getRGB(x, y);
                    final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                    final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                    final int index = Math.round(gray * (base.length() + 1) / 255);
                    String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                    builder.append(s);
                }
                builder.append("\r\n");
            }
            textArea.setText("图片转字符串进度 " + (len[0] * 100.00) / list.size() + "%");
            len[0]++;
            list.add(builder.toString());
        });
        System.out.println("图片转字符串完成");
        return list;
    }

    public static List<String> imgStrings(String fileName, InputStream inputStream) {
        JTextArea textArea = MainFrame.mainFrame.getTextArea();
        List<BufferedImage> bufferedImages = getTempPath(fileName, inputStream);
        List<String> list = new ArrayList<>();
        final int[] len = {0};
        bufferedImages.forEach(bufferedImage -> {
            String base = "\"@#&$%*o!;.";// 字符串由复杂到简单
            //输出到指定文件中
            StringBuilder builder = new StringBuilder();
            for (int y = 0; y < bufferedImage.getHeight(); y += 2) {
                for (int x = 0; x < bufferedImage.getWidth(); x += 1) {
                    final int pixel = bufferedImage.getRGB(x, y);
                    final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                    final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                    final int index = Math.round(gray * (base.length() + 1) / 255);
                    String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                    builder.append(s);
                }
                builder.append("\r\n");
            }
            textArea.setText("图片转字符串进度 " + (len[0] * 100.00) / list.size() + "%");
            len[0]++;
            list.add(builder.toString());
        });
        System.out.println("图片转字符串完成");
        return list;
    }


    public static SourceDataLine sourceDataLine;
    public static Buffer[] buf;
    public static FloatBuffer leftData, rightData;
    public static ShortBuffer ILData, IRData;
    public static ByteBuffer TLData, TRData;
    public static float vol = 1;//音量
    public static int sampleFormat;
    public static byte[] tl, tr;
    public static List<byte[]> combines = new ArrayList<>();

    private static void processAudio(Buffer[] samples) {
        byte[] combine = new byte[0];
        int k;
        buf = samples;
        switch (sampleFormat) {
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                leftData = (FloatBuffer) buf[0];
                TLData = floatToByteValue(leftData, vol);
                rightData = (FloatBuffer) buf[1];
                TRData = floatToByteValue(rightData, vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length + tr.length];
                k = 0;
                for (int i = 0; i < tl.length; i = i + 2) {//混合两个声道。
                    for (int j = 0; j < 2; j++) {
                        combine[j + 4 * k] = tl[i + j];
                        combine[j + 2 + 4 * k] = tr[i + j];
                    }
                    k++;
                }
                combines.add(combine);
//                   sourceDataLine.write(combine,0,combine.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                ILData = (ShortBuffer) buf[0];
                TLData = shortToByteValue(ILData, vol);
                tl = TLData.array();
                combines.add(tl);
//                     sourceDataLine.write(tl,0,tl.length);
                break;
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                leftData = (FloatBuffer) buf[0];
                TLData = floatToByteValue(leftData, vol);
                tl = TLData.array();
                combines.add(tl);
//                           sourceDataLine.write(tl,0,tl.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                ILData = (ShortBuffer) buf[0];
                IRData = (ShortBuffer) buf[1];
                TLData = shortToByteValue(ILData, vol);
                TRData = shortToByteValue(IRData, vol);
                tl = TLData.array();
                tr = TRData.array();
                combine = new byte[tl.length + tr.length];
                k = 0;
                for (int i = 0; i < tl.length; i = i + 2) {
                    for (int j = 0; j < 2; j++) {
                        combine[j + 4 * k] = tl[i + j];
                        combine[j + 2 + 4 * k] = tr[i + j];
                    }
                    k++;
                }
                combines.add(combine);
//                  sourceDataLine.write(combine,0,combine.length);
                break;
            default:
                JOptionPane.showMessageDialog(null, "unsupport audio format", "unsupport audio format", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                break;
        }

    }

    public static AudioFormat af = null;
    public static DataLine.Info dataLineInfo;

    private static void initSourceDataLine(FFmpegFrameGrabber fg) {
        switch (fg.getSampleFormat()) {
            case avutil.AV_SAMPLE_FMT_U8://无符号short 8bit
                break;
            case avutil.AV_SAMPLE_FMT_S16://有符号short 16bit
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.getSampleRate(), 16, fg.getAudioChannels(), fg.getAudioChannels() * 2, fg.getSampleRate(), true);
                break;
            case avutil.AV_SAMPLE_FMT_S32:
                break;
            case avutil.AV_SAMPLE_FMT_FLT:
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.getSampleRate(), 16, fg.getAudioChannels(), fg.getAudioChannels() * 2, fg.getSampleRate(), true);
                break;
            case avutil.AV_SAMPLE_FMT_DBL:
                break;
            case avutil.AV_SAMPLE_FMT_U8P:
                break;
            case avutil.AV_SAMPLE_FMT_S16P://有符号short 16bit,平面型
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.getSampleRate(), 16, fg.getAudioChannels(), fg.getAudioChannels() * 2, fg.getSampleRate(), true);
                break;
            case avutil.AV_SAMPLE_FMT_S32P://有符号short 32bit，平面型，但是32bit的话可能电脑声卡不支持，这种音乐也少见
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.getSampleRate(), 32, fg.getAudioChannels(), fg.getAudioChannels() * 2, fg.getSampleRate(), true);
                break;
            case avutil.AV_SAMPLE_FMT_FLTP://float 平面型 需转为16bit short
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.getSampleRate(), 16, fg.getAudioChannels(), fg.getAudioChannels() * 2, fg.getSampleRate(), true);
                break;
            case avutil.AV_SAMPLE_FMT_DBLP:
                break;
            case avutil.AV_SAMPLE_FMT_S64://有符号short 64bit 非平面型
                break;
            case avutil.AV_SAMPLE_FMT_S64P://有符号short 64bit平面型
                break;
            default:
                System.out.println("不支持的音乐格式");
                System.exit(0);
        }
        dataLineInfo = new DataLine.Info(SourceDataLine.class,
                af, AudioSystem.NOT_SPECIFIED);
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(af);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static ByteBuffer shortToByteValue(ShortBuffer arr, float vol) {
        int len = arr.capacity();
        ByteBuffer bb = ByteBuffer.allocate(len * 2);
        for (int i = 0; i < len; i++) {
            bb.putShort(i * 2, (short) ((float) arr.get(i) * vol));
        }
        return bb; // 默认转为大端序
    }

    public static ByteBuffer floatToByteValue(FloatBuffer arr, float vol) {
        int len = arr.capacity();
        float f;
        float v;
        ByteBuffer res = ByteBuffer.allocate(len * 2);
        v = 32768.0f * vol;
        for (int i = 0; i < len; i++) {
            f = arr.get(i) * v;//Ref：https://stackoverflow.com/questions/15087668/how-to-convert-pcm-samples-in-byte-array-as-floating-point-numbers-in-the-range
            if (f > v) {
                f = v;
            }
            if (f < -v) {
                f = v;
            }
            //默认转为大端序
            res.putShort(i * 2, (short) f);//注意乘以2，因为一次写入两个字节。
        }
        return res;
    }

    private static void printMusicInfo(FFmpegFrameGrabber fg) {
        //System.out.println("音频采样率"+fg.getSampleRate());
        //System.out.println("音频通道数"+fg.getAudioChannels());
    }

}
