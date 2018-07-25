package com.olstudent;

import com.olstudent.netty.NettyConfig;
import com.olstudent.netty.NettyConfigImpl;
import com.olstudent.service.Service;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImServiceApplication {

	public static void main(String[] args) throws InterruptedException{
		//SpringApplication.run(ImServiceApplication.class, args);
		start();
	}

    public static void start() throws InterruptedException {

        // 启动服务
        new Service().initAndStart();

        NettyConfig config = new NettyConfigImpl();
        config.setParentGroup(1);
        config.setChildGroup();
        config.setChannel(NioServerSocketChannel.class);
        config.setHandler();
        config.bind(7397);
    }
}
