package com.netty.transport.mockbio;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class TimeServerHandlerTask implements Runnable{

    private  Socket socket;
    public TimeServerHandlerTask(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run(){
        //�����������������
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            //��ȡ�����������
            in = new ObjectInputStream(this.socket.getInputStream());
            out = new ObjectOutputStream(this.socket.getOutputStream());

            //��ʼ��ȡ��Ϣ
            String message = null;
            String currentTime = null;
            while (true){
                Object obj = in.readObject();
                if (obj instanceof Throwable){
                    System.out.println("�쳣������");
                }
                message = obj == null? null : obj.toString();
                if (message == null) break;
                //������Ϣ
                System.out.println("time server recv message: " + message);
                currentTime = "QUERY TIME ORDER".equals(message)? new Date(System.currentTimeMillis()).toString(): "BAD";
                //�������Ϣ�����ͻ���
                out.writeObject(currentTime);
            }
            try {
                Thread.sleep(100);
            }catch (InterruptedException e){

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            //�ر���
            if (in != null){
                try {
                    in.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (out != null){
                try {
                    out.close();
                    out = null;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if(this.socket != null){
                try {
                    this.socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                this.socket = null;
            }
        }
    }


}