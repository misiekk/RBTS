package RemoteBT;
import java.lang.Thread;
import java.util.Observable;
import java.util.Observer;

//import java.util.UUID;
import javax.bluetooth.UUID;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;



public class RemoteThread extends Observable implements Runnable, Observer {
	private boolean filesSent = false;
	
	public RemoteThread(Observer o) {
		this.addObserver(o);
	}
	
	@Override
	public void run(){
		prepareConnection();
	}
	
	private void prepareConnection(){
		LocalDevice localDev = null;
		StreamConnection con = null;
		StreamConnectionNotifier not;
		
		try{
			localDev = LocalDevice.getLocalDevice();
			localDev.setDiscoverable(DiscoveryAgent.GIAC);
			UUID uuid = new UUID("1101", true);
			//UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBT";
            not = (StreamConnectionNotifier)Connector.open(url);
		} catch(Exception e){
			e.printStackTrace();
			return;
			}
		
		while(true){
			try{
				System.out.println("AAA");
				con = not.acceptAndOpen();
				System.out.println("BBB");
				Thread conThr = new Thread(new ConnectionThread(con, this, filesSent));
				System.out.println("CCC");
				conThr.start();
				System.out.println("DDD");
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {	
		if((Integer)arg == RemoteBTServer.FILES_RECEIVED_NOTIFY){
			filesSent = true;
			this.setChanged();
			this.notifyObservers(RemoteBTServer.FILES_RECEIVED_NOTIFY);
		}
		else if((Integer)arg == RemoteBTServer.PREV_SLIDE_NOTIFY){
			this.setChanged();
			this.notifyObservers(RemoteBTServer.PREV_SLIDE_NOTIFY);
		}
		else if((Integer)arg == RemoteBTServer.NEXT_SLIDE_NOTIFY){
			this.setChanged();
			this.notifyObservers(RemoteBTServer.NEXT_SLIDE_NOTIFY);
		}	
		else if((Integer)arg == RemoteBTServer.LOADING_FILES_NOTIFY){
			this.setChanged();
			this.notifyObservers(RemoteBTServer.LOADING_FILES_NOTIFY);
		}	
		else if((Integer)arg == RemoteBTServer.FIRST_SLIDE_NOTIFY){
			this.setChanged();
			this.notifyObservers(RemoteBTServer.FIRST_SLIDE_NOTIFY);
		}	
		else if((Integer)arg == RemoteBTServer.LAST_SLIDE_NOTIFY){
			this.setChanged();
			this.notifyObservers(RemoteBTServer.LAST_SLIDE_NOTIFY);
		}	
	}
}
