package aioclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;


public class AioSendHandler implements CompletionHandler
    <Integer,ByteBuffer>
{ 
    private AsynchronousSocketChannel socket;
    private AioTcpClient atc;
    public AioSendHandler(Integer ChannelId, AsynchronousSocketChannel socket,AioTcpClient atc) { 
        this.socket = socket; 
    } 

    @Override
    public void completed(Integer i, ByteBuffer buf) {
        if (i > 0) { 
        	//buf.flip();
            socket.write(buf, buf, this); 
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
    public void failed(Throwable exc, ByteBuffer attachment) {
        System.out.println("cancelled"); 
	}

}
