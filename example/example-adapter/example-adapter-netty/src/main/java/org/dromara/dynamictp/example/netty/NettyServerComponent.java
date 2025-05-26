package org.dromara.dynamictp.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Netty server component to demonstrate EventLoopGroup monitoring
 *
 * @author yanhom
 * @since 1.2.2
 */
@Slf4j
@Component
public class NettyServerComponent implements InitializingBean, DisposableBean {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @Override
    public void afterPropertiesSet() throws Exception {
        startNettyServer();
    }

    @Override
    public void destroy() throws Exception {
        stopNettyServer();
    }

    private void startNettyServer() {
        bossGroup = new NioEventLoopGroup(1);  // Boss group with 1 thread
        workerGroup = new NioEventLoopGroup(4);  // Worker group with 4 threads

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    log.info("Received message: {}", msg);
                                    ctx.writeAndFlush("Echo: " + msg);
                                }
                            });
                        }
                    });

            ChannelFuture future = bootstrap.bind(8080).sync();
            serverChannel = future.channel();
            log.info("Netty server started on port 8080");
        } catch (Exception e) {
            log.error("Failed to start Netty server", e);
        }
    }

    private void stopNettyServer() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("Netty server stopped");
    }
}
