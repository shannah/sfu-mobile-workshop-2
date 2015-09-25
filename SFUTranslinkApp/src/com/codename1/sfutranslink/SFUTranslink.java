package com.codename1.sfutranslink;


import com.codename1.io.Util;
import com.codename1.sfutranslink.TranslinkRESTClient.Route;
import com.codename1.sfutranslink.TranslinkRESTClient.Stop;
import com.codename1.ui.Button;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class SFUTranslink {

    private Form current;
    private Resources theme;
    private TranslinkRESTClient client;
    

    public void init(Object context) {
        theme = UIManager.initFirstTheme("/theme");
        client = new TranslinkRESTClient();
        Util.register("Stop", Stop.class);
        Util.register("Route", Route.class);
        // Pro users - uncomment this code to get crash reports sent to you automatically
        /*Display.getInstance().addEdtErrorHandler(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                evt.consume();
                Log.p("Exception in AppName version " + Display.getInstance().getProperty("AppVersion", "Unknown"));
                Log.p("OS " + Display.getInstance().getPlatformName());
                Log.p("Error " + evt.getSource());
                Log.p("Current Form " + Display.getInstance().getCurrent().getName());
                Log.e((Throwable)evt.getSource());
                Log.sendLog();
            }
        });*/
    }
    
    /*public void start() {
        Form f = new Form("Hello World");
        f.setUIID("MyForm");
        f.setLayout(new BorderLayout());
        f.addComponent(BorderLayout.NORTH, new Button("1"));
        f.addComponent(BorderLayout.WEST, new Button("2"));
        f.addComponent(BorderLayout.CENTER, new Button("3"));
        f.addComponent(BorderLayout.EAST, new Button("4"));
        f.addComponent(BorderLayout.SOUTH, new Button("5"));
        f.show();
        
    }*/
    
    public void start() {
        
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("SFU");
        hi.setLayout(new BorderLayout());
        BusScheduleWidget busSchedule = new BusScheduleWidget(client);
        hi.addComponent(BorderLayout.CENTER, busSchedule);
        hi.show();
        
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
