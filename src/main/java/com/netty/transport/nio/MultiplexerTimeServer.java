package com.netty.transport.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable{

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop = false;

    public MultiplexerTimeServer(int port){

        try {
            //�򿪶�·����ѡ����
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            //����Ϊ������
            serverSocketChannel.configureBlocking(false);
            //backlog����Ϊ1024,δ����������ε�����+����������ε�����=1024
            serverSocketChannel.bind(new InetSocketAddress(port), 1024);
            //����OP_ACCEPT����,�ж��Ƿ�����������
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        //ѡ������ʼ��ѯ
        while (!stop){
            try {
                selector.select(1000);
                //������Ҫ������key�б�
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                //һ��һ����ʼ��ѯ���Ƿ��д��ھ���״̬��
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        //�����ھ�����״̬
                        handleInput(key);
                    }catch (Exception e){
                        if(key != null){
                            key.cancel();
                            if (key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }
            }catch (IOException e){

            }
        }

        if (selector!=null){
            try {
                selector.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    /**
     * ���������key
     * @param key
     */
    private void handleInput(SelectionKey key) throws IOException{
        if (key.isValid()){
            // ������accept�¼�
            if (key.isAcceptable()){
                //��ȡ������ͨ��
                ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                SocketChannel sc = ssc.accept();
                //����ͨ������Ϊ������
                sc.configureBlocking(false);
                //������ͨ���Ķ��¼�
                sc.register(selector, SelectionKey.OP_READ);
            }
            //���������¼�
            if (key.isReadable()){
                //��ȡ����
                SocketChannel socketChannel = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //��ʼ��
                int readByte = socketChannel.read(readBuffer);
                if (readByte > 0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("server recv message: " + message);
                    String currentTime = "QUERY TIME ORDER".equals(message)? new Date(System.currentTimeMillis()).toString(): "BAD";
                    //д������ͻ���
                    doWrite(socketChannel, currentTime);
                }
            }
        }
    }

    private void doWrite(SocketChannel socketChannel, String resp)throws IOException{
        if (resp!=null && resp.trim().length()>0){
            byte[] bytes = resp.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
        }
    }
}