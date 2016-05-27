package com.netty.transport.mockbio;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TimeClient{

    public static void main(String[] args){

        int port = 8080;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            socket = new Socket("127.0.0.1", port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject("QUERY TIME ORDER");
            out.writeObject(null);
            in = new ObjectInputStream(socket.getInputStream());
            String resp = in.readObject().toString();
            System.out.println("recv msg: " + resp);

        }catch (Exception e){

        }finally {
            //¹Ø±ÕÁ÷

            if (out != null){
                try {
                    out.close();
                    out = null;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (in != null){
                try {
                    in.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }



            if(socket != null){
                try {
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }
}