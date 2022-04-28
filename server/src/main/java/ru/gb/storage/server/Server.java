package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

public class Server {
    private final int port;
    StringBuilder message = new StringBuilder();  // стрингбилдер для склеивания нашего сообшения для отправки клиенту

    public static void main(String[] args) throws InterruptedException {
        new Server(9000).start();
    }

    public Server(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {

                                        @Override
                                        public void channelRegistered(ChannelHandlerContext ctx) {
                                            System.out.println("channelRegistered");
                                        }

                                        @Override
                                        public void channelUnregistered(ChannelHandlerContext ctx) {
                                            System.out.println("channelUnregistered");
                                        }

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) {
                                            System.out.println("channelActive");
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) {
                                            System.out.println("channelInactive");
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println("channelRead");
                                            System.out.println("Received message: ");
                                            final ByteBuf m = (ByteBuf) msg;

                                            for (int i = m.readerIndex(); i < m.writerIndex(); i++) {
                                                System.out.print((char) m.getByte(i));
                                                message.append((char) m.getByte(i));
                                            }
                                            message.append("\n");
                                            m.clear();
                                            System.out.flush();
                                            System.out.println();

                                            char[] ch = new char[message.length()];
                                            message.toString().getChars(0, message.length(), ch, 0);

                                            for (int i = 0; i < ch.length - 1; i++) {
                                                if ((byte) ch[i] == 92 && (byte) ch[i + 1] == 110) {
                                                    message.delete(message.length() - 2, message.length());
                                                    ctx.writeAndFlush(((ByteBuf) msg).writeBytes(message.toString().getBytes(StandardCharsets.UTF_8)));
                                                    message.delete(0, message.length());
                                                }
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            System.out.println("Cause exception");
                                            cause.printStackTrace();
                                            ctx.close();
                                        }
                                    }

                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = server.bind(port).sync().channel();

            System.out.println("Server started");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
