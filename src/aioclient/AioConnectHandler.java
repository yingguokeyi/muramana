package aioclient;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

public class AioConnectHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
	private Integer content = 0;
	public Map<Integer, AsynchronousSocketChannel> sockets;
	public Map<Integer, AsynchronousSocketChannel> socketsEmploy;
	private AioTcpClient atc;
	public AioConnectHandler(Integer value, Map<Integer, AsynchronousSocketChannel> sockets,
			Map<Integer, AsynchronousSocketChannel> socketsEmploy,AioTcpClient atc) {
		this.content = value;
		this.sockets = sockets;
		this.socketsEmploy = socketsEmploy;
		this.atc = atc;
	}

	@Override
	public void completed(Void attachment, AsynchronousSocketChannel connector) {
		// try {
		// connector.write(ByteBuffer.wrap(String.valueOf(content).getBytes())).get();
		startRead(connector);
		// } catch (ExecutionException e) {
		// e.printStackTrace();
		// } catch (InterruptedException ep) {
		// ep.printStackTrace();
		// }
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		exc.printStackTrace();
	}

	// 这不是 CompletionHandler接口的方法。
	public void startRead(AsynchronousSocketChannel socket) {
		ByteBuffer clientBuffer = ByteBuffer.allocate(102400);
		
		// read的原型是
		// read(ByteBuffer dst, A attachment,
		// CompletionHandler<Integer,? super A> handler)
		// 即它的操作处理器，的A型，是实际调用read的第二个参数，即clientBuffer。
		// V型是存有read的连接情况的参数
		socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket,content,sockets,socketsEmploy,atc));
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
