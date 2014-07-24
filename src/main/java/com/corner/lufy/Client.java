package com.corner.lufy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * http client
 * @author Lei.Sang
 *
 */
public class Client implements Runnable{
	private static final int MAX_CLIENT = 1000;
	
	private static final String BASE_DOC = "D:" + File.separator + "download";
	
	public static void main(String args[]) {
		ExecutorService exec = Executors.newCachedThreadPool();
		for(int i = 0; i < MAX_CLIENT; i++) {
			exec.execute(new Client());
		}
		exec.shutdown();
	}
	
	@Override
	public void run() {
		requestServer("localhost", 9111, "/dear.jpg");
	}
	
	/**
	 * send a message to a spacial server
	 * @param addr
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void requestServer(String addr, int port, String filePath) {
		Socket so = null;
		PrintWriter out = null;
		InputStream in = null;
		OutputStream fout = null;
		try {
			//connect to server
			so = new Socket(addr, port);
			
			//request
			out = new PrintWriter(new OutputStreamWriter(so.getOutputStream()));
			StringBuffer sb = new StringBuffer();
			sb.append("GET " + filePath + " HTTP/1.1\r\n");
			sb.append("Host: " + addr + ":" + port + "\r\n");
			sb.append("\r\n");
			out.print(sb.toString());
			out.flush();
			
			//get response message
			byte[] buffer = new byte[1024*1024];
			in = so.getInputStream();
			//out to a file
			File file = new File(BASE_DOC + filePath);
			fout = new FileOutputStream(file);
			while(in.read(buffer) > 0) {
				fout.write(buffer);
				buffer = new byte[1024*1024];
			}
			fout.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != so && !so.isClosed())
					so.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
