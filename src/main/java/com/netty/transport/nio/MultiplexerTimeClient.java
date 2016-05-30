package com.netty.transport.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeClient implements Runnable{


    private String host;

    private int port;

    private Selector selector;

    private SocketChannel socketChannel;

    private volatile boolean stop = false;

    public MultiplexerTimeClient(String host, int port){

        this.host = host == null? "127.0.0.1": host;
        this.port = port;

        try {
            //�򿪶�·����ѡ����
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            //����Ϊ������
            socketChannel.configureBlocking(false);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
   // @Override
    public void run(){

        try {
            doConnect();
        }catch (IOException e){
            e.printStackTrace();
        }
        //ѡ������ʼ��ѯ
        while (!stop){

            try {
                selector.select();
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
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel)key.channel();
            // ������connect�¼�
            if (key.isConnectable()) {
                if (sc.finishConnect()){
                    //������ͨ���Ķ��¼�
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                }else {
                    System.exit(1);
                }
            }
            //���������¼�
            if (key.isReadable()) {
                //��ȡ����
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //��ʼ��
                int readByte = socketChannel.read(readBuffer);
                if (readByte > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("client recv message: " + message);
                    this.stop = true;
                }else if(readByte < 0){
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void doConnect() throws IOException{
       if (socketChannel.connect(new InetSocketAddress(host, port))){
           socketChannel.register(selector, SelectionKey.OP_READ);
           doWrite(socketChannel);
        }else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //doWrite(socketChannel);
        }
    }
    private void doWrite(SocketChannel socketChannel)throws IOException{

            byte[] bytes = "QUERY TIME ORDER".getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            if (!byteBuffer.hasRemaining()){
                System.out.println("send message succeed.");
            }
        }
}