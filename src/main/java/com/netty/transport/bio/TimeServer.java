package com.netty.transport.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 采用同步阻塞模式bio实现网络通信
 */
public class TimeServer{

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port);

            Socket socket = null;
            while (true){
                //循环接收客户端请求
                socket = serverSocket.accept();

                //当接收到请求后,处理请求(接收消息,处理消息)
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