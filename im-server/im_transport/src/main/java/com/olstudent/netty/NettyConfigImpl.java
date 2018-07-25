package com.olstudent.netty;

import com.olstudent.exception.NullParamsException;
import com.olstudent.handler.AcceptorHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;

/**
 * The implementation of NettyConfig.
 *
 * @author txp.
 */
public class NettyConfigImpl implements NettyConfig {
    private static final Logger logger = Logger.getLogger(NettyConfigImpl.class);

    private final ServerBootstrap bootstrap;
    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;
    private Class channelClass;

    public NettyConfigImpl() {
        bootstrap = new ServerBootstrap();
    }

    @Override
    public void setParentGroup() {
        parentGroup = new NioEventLoopGroup();
    }

    @Override
    public void setParentGroup(int nThreads) {
        parentGroup = new NioEventLoopGroup(nThreads);
    }

    @Override
    public void setChildGroup() {
        childGroup = new NioEventLoopGroup();
    }

    @Override
    public void setChildGroup(int nThreads) {
        childGroup = new NioEventLoopGroup(nThreads);
    }

    @Override
    public void setChannel(Class channelClass) {
        this.channelClass = channelClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setHandler() {
        validate();
        bootstrap.group(parentGroup, childGroup);
        bootstrap.channel(channelClass);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("http-codec", new HttpServerCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                // 心跳包检测 每11秒检测客户端时候发送心跳包
                pipeline.addLast("IdleStateHandler", new IdleStateHandler(11, 0, 0));
                pipeline.addLast("handler",new AcceptorHandler());
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void bind(int port) {
        bind(port, true);
    }

    @Override
    public void bind(int port, boolean sync) {
        ChannelFuture future = null;

        try {
            future = bootstrap.bind(port).sync();
            logger.info("服务器启动成功 监听端口(" + port + ")");

            if (sync) {
                future.channel().closeFuture().sync();
            } else {
                future.channel().closeFuture();
            }
            logger.info("服务器关闭");

        } catch (InterruptedException e) {
            logger.warn("Netty绑定异常", e);
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    private void validate() {
        if (parentGroup == null
                || childGroup == null
                || channelClass == null) {
            throw new NullParamsException("parentGroup == null " +
                    "|| childGroup == null " +
                    "|| channelClass == null");
        }
    }
}
