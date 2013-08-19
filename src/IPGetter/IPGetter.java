package IPGetter;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class IPGetter {
  
  private static final URL IP_CHECK_URL =
      makeURL("http://ipchk.sourceforge.net/rawip/");
  
  private static final int QUICK_INTERVAL = 5 * 1000;
  private static final int SLOW_INTERVAL  = 5 * 60 * 1000;
  
  private String currentIP;
  private TrayIcon trayIcon;
  private Timer quickTimer = null;
  
  public IPGetter() {
    currentIP = getIPAddress();
  }
  
  private static URL makeURL(String URLString) {
    
    try {
      
      return new URL(URLString);
      
    } catch (MalformedURLException e) {
      
      e.printStackTrace();
      return null;
      
    }
    
  }
  
  private String getIPAddress() {
    
    try (BufferedReader in = new BufferedReader(new InputStreamReader(
        IP_CHECK_URL.openStream()));) {
      
      return in.readLine();
      
    } catch (Exception e) {
      
      e.printStackTrace();
      quickTimer = constructTimer(QUICK_INTERVAL);      
      return "<unknown>";
      
    }
     
  }
  
  private void displayMessage(String message, TrayIcon.MessageType messageType) {
    trayIcon.displayMessage("IPGetter", message + currentIP, messageType);
  }
  
  private void displayUpdateMessage(boolean onlyWhenChanged) {
    
    String newIP = getIPAddress();
    
    if (!currentIP.equals(newIP)) {
      
      currentIP = newIP;
      displayMessage("New IP: ", TrayIcon.MessageType.INFO);
      
      if (quickTimer != null && !currentIP.equals("<unknown>")) {
        quickTimer.cancel();
      }
      
    } else if (!onlyWhenChanged)
      displayMessage("IP not changed: ", TrayIcon.MessageType.INFO);
    
  }
  
  private void copyIPToClipboard() {
    
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
        new StringSelection(currentIP), null);
    
  }
  
  private void createAndShowGUI() {
    
    //Check the SystemTray support
    if (!SystemTray.isSupported()) {
        System.out.println("SystemTray is not supported");
        return;
    }
    
    PopupMenu popupMenu= new PopupMenu();
    
    MenuItem copyItem = new MenuItem("Copy IP to clipboard");
    MenuItem updateItem = new MenuItem("Update now");
    MenuItem exitItem = new MenuItem("Exit");
    
    popupMenu.add(copyItem);
    popupMenu.addSeparator();
    popupMenu.add(updateItem);
    popupMenu.add(exitItem);
    
    trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage((
        getClass().getResource("/icon.png"))), "IPGetter", popupMenu);
    
    try {
      SystemTray.getSystemTray().add(trayIcon);
    } catch (AWTException e) {
      e.printStackTrace();
    }
    
    trayIcon.setImageAutoSize(true);
    trayIcon.setToolTip("Current IP: " + currentIP);
    trayIcon.displayMessage("IPGetter", "Current IP: " + currentIP,
        TrayIcon.MessageType.INFO);
    
    trayIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON1) {
              copyIPToClipboard();
          }
      }
    });
    
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyIPToClipboard();
      }
    });
    
    updateItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        displayUpdateMessage(false);
      }
    });
    
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          SystemTray.getSystemTray().remove(trayIcon);
          System.exit(0);
      }
    });
  }
  
  private Timer constructTimer(int intervalTime) {
    
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          displayUpdateMessage(true);
        }
    }, intervalTime, intervalTime);
    return timer;
    
  }

  public static void main(String[] args) {
    
    final IPGetter ipGetter = new IPGetter();  
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ipGetter.createAndShowGUI();
        ipGetter.constructTimer(SLOW_INTERVAL);
      }
    });
    
  }

}