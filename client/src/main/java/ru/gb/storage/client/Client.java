package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            System.out.println("Echo message: ");
                                            final ByteBuf m = (ByteBuf) msg;
                                            while (m.isReadable()) {
                                                System.out.print((char) m.readByte());
                                            }
                                            System.out.flush();
                                            System.out.println();
                                            ReferenceCountUtil.release(msg);
                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started...");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();

            while (channel.isActive()) {
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();

                ByteBuf msg = Unpooled.wrappedBuffer((message + "\t" + new Date()).getBytes(StandardCharsets.UTF_8));
                channel.write(msg);
                channel.flush();
            }
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}