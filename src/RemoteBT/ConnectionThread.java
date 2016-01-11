package RemoteBT;


import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.microedition.io.StreamConnection;
import javax.swing.ImageIcon;


public class ConnectionThread extends Observable implements Runnable {
	private StreamConnection con;
	InputStream inputStream;
	OutputStream responseStream;
	
	private static final int READ_NUMBER_RESPONSE = 7;
	private static final int READ_FILE_RESPONSE = 6;
	private static final int READ_COMMAND_OK_RESPONSE = 5;
	private static final int READ_COMMAND_NOT_OK_RESPONSE = 8;
	
	private byte[] data;
	private boolean modeCommand = false;
	public ConnectionThread(StreamConnection c, Observer o, boolean controlMode) {
		con = c;
		this.modeCommand = controlMode;
		this.addObserver(o);
	}
	
	public void sun(){
		try{
		InputStream inputStream = con.openInputStream();
		while (true) {
            int command = inputStream.read();

            if (command == 0) {
                System.out.println("finish process");
                break;
            }
            //processCommand(command);
        }
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void run(){
		System.out.println("Started thread...");
		try {
			inputStream = con.openInputStream();
			responseStream = con.openOutputStream();
		} catch (IOException e){
			e.printStackTrace();
			return;
		}
		if(!modeCommand){
			processFiles();
		}
		else{
			processCommands();
		}
	}
	
	private void xxxprocessCommand(int command) {
		try {
			Robot robot = new Robot();
			switch (command) {
	    	case RemoteBTServer.NEXT_SLIDE_NOTIFY:
	    		robot.keyPress(KeyEvent.VK_RIGHT);  		
	    		robot.keyRelease(KeyEvent.VK_RIGHT);
	    		break;
	    	case RemoteBTServer.PREV_SLIDE_NOTIFY:
	    		robot.keyPress(KeyEvent.VK_LEFT);   		
	    		robot.keyRelease(KeyEvent.VK_LEFT);
	    		break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void processFiles(){
		int count = 0;
		int slidesNumber = -1;
		try {
			data = new byte[1024];
			while((count = inputStream.read(data)) != -1){
				System.out.println("count = " + Integer.toString(count));
	        	String msg = new String(data, 0, count);
	        	System.out.println("msg = " + msg);
	        	if(msg.substring(0, 2).equals(RemoteBTServer.PREFIX_INIT)){		// msg with number of slides
	        		String t = msg.substring(2, count);
	        		System.out.println("t = " + t);
	        		slidesNumber = Integer.parseInt(t);
	        		System.out.println("SN = " + Integer.toString(slidesNumber));
	        		break;
	        	}
			}
			data = null;
			count = 0;
			// send response
			responseStream.write(READ_NUMBER_RESPONSE);
	        responseStream.flush();
			this.setChanged();
			this.notifyObservers(RemoteBTServer.LOADING_FILES_NOTIFY);
	        for(int i=0; i<slidesNumber; ++i){
	        	FileOutputStream fos = null;
	        	data = null;
				String fileName = RemoteBTServer.PATH_TO_SLIDES + "slide" + Integer.toString(i) + ".png";
				File f = new File(fileName);
				data = new byte[65536];
				fos = new FileOutputStream(f);
				while((count = inputStream.read(data)) != -1){
					byte[] twoLastBytes = new byte[2];
					twoLastBytes = Arrays.copyOfRange(data, count-2, count);
		        	String msg = new String(twoLastBytes, 0, 2);
		        	if(msg.equals(RemoteBTServer.PREFIX_EOF)){		// end of file
		        		System.out.println("End of file transfer! " + i);
		        		fos.write(data, 0, count-2);
		        		fos.close();
		        		break;
		        	}
		        	else{
		        		fos.write(data, 0, count);
		        	}
				}
				responseStream.write(READ_FILE_RESPONSE);
		        responseStream.flush();
		        System.out.println("Response sent!" + i);
	        }
		}
		catch (Exception e) {
    		e.printStackTrace();
    	}
		System.out.println("All files received!");

		// now inform parent thread
		this.setChanged();
		this.notifyObservers(RemoteBTServer.FILES_RECEIVED_NOTIFY);
		
		processCommands();
	}
	
	public void processCommands(){
		int count = 0;
		boolean end = false;
		try {
			data = null;
			while(!end){
				data = new byte[1024];
				byte[] temp = null;
				while((count = inputStream.read(data)) != -1){	
					System.out.println("bb " + count);
					if(count < 3){
						//responseStream.write(READ_COMMAND_NOT_OK_RESPONSE);
				        //responseStream.flush();
				        //data = null;
				        //break;
				        continue;
					}
					System.out.println("count = " + Integer.toString(count));
		        	String msg = new String(data, 0, count);
		        	System.out.println("msg = " + msg);
		        	if(msg.substring(0, 2).equals(RemoteBTServer.PREFIX_SLIDE)){		// msg with commands
		        		String t = msg.substring(2, count);
		        		System.out.println("t = " + t);
		        		if(t.length() == 0){	// error
		        			responseStream.write(READ_COMMAND_NOT_OK_RESPONSE);
					        responseStream.flush();
					        data = null;
					        break;
		        		}
		        		int command = Integer.parseInt(t);
		        		
		        		
		        		// check which command and pass it 
		        		if(command == RemoteBTServer.NEXT_SLIDE_NOTIFY){
		        			this.setChanged();
		        			this.notifyObservers(RemoteBTServer.NEXT_SLIDE_NOTIFY);
		        		}
		        		else if(command == RemoteBTServer.PREV_SLIDE_NOTIFY){
		        			this.setChanged();
		        			this.notifyObservers(RemoteBTServer.PREV_SLIDE_NOTIFY);
		        		}
		        		else if(command == RemoteBTServer.FIRST_SLIDE_NOTIFY){
		        			this.setChanged();
		        			this.notifyObservers(RemoteBTServer.FIRST_SLIDE_NOTIFY);
		        		}
		        		else if(command == RemoteBTServer.LAST_SLIDE_NOTIFY){
		        			this.setChanged();
		        			this.notifyObservers(RemoteBTServer.LAST_SLIDE_NOTIFY);
		        		}
		        		else{
		        			throw new IOException("Unrecognised command!");
		        		}
		        		responseStream.write(READ_COMMAND_OK_RESPONSE);
				        responseStream.flush();
		        		break;
		        	}
		        	else if(msg.substring(0, 2).equals(RemoteBTServer.EXIT)){		// exit
		        		end = true;
		        		break;
		        	}
		        	else{
		        		throw new IOException("Unrecognised message prefix!");
		        	}
				}
				data = null;
				count = 0;
			}			
		} catch (IOException e){ e.printStackTrace(); }
	}

}
