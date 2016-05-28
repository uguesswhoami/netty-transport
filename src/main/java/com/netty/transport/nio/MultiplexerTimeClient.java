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
            //打开多路复用选择器
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            //配置为非阻塞
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
        //选择器开始轮询
        while (!stop){

            try {
                selector.select();
                //返回需要监听的key列表
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                //一个一个开始轮询看是否有处于就绪状态的
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        //处理处于就绪的状态
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
     * 处理就绪的key
     * @param key
     */
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel)key.channel();
            // 监听到connect事件
            if (key.isConnectable()) {
                if (sc.finishConnect()){
                    //监听该通道的读事件
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                }else {
                    System.exit(1);
                }
            }
            //监听到读事件
            if (key.isReadable()) {
                //读取数据
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //开始读
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