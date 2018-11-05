package aioclient;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;

import cache.ResultPoor;

public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel socket;

	private Integer content = 0;
	public Map<Integer, AsynchronousSocketChannel> sockets;
	public Map<Integer, AsynchronousSocketChannel> socketsEmploy;
	private AioTcpClient atc;
	public AioReadHandler(AsynchronousSocketChannel socket, Integer value,
			Map<Integer, AsynchronousSocketChannel> sockets, Map<Integer, AsynchronousSocketChannel> socketsEmploy,AioTcpClient atc) {
		this.socket = socket;
		this.content = value;
		this.sockets = sockets;
		this.socketsEmploy = socketsEmploy;
		this.atc = atc;
	}

	public void cancelled(ByteBuffer attachment) {
		System.out.println("cancelled");
	}

	private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

	private String rsText = "";
	
	
	@Override
	public void completed(Integer i, ByteBuffer buf) {
		if (i > 0) {
			System.out.println("before flip" + buf.limit());

			buf.flip();
			try {
				System.out.println("after flip" + buf.limit());

				// byte[] tbyte = new byte[buf.limit()];
				// buf.get(tbyte);
				// System.out.println("get one byte to string:" + new
				// String(tbyte));

				CharBuffer resultText = decoder.decode(buf);
				// String resultText = new String(tbyte);

				System.out.println("收到" + socket.getRemoteAddress().toString() + "的消息:" + resultText.toString());
				rsText += resultText.toString();
				System.out.println("完整格式：--" + rsText);
				
				buf.compact();
				try (JSONReader reader = new JSONReader(new StringReader(rsText))) {
					JSONObject array = (JSONObject) reader.readObject();
					int sid = array.getIntValue("sid");
					ResultPoor.resultMap.put(sid, rsText);
					
					// buf.clear();
					System.out.println("END" + buf.limit());
					System.out.println("这条可以被解析");
					rsText = "";
					synchronized (this) {
						sockets.put(content, socket);
						socketsEmploy.remove(content);
					}

					System.out.println("使用中socket数量：" + socketsEmploy.size());
					System.out.println("刚归还socket主键：" + content);
					System.out.println("可用socket数量：" + sockets.size());
					
				}catch(Exception e){
					System.out.println("格式化json错误");
				}

			} catch (CharacterCodingException e) {
				System.out.println(1);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println(12);
				e.printStackTrace();
			} finally {
				socket.read(buf, buf, this);
			}

		} else if (i == -1) {
			try {
				System.out.println("对端断线:" + socket.getRemoteAddress().toString());
				buf = null;
				atc.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer buf) {
		System.out.println(exc);
	}
}
