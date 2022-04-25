package clqwq.press.push_to_talk.utils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Receiver extends Thread{
    private final int port = 5107;
    private final String ip = "230.198.112.0";
    private InetAddress group = null;
    private MulticastSocket socket = null;

    public Receiver() {
        try {
            group = InetAddress.getByName(ip);
            socket = new MulticastSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] buff = new byte[8192];
            DatagramPacket packet = new DatagramPacket(buff, buff.length, group, port);
            try {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("正在接收的内容：\n" + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
