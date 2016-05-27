package com.netty.transport.mockbio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer{


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port);

            Socket socket = null;
            while (true){
                //循环接收客户端请求
                socket = serverSocket.accept();

                //当接收到请求后,处理请求(接收消息,处理消息) 将处理过程包装为task扔到线程池中
                ThreadExecutorsUtil.execute(new TimeServerHandlerTask(socket));
            }
        }finally {
            if (serverSocket != null){
                serverSocket.close();
                serverSocket = null;
            }
        }

    }
}