package clqwq.press.push_to_talk.utils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MessageSender extends Thread{
    private int port;
    private String ip;
    private InetAddress group;                      //组播的组地址
    private MulticastSocket multiSocket;            //组播套接字
    private String message;

    public MessageSender(String message) {
        port = Address.port;
        ip = Address.ip;
        try {
            this.message = message;
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
            byte[] buff = message.getBytes();
            packet = new DatagramPacket(buff, buff.length, group, port);
            System.out.println(new String(buff));
            multiSocket.send(packet);
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }

}
