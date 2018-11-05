package aioclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cache.ResultPoor;

public abstract class AioTcpClient {
	public Map<Integer, AsynchronousSocketChannel> sockets = new ConcurrentHashMap<Integer, AsynchronousSocketChannel>();
	// 使用中sockets
	public Map<Integer, AsynchronousSocketChannel> socketsEmploy = new ConcurrentHashMap<Integer, AsynchronousSocketChannel>();
	public AioTcpClient me;

	private AsynchronousChannelGroup asyncChannelGroup;

	public AioTcpClient(){
		// 创建线程池
		ExecutorService executor = Executors.newFixedThreadPool(10);
		// 创建异眇通道管理器
		try {
			asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
		} catch (IOException e) {
			e.printStackTrace();
		}

		me = this;
	}

	public void start(final String ip, final int port, int count) {
		// 启动20000个并发连接，使用20个线程的池子
		for (int i = 0; i < count; i++) {
			try {
				// 客户端socket.当然它是异步方式的。
				AsynchronousSocketChannel connector = null;
				if (connector == null || !connector.isOpen()) {
					// 从异步通道管理器处得到客户端socket
					connector = AsynchronousSocketChannel.open(asyncChannelGroup);
					sockets.putIfAbsent(i, connector);

					connector.setOption(StandardSocketOptions.TCP_NODELAY, true);
					connector.setOption(StandardSocketOptions.SO_REUSEADDR, true);
					connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					// 开始连接服务器。这里的的connect原型是
					// connect(SocketAddress remote, A attachment,
					// CompletionHandler<Void,? super A> handler)
					// 也就是它的CompletionHandler 的A型参数是由这里的调用方法
					// 的第二个参数决定。即是connector。客户端连接器。
					// V型为null
					connector.connect(new InetSocketAddress(ip, port), connector,
							new AioConnectHandler(i, sockets, socketsEmploy,this));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void work(String url, int port, int count) throws Exception {
		System.out.println("启动" + url + ":" + port);
		me.start(url, port, count);
		new Thread() {
			public void run() {
				ResultPoor.recovery();
			};
		}.start();
		
	}

	public void send(String str) throws UnsupportedEncodingException {

		AsynchronousSocketChannel socket = null;
		Integer channelId = null;
		boolean flag = true;
		while (flag) {
			synchronized (this) {
				if (sockets.size() > 0) {

					Map.Entry<Integer, AsynchronousSocketChannel> entry = sockets.entrySet().iterator().next();
					channelId = entry.getKey();

					socket = entry.getValue();
					// 此处两个操作做事务安全操作。
					socketsEmploy.put(entry.getKey(), entry.getValue());
					sockets.remove(entry.getKey());

					System.out.println("使用中socket数量：" + socketsEmploy.size());
					System.out.println("刚分配socket主键：" + entry.getValue());
					System.out.println("可用socket数量：" + sockets.size());

					ByteBuffer clientBuffer = ByteBuffer.wrap(str.getBytes("UTF-8"));
					socket.write(clientBuffer, clientBuffer, new AioSendHandler(channelId, socket, this));

					flag = false;
				} else {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}
	public abstract void disconnect();
	
	public AsynchronousSocketChannel getSocketChannel() {

		AsynchronousSocketChannel socket = null;
		if (sockets.size() > 0) {
			Map.Entry<Integer, AsynchronousSocketChannel> entry = sockets.entrySet().iterator().next();

			socket = entry.getValue();

			socketsEmploy.put(entry.getKey(), entry.getValue());
			sockets.remove(entry.getKey());

		}
		return socket;

	}
}
