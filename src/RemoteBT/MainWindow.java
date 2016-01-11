package RemoteBT;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.imageio.*;

public class MainWindow extends JFrame implements Runnable, Observer {
	JLabel textInfo;
	//public ArrayList<ImageIcon> images;
	public Map<Integer, ImageIcon> imagesMap;
	GraphicsDevice gd = null;
	private boolean readyToLoad = false;
	private int actualSlide = 0;
	private int slidesCount = 0;
	private int screenW = 0;
	private int screenH = 0;
	
	public MainWindow(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 500);
		setTitle("EasyPres");
		//this.setExtendedState(MAXIMIZED_BOTH);
		this.getContentPane().setBackground(Color.BLACK);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gd.setFullScreenWindow(this);
		
		
		Thread t = new Thread(new RemoteThread(this));
		t.start();
		textInfo = new JLabel("", SwingConstants.CENTER);
		textInfo.setForeground(Color.WHITE);
		
		textInfo.setText("EasyPres");
		this.add(textInfo);
		
		//images = new ArrayList<ImageIcon>();
		imagesMap = new HashMap<Integer, ImageIcon>();
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.screenH = screen.height;
		this.screenW = screen.width;
		System.out.println("Resolution: " + this.screenW + "x" + this.screenH);
		System.out.println("Resolution2: " + gd.getDisplayMode().getWidth() +"x" + gd.getDisplayMode().getHeight());
	}
	
	public void run(){
		
	}
	
	
	public void clean(){
		File dir = new File(RemoteBTServer.PATH_TO_SLIDES);
		for(File f : dir.listFiles()){
			f.delete();
		}
	}


	public void loadImage(String path){
		String[] tab = {"a.jpg", "b.jpg", "c.jpg"};
		try{
			for(String e : tab){
				String temp = path+e;
				System.out.println(temp);
				File f = new File(temp);
				Image image = ImageIO.read(f);
				ImageIcon icon = new ImageIcon(image);
				
				JLabel label = new JLabel(icon);
				this.add(label);
				pack();
				Thread.sleep(1000);
				this.remove(label);
				//images.add(icon);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void prepareImg() throws IOException{
		File dir = new File(RemoteBTServer.PATH_TO_SLIDES);
		if(dir.exists() && dir.isDirectory()){
			File files[] = dir.listFiles();
						
			for(File f : files){
				//System.out.println(f.getName());
				String temp = f.getName();
				temp = temp.substring(5);	// delete 'slide' from name
				temp = temp.substring(0, temp.length()-4);	// delete extension from name
				
				Image image = ImageIO.read(f);
				BufferedImage bi = new BufferedImage(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight(),
						BufferedImage.TYPE_INT_RGB); 
				Graphics2D g = bi.createGraphics();
				g.drawImage(image, 0, 0, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight(), null);
				
				ImageIcon icon = new ImageIcon(bi);
				//images.add(icon);
				imagesMap.put(Integer.parseInt(temp), icon);
			}
		}
		else{
			throw new IOException("Path doesn't exists!");
		}
		textInfo.setText("Loaded " + imagesMap.size() + " slides!");
		this.slidesCount = imagesMap.size();
		System.out.println("Slides count is " + slidesCount);

		showSlides(actualSlide); // first slide
	}
	
	void showSlides(int param){
		ImageIcon element = imagesMap.get(param);
		textInfo.setText(null);
		textInfo.setIcon(element);
		textInfo.setBounds(0, 0, gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
		pack();
		System.out.println("Showing = " + actualSlide);
	}
		
	@Override
	public void update(Observable o, Object arg) {
		if((Integer)arg == RemoteBTServer.LOADING_FILES_NOTIFY){
			textInfo.setText("Loading slides...");
		}
		else if((Integer)arg == RemoteBTServer.FILES_RECEIVED_NOTIFY){
			textInfo.setText("Files received");
			readyToLoad = true;		
			try {
				prepareImg();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if((Integer)arg == RemoteBTServer.PREV_SLIDE_NOTIFY){
			System.out.println("PREV!");
			if(actualSlide > 0){
				actualSlide--;
				showSlides(actualSlide);
			}
			System.out.println("Actual = " + actualSlide);
		}
		else if((Integer)arg == RemoteBTServer.NEXT_SLIDE_NOTIFY){
			System.out.println("NEXT!");
			if(actualSlide < slidesCount-1){
				actualSlide++;
				showSlides(actualSlide);
			}
			System.out.println("Actual = " + actualSlide);
		}		
		else if((Integer)arg == RemoteBTServer.LAST_SLIDE_NOTIFY){
			System.out.println("LAST!");
			actualSlide = slidesCount-1;
			showSlides(actualSlide);
			System.out.println("Actual = " + actualSlide);
		}		
		else if((Integer)arg == RemoteBTServer.FIRST_SLIDE_NOTIFY){
			System.out.println("FIRST!");
			actualSlide = 0;
			showSlides(actualSlide);
			System.out.println("Actual = " + actualSlide);
		}		
	}	
}
	

