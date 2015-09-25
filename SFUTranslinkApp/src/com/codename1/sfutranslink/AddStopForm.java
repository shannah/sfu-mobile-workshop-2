/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sfutranslink;

import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.sfutranslink.TranslinkRESTClient.Stop;
import com.codename1.ui.Button;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Command;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BoxLayout;
import java.util.List;

/**
 *
 * @author shannah
 */
public class AddStopForm extends Form {
    TranslinkRESTClient client;
    public AddStopForm(TranslinkRESTClient client) {
        super("Add Stop");
        //setUIID("AddStopForm");
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        Display.getInstance().callSerially(()-> {
            
            //Location loc = LocationManager.getLocationManager().getCurrentLocationSync();
            Location loc = null;
            
            List<Stop> stops = null;
            if (loc != null) {
                stops = client.findStopsInRadius(loc.getLatitude(), loc.getLongitude(), 1000);
            } else {
                stops = client.findStopsAtSFU();
            }
            System.out.println("Stops are "+stops);
            for (Stop stop : stops) {
                if (client.defaultStops().contains(stop)) {
                    continue;
                }
                Button b = new Button(stop.toString());
                b.addActionListener((evt)-> {
                    client.addStopToDefaults(stop.getStopNumber());
                    this.getBackCommand().actionPerformed(evt);
                });
                addComponent(b);
            }
            animateLayout(100);
        });
        
        final Form currentForm = Display.getInstance().getCurrent();
        setBackCommand(new Command("Back") {
            public void actionPerformed(ActionEvent evt) {
                currentForm.showBack();
            }
        });
    }
}
