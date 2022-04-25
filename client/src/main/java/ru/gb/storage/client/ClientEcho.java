package ru.gb.storage.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientEcho {
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(1);
    private final static Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            new ClientEcho().start();
        } finally {
            THREAD_POOL.shutdown();
        }
    }

    public void start() {
        THREAD_POOL.execute(() -> {
            System.out.println("New client started on thread " + Thread.currentThread().getName());
            try {
                SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));
                while (true) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                    System.out.print("Enter your message: ");
                    String message = SCANNER.nextLine();

                    channel.write(ByteBuffer.wrap(message.getBytes()));
                    channel.read(byteBuffer);
                    byteBuffer.flip();
                    String bf = new String(byteBuffer.array());
                    System.out.println("Echo: " + bf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
