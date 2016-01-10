package RemoteBT;
import java.lang.Thread;



public class RemoteBTServer{
	public static final int FILES_RECEIVED_NOTIFY = 1;
	public static final int ALL_FILES_RECEIVED_NOTIFY = 2;
	public static final int NEXT_SLIDE_NOTIFY = 3;
	public static final int PREV_SLIDE_NOTIFY = 4;
	public static final int FIRST_SLIDE_NOTIFY = 5;
	public static final int LAST_SLIDE_NOTIFY = 6;
	public static final int LOADING_FILES_NOTIFY = 11;
	public static final String EXIT = "#X";
	public static final String PREFIX_SLIDE = "#S";
	public static final String PREFIX_INIT = "#I";
	public static final String PREFIX_EOF = "#E";
	
	
	public static final String PATH_TO_SLIDES = "/home/piotrek/eclipse/workspace/RemoteBTServer/presentation/";
	
	
	
	public static void main(String args[]){
		//Thread t = new Thread(new RemoteThread());
		//Thread t = new Thread(new WifiThread());
		//t.start();		
		MainWindow mw = new MainWindow();
		mw.setVisible(true);
		/*
		String path = "/home/piotrek/eclipse/workspace/RemoteBTServer/res/";
		mw.loadImage(path);*/

	}
}
