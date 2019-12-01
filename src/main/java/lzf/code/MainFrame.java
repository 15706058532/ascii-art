package lzf.code;


import info.clearthought.layout.TableLayout;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 写点注释
 *
 * @author Zhenfeng Li
 * @version 1.0
 * @date 2019-11-30 20:24
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = -3580930634930090766L;
    public static MainFrame mainFrame;
    public JTextArea textArea;

    public MainFrame() throws HeadlessException, InterruptedException, LineUnavailableException, IOException {
        super("字符画");
        setLayout(new TableLayout(new double[][]{{1, TableLayout.FILL, 1}, {1, TableLayout.FILL, 1}}));
        //窗口居中
        setSize(new Dimension(1220, 775));
        setLocationRelativeTo(null);
        //窗口最小尺寸
        setMinimumSize(new Dimension(1220, 775));
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void init() throws InterruptedException, LineUnavailableException, IOException {
        textArea = new JTextArea();
        Font font = textArea.getFont();
        int size = font.getSize();
        int style = font.getStyle();
        String name = font.getName();
        textArea.setBorder(LineBorder.createBlackLineBorder());
        add(textArea, "1,1,1,1");
        textArea.setText("视频加载需1分钟左右，请稍稍等待一下下");
        textArea.setText("字体名称 "+name+"\r\n字体大小 "+size+" \r\n字体类型："+style);
    }

    public static void main(String[] args) throws InterruptedException, LineUnavailableException, IOException {
        try {
            // 将系统的风格导入到程序中
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        mainFrame = new MainFrame();
        mainFrame.init();
        mainFrame.update(mainFrame.getGraphics());
        URL resource = MainFrame.class.getResource("/jilejingtu.mp4");
        List<String> list = Mp4Utils.imgStrings("jilejingtu.mp4", resource.openStream());
//        List<String> list = Mp4Utils.imgStrings(new File("C:\\Users\\15706\\Desktop\\155b4852ee90e3752c903fbd6b95858c.mp4"));
        System.out.println("播放开始");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            System.out.println("音频播放开始");
            for (byte[] combine : Mp4Utils.combines) {
                Mp4Utils.sourceDataLine.write(combine, 0, combine.length);
                if (countDownLatch.getCount() != 0) {
                    countDownLatch.countDown();
                }
            }
        }).start();
        countDownLatch.await();
        System.out.println("图片播放开始");
        for (String s : list) {
            TimeUnit.NANOSECONDS.sleep(32300000);
            mainFrame.getTextArea().setText(s);
        }
        System.out.println("播放结束");
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
