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
            //打开多路复用选择器
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            //配置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //backlog设置为1024,未完成握手三次的连接+完成握手三次的连接=1024
            serverSocketChannel.bind(new InetSocketAddress(port), 1024);
            //监听OP_ACCEPT属性,判断是否有连接请求
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        //选择器开始轮询
        while (!stop){
            try {
                selector.select(1000);
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
    private void handleInput(SelectionKey key) throws IOException{
        if (key.isValid()){
            // 监听到accept事件
            if (key.isAcceptable()){
                //获取就绪的通道
                ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                SocketChannel sc = ssc.accept();
                //将该通道设置为非阻塞
                sc.configureBlocking(false);
                //监听该通道的读事件
                sc.register(selector, SelectionKey.OP_READ);
            }
            //监听到读事件
            if (key.isReadable()){
                //读取数据
                SocketChannel socketChannel = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //开始读
                int readByte = socketChannel.read(readBuffer);
                if (readByte > 0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("server recv message: " + message);
                    String currentTime = "QUERY TIME ORDER".equals(message)? new Date(System.currentTimeMillis()).toString(): "BAD";
                    //写结果到客户端
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