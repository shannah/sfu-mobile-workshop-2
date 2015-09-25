/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sfutranslink;

import com.codename1.sfutranslink.TranslinkRESTClient.Stop;
import com.codename1.ui.Button;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.UITimer;

/**
 *
 * @author shannah
 */
public class BusSchedulePreferencesForm extends Form {
    TranslinkRESTClient client;
    Container stopsList;
    UITimer refreshTimer;
    long lastUpdated;
    public BusSchedulePreferencesForm(TranslinkRESTClient client) {
        super("Bus Schedule Preferences");
        this.client = client;
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        
        Container stopsHeader = new Container(new BorderLayout());
        stopsHeader.setUIID("BusSchedulePreferencesHeader");
        Label stopsHeading = new Label("Default Stops");
        stopsHeader.addComponent(BorderLayout.WEST, stopsHeading);
        
        Button addStopButton = new Button("Add");
        addStopButton.addActionListener((evt)->{
            new AddStopForm(client).show();
        });
        stopsHeader.addComponent(BorderLayout.EAST, addStopButton);
        
        addComponent(stopsHeader);
        stopsList = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        addComponent(stopsList);
        Display.getInstance().callSerially(()-> {
            refreshStopsList();
            
        });
        Label routesHeading = new Label("Default Routes");
        addComponent(routesHeading);
        
        final Form currentForm = Display.getInstance().getCurrent();
        setBackCommand(new Command("Back") {
            public void actionPerformed(ActionEvent evt) {
                currentForm.showBack();
            }
        });
        
        this.addShowListener((evt)-> {
            refreshTimer = new UITimer(()->{
            if (client.lastModified > lastUpdated || System.currentTimeMillis()-lastUpdated > 30000) {
                    lastUpdated = System.currentTimeMillis();
                    refreshStopsList();
                }
            });
            refreshTimer.schedule(1000, true, this);
        });
    }

   
    
    
    
    
    public void refreshStopsList() {
        stopsList.removeAll();
        for (Stop stop : client.registeredStops()) {
            CheckBox cb = new CheckBox(stop.toString());
            cb.setUIID("BusScheduleStopsCheckBox");
            cb.setSelected(client.defaultStops().contains(stop));
            stopsList.addComponent(cb);
            cb.addActionListener((evt)->{
                if (cb.isSelected()) {
                    client.addStopToDefaults(stop.getStopNumber());
                } else {
                    client.removeStopFromDefaults(stop.getStopNumber());
                }
            });
        }

        this.revalidate();
        lastUpdated = System.currentTimeMillis();

        
    }
}
