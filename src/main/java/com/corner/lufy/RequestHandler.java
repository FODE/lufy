package com.corner.lufy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * handler request
 * @author Lei.Sang
 *
 */
public class RequestHandler implements Runnable{
	private Socket so;
	private String requestEntity;
	
	private static String BASE_DOC;
	
	public RequestHandler(Socket so) {
		this.so = so;
		BASE_DOC = System.getProperty("user.dir") +
				File.separator + "src" + File.separator + "main" + File.separator + "webapp" +
				File.separator + "WEB-INF";
	}

	@Override
	public void run() {
		try {
			//get request
			requestEntity = getRequestMessage();
			//response
			response(BASE_DOC + File.separator + getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			close();	
		}
	}
	
	/**
	 * get request path
	 * @return
	 * @throws Exception 
	 */
	private String getPath() throws Exception {
		if(null == requestEntity || requestEntity.equals("")) {
			return "/";
		}
		String[] result = requestEntity.split(" ");
		if(result.length < 2) {
			return "/";
		}
		return result[1];
	}
	
	/**
	 * get request message
	 * @return
	 * @throws Exception
	 */
	private String getRequestMessage() throws Exception{
		InputStream input = so.getInputStream();
		StringBuffer sb = new StringBuffer();
		byte[] word = new byte[1024*2];
		input.read(word);
		sb.append(new String(word));
		//input.close();
		return sb.toString();
	}
	
	/**
	 * response to client
	 * @param filePath
	 * @throws Exception 
	 */
	public void response(String filePath) throws Exception {
		processResponse(filePath, getContentTypeByFile());
	}
	
	/**
	 * process response
	 * @param filePath
	 * @param contentType
	 */
	private void processResponse(String filePath, String contentType) {
		OutputStream out = null;
		InputStream in = null;
		DataInputStream dis = null;
		try {
			if("/".equals(getPath())) {
				filePath += "/index.html";
			}
			//get file input stream
			File file = new File(filePath);
			if(!file.exists()) {
				file = new File(BASE_DOC + "/404.html");
			}
			byte[] buffer = new byte[(int)file.length()];
			dis = new DataInputStream(new FileInputStream(file));
			
			//response http line
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(so.getOutputStream()));
			writer.write("HTTP/1.1 200 OK\r\n");
			writer.write("Date: " + new Date() + "\r\n");
			writer.write("Server: SFHttp/1.0\r\n");
			writer.write("Content-length: " + buffer.length + "\r\n");
			writer.write("Content-type: "+contentType+"\r\n\r\n");
			writer.flush();
			
			//write byte data
			dis.readFully(buffer);
			
			//write data to client
			out = so.getOutputStream();
			out.write(buffer);
			out.flush();
			//Thread.sleep(6000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != in)
					in.close();
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getContentTypeByFile() throws Exception {
		String path = getPath();
		if("/".equals(path)) {
			path = "/index.html";
		}
		String[] paths = path.split("\\.");
		String suffix = paths[1];
		String contentType = "";
		switch(suffix) {
			case "txt":
			case "java":
				contentType = "text/plain";
				break;
			case "html":
			case "htm":
				contentType = "text/html";
				break;
			case "jpg":
			case "JPG":
				contentType = "image/jpg";
				break;
			case "png":
			case "PNG":
				contentType = "image/png";
				break;
			case "zip":
				contentType = "application/zip";
				break;
			case "pdf":
				contentType = "application/pdf";
				break;
			default:
				break;
		}
		return contentType;
	}
	
	/**
	 * close resources
	 */
	private void close() {
		try {
			if(null != so && !so.isClosed())
				so.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
