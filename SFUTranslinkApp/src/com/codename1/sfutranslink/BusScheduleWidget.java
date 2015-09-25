/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sfutranslink;

import com.codename1.io.Storage;
import com.codename1.sfutranslink.TranslinkRESTClient.ScheduleItem;
import com.codename1.ui.Button;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shannah
 */
public class BusScheduleWidget extends Container {
    Set<String>stopNumbers;
    Set<String> routeNumbers;
    
    long lastUpdated;
    long updatedTo;
    List<ScheduleItem> schedule;
    private Container scheduleList;
    private String storageKey="BusScheduleWidget.schedule";
    TranslinkRESTClient client;
    
    UITimer refreshTimer;
    
    public BusScheduleWidget(TranslinkRESTClient client) {
        this.client = client;
        setUIID("BusScheduleWidget");
        setLayout(new BorderLayout());
        Container titleBar = new Container();
        titleBar.setUIID("BusScheduleTitleArea");
        titleBar.setLayout(new BorderLayout());
        Label heading = new Label("Buses");
        heading.setUIID("BusScheduleTitle");
        Button editButton = new Button("Edit");
        editButton.addActionListener((evt)->{
            new BusSchedulePreferencesForm(client).show();
        });
        editButton.setUIID("BusScheduleCommand");
        
        titleBar.addComponent(BorderLayout.WEST, heading);
        titleBar.addComponent(BorderLayout.EAST, editButton);
        
        addComponent(BorderLayout.NORTH, titleBar);
        
        scheduleList = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        scheduleList.setUIID("BusScheduleList");
        scheduleList.setScrollableY(true);
        
        addComponent(BorderLayout.CENTER, scheduleList);
        Display.getInstance().callSerially(()->{
            loadSchedule();
        });
        
        
        
        
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Display.getInstance().isEdt();
        if (refreshTimer == null) {
            
            refreshTimer = new UITimer(()->{
                if (client.lastModified > lastUpdated || System.currentTimeMillis()-lastUpdated > 30000) {
                    lastUpdated = System.currentTimeMillis();
                    loadSchedule();
                }
            });
            refreshTimer.schedule(1000, true, Display.getInstance().getCurrent());
        }
    }
    
    
    

    @Override
    public int getPreferredH() {
        return this.getStyle().getFont().getHeight() * 10;
    }
    
    
    
    public void loadSchedule() {
        
        schedule = client.findNextBuses(null, null);
        System.out.println("Loading schedule "+schedule);
        
        scheduleList.removeAll();
        for (ScheduleItem scheduleItem : schedule) {
            Container c = new Container(new BorderLayout());
            c.setUIID("BusScheduleListItem");
            
            Container left = new Container(new BoxLayout(BoxLayout.Y_AXIS));
            
            Label routeLabel = new Label(scheduleItem.getRoute().getRouteNumber() + 
                    " to "+
                    scheduleItem.getRoute().getDestination());
            routeLabel.setUIID("BusScheduleListItemRoute");
            
            left.addComponent(routeLabel);
            
            Label stopLabel = new Label(scheduleItem.getRoute().getRouteNumber() + " " + scheduleItem.getStop().getStopName());
            stopLabel.setUIID("BusScheduleListItemStop");
            left.addComponent(stopLabel);
            
            c.addComponent(BorderLayout.WEST, left);
            
            Label timeLabel = new Label(scheduleItem.getMinutesUntilExpectedLeaveTime() + " min");
            timeLabel.setUIID("BusScheduleListItemTime");
            
            c.addComponent(BorderLayout.EAST, timeLabel);
            
            scheduleList.addComponent(c);
            
                    
        }
        
        scheduleList.revalidate();
        lastUpdated = System.currentTimeMillis();
        
        
        
    }
    
    
    
}
