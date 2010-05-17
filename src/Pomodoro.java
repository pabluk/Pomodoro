import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
/*
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
*/

public class Pomodoro extends MIDlet{
	private Display  display;
	private CanvasCounter canvas;
	private static int counter;

	public Pomodoro(){
		display = Display.getDisplay(this);
		canvas  = new CanvasCounter(this);
		counter = 225;
	}

	protected void startApp(){
		display.setCurrent(canvas);
	}

	protected void pauseApp(){
	}

	protected void destroyApp(boolean unconditional){
		notifyDestroyed();
	}

	public void exitMIDlet(){
		destroyApp(true);
	}

    	public void addCounter() {
		counter += 9;
	}

	public void subCounter() {
		counter -= 9;
	}

	public void resetCounter() {
	       	counter = 225;
	}

	public int getCounter() {
		return counter;
	}

	public void vibrate(int duration) {
		display.getDisplay(this).vibrate(duration);
	}

	public void showException(Exception e) {
		Alert a = new Alert("Exception", e.toString(), null, null);
		a.setTimeout(Alert.FOREVER);
		display.setCurrent(a);
	}
}

class CanvasCounter extends Canvas implements CommandListener{
	private Command start;
	private Command stop;
	private Command exit;
	private Pomodoro midlet;
    	private Image image;
	private Image layout;
	private Image pointer;
	private static String msgAction;
	private Timer tm;
	private PomodoroTimer tt;
	private Player p;

	public CanvasCounter(Pomodoro midlet){

		this.midlet = midlet;

		try {
			image = Image.createImage("/background.png");
			layout = Image.createImage("/layout.png");
			pointer = Image.createImage("/pointer.png");
		} catch (Exception e) {}

		String[] types = Manager.getSupportedContentTypes(null);
		String soundfile = "/ring.mp3";
		String soundtype = "audio/mpeg";
		for (int i=0; i<types.length; i++) {
			if (types[i] == "audio/x-wav") {
				soundfile = "/ring.wav";
				soundtype = types[i];
			}
		}

		try {
		    	InputStream in = getClass().getResourceAsStream(soundfile);
			p = Manager.createPlayer(in, soundtype);
			p.realize();
		} catch (Exception e) {
			midlet.showException(e);
			return;
		}

		start = new Command("Start", Command.EXIT, 0);
		stop = new Command("Stop", Command.EXIT, 0);
		exit = new Command("Exit", Command.SCREEN, 2);
		addCommand(start);
		addCommand(exit);
		setCommandListener(this);

	} 
	
	protected void paint(Graphics g){
		showCounterImg(g);
	}

	public void commandAction(Command c, Displayable d){
		msgAction = c.getLabel();
		if (c == exit) {
			setCommandListener(null);
			midlet.exitMIDlet();
		} else if (c == start) {
			startTimer();
			repaint();
		} else if (c == stop) {
			stopTimer();
			repaint();
		}
		//System.out.println(vc.getLevel());
		repaint();
	}

	private void startTimer() {
		tm = new Timer();
		tt = new PomodoroTimer();
		//tm.schedule(tt, 1000, 1000);
		tm.schedule(tt, 60000, 60000);
		removeCommand(start);
		addCommand(stop);
		setCommandListener(this);
	}

	private void stopTimer() {
		tm.cancel();
		midlet.resetCounter();
		removeCommand(stop);
		addCommand(start);
		setCommandListener(this);
		repaint();
	}

	private void showCounterImg(Graphics g) {
		g.setColor(255, 0, 0);
		g.fillRect(0, 0, getWidth(), getHeight());
		//g.setColor(0xffffff);
		//g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
		//g.drawString(Integer.toString(vc.getLevel()), 0, 0, g.LEFT | g.TOP);
		//System.out.println(getWidth());
		//System.out.println(getHeight());
		g.drawImage(layout, (getWidth()/2)-(pointer.getWidth()/2) - midlet.getCounter(), 
                    (getHeight()/2)+(layout.getHeight()/2), Graphics.BOTTOM | Graphics.LEFT);
		g.drawImage(pointer, (getWidth()/2)-(pointer.getWidth()/2), 
                    (getHeight()/2)+(layout.getHeight()/2), Graphics.TOP | Graphics.LEFT);
		g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
	}

	private class PomodoroTimer extends TimerTask {
		public final void run() {
			midlet.subCounter();
			repaint();

			if (midlet.getCounter() == 0) {
				try {
					p.start();
				} catch (Exception e) {
					midlet.showException(e);
					return;
				}
				midlet.vibrate(1300);
				stopTimer();
			}
		}
	}
}
