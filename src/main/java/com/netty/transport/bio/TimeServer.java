package com.netty.transport.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ����ͬ������ģʽbioʵ������ͨ��
 */
public class TimeServer{

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port);

            Socket socket = null;
            while (true){
                //ѭ�����տͻ�������
                socket = serverSocket.accept();

                //�����յ������,��������(������Ϣ,������Ϣ)
                new Thread(new TimeServerHandler(socket)).start();
            }
        }finally {
            if (serverSocket != null){
                serverSocket.close();
                serverSocket = null;
            }
        }

    }
}