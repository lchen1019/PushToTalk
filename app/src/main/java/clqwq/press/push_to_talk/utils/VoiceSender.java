package clqwq.press.push_to_talk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class VoiceSender extends Thread{
    private int port;
    private String ip;
    private InetAddress group;                      //组播的组地址
    private MulticastSocket multiSocket;            //组播套接字
    private String filePath;
    private String time;

    public VoiceSender(String filePath, String time) {
        port = Address.port;
        ip = Address.ip;
        this.filePath = filePath;
        this.time = time;
        try {
            group = InetAddress.getByName(ip);              //设置广播组地址
            multiSocket = new MulticastSocket(port);        //多点广播套接字将在port端口广播
            multiSocket.joinGroup(group);
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

    @Override
    public void run() {
        try {
            DatagramPacket packet = null;
            File file = new File(filePath);
            InputStream in = new FileInputStream(file);
            String prefix = Address.name + "&&&" + time;
            byte[] bytes = prefix.getBytes(StandardCharsets.UTF_8);
            System.out.println(prefix.getBytes(StandardCharsets.UTF_8).length);
            // 最多支持1024 KB内容发送
            byte[] buff = new byte[1024 * 1024];
            int len = in.read(buff);
            if (len - 1 + 1 >= 0) System.arraycopy(buff, 0, buff, 100, len - 1 + 1);
            System.out.println(bytes.length);
            System.out.println(bytes.length);
            System.out.println(bytes.length);
            System.out.println(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                buff[i] = bytes[i];
            }
            for (int i = bytes.length; i < 100; i++) {
                buff[i] = 0;
            }
            packet = new DatagramPacket(buff, len + 100, group, port);
            System.out.println(new String(buff));
//            System.out.println("asdasdas");
//            System.out.println(len);
//            System.out.println((len) / 1024 + "KB");
//            System.out.println("asdasdas");
            multiSocket.send(packet);
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

}
