/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.sfutranslink;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.Externalizable;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Form;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;

/**
 *
 * @author shannah
 */
public class TranslinkRESTClient {
    private String apiKey="gtjVvjeLK0JCktGRtNBR";
    private String baseUrl="http://api.translink.ca";
    private String getRTTIUrl() {
        return baseUrl + "/RTTIAPI/V1/";
    }
    
    long lastModified;
    
    public long getLastModified() {
        return lastModified;
    }
    
    public void setModified() {
        lastModified = System.currentTimeMillis();
    }
    
    private Set<Route> defaultRoutes;
    private Set<Stop> defaultStops;
    private Set<Stop> registeredStops;
    
    /**
     * Gets the set of routes that are used to filter find requests if no route is
     * specifically set.
     * @return The set of routes that is used for queries.
     */
    public Set<Route> defaultRoutes() {
        if (defaultStops == null) {
            
            // Check the cache
            Storage s = Storage.getInstance();
            if (s.exists("TranslinkRESTClient.defaultRoutes")) {
                List<Route> routes = (List<Route>)s.readObject("TranslinkRESTClient.defaultRoutes");
                defaultRoutes = new HashSet<Route>();
                defaultRoutes.addAll(routes);
                return defaultRoutes;
            }
            
            List<Route> res = findRoutesAtSFU();
            defaultRoutes = new HashSet<Route>();
            defaultRoutes.addAll(res);
            s.writeObject("TranslinkRESTClient.defaultRoutes", defaultRoutes);
        }
        return defaultRoutes;
    }
    
    /**
     * Gets the set of stops that are registered in the app.  These stops are not
     * used in any queries, but this can be used as a set of options for the user
     * to enable/disable to add to the defaultStops().
     * @return The set of registered stops.
     */
    public Set<Stop> registeredStops() {
        if (registeredStops == null) {
            
            // Check the cache
            Storage s = Storage.getInstance();
            if (s.exists("TranslinkRESTClient.registeredStops")) {
                List<Stop> stops = (List<Stop>)s.readObject("TranslinkRESTClient.registeredStops");
                registeredStops = new HashSet<Stop>();
                registeredStops.addAll(stops);
                return registeredStops;
            }
            
            List<Stop> res = findStopsAtSFU();
            registeredStops = new HashSet<Stop>();
            registeredStops.addAll(res);
            s.writeObject("TranslinkRESTClient.registeredStops", registeredStops);
        }
        return registeredStops;
    }
    
    /**
     * Gets the set of stops that are used to filter find requests if no stop is
     * specifically set.
     * @return The set of stops that is used for queries.
     */
    public Set<Stop> defaultStops() {
        if (defaultStops == null) {
            
            // Check the cache
            Storage s = Storage.getInstance();
            if (s.exists("TranslinkRESTClient.defaultStops")) {
                List<Stop> stops = (List<Stop>)s.readObject("TranslinkRESTClient.defaultStops");
                defaultStops = new HashSet<Stop>();
                defaultStops.addAll(stops);
                return defaultStops;
            }
            
            Set<Stop> res = registeredStops();
            defaultStops = new HashSet<Stop>();
            defaultStops.addAll(res);
            s.writeObject("TranslinkRESTClient.defaultStops", defaultStops);
        }
        return defaultStops;
    }
    
    
    /**
     * Adds a stop to the set of default stops so that it will be included in 
     * queries.
     * @param stopId The Stop Number
     */
    public void addStopToDefaults(String stopId) {
        Stop stop = new Stop();
        stop.setStopNumber(stopId);
        if (!defaultStops.contains(stop)) {
            stop = findStop(stopId);
            if (stop != null) {
                defaultStops.add(stop);
                List<Stop> stopList = new ArrayList<Stop>();
                stopList.addAll(defaultStops);
                Storage.getInstance().writeObject("TranslinkRESTClient.defaultStops", stopList);
                setModified();
            }
        }
    }
    
    /**
     * Removes a stop from the set of default stops so that it will no longer
     * be included in queries.
     * 
     * 
     * @param stopId The Stop number.
     */
    public void removeStopFromDefaults(String stopId) {
        Stop stop = new Stop();
        stop.setStopNumber(stopId);
        if (defaultStops.contains(stop)) {
            defaultStops.remove(stop);
            List<Stop> stopList = new ArrayList<Stop>();
            stopList.addAll(defaultStops);
            Storage.getInstance().writeObject("TranslinkRESTClient.defaultStops", stopList);
            setModified();
        }
    }
    
    /**
     * Adds a stop to the set of registered stops.
     * @param stopId The stop number.
     */
    public void addStopToRegistered(String stopId) {
        Stop stop = new Stop();
        stop.setStopNumber(stopId);
        if (!registeredStops.contains(stop)) {
            stop = findStop(stopId);
            if (stop != null) {
                registeredStops.add(stop);
                List<Stop> stopList = new ArrayList<Stop>();
                stopList.addAll(registeredStops);
                Storage.getInstance().writeObject("TranslinkRESTClient.registeredStops", stopList);
                setModified();
            }
        }
        
    }
    
    public void removeStopFromRegistered(String stopId) {
        Stop stop = new Stop();
        stop.setStopNumber(stopId);
        if (registeredStops.contains(stop)) {
            registeredStops.remove(stop);
            List<Stop> stopList = new ArrayList<Stop>();
            stopList.addAll(registeredStops);
            Storage.getInstance().writeObject("TranslinkRESTClient.registeredStops", stopList);
            setModified();
        }
    }
    
    
    
    public Stop findStop(String stopId) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(getRTTIUrl()+"stops/"+stopId);
        initConnection(req);
        
        Map m = getResultAsMap(req);
        if (m != null) {
            Stop out = new Stop();
            out.setValues(m);
            return out;
        }
        
        return null;
    }
    private void initConnection(ConnectionRequest req) {
        req.setPost(false);
        req.setHttpMethod("GET");
        req.addRequestHeader("Accept", "application/json");
        req.addArgument("apiKey", apiKey);
        req.setDuplicateSupported(true);
        
        
    }
    
    private String getResultAsString(ConnectionRequest req) {
        NetworkManager.getInstance().addToQueueAndWait(req);
        try {
            return new String(req.getResponseData(), "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    private List<Map> getResultAsList(ConnectionRequest req) {
        NetworkManager.getInstance().addToQueueAndWait(req);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(req.getResponseData());
            InputStreamReader reader = new InputStreamReader(bais);
            return (List<Map>)((new JSONParser()).parseJSON(reader).get("root"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<Map>();
        }
    }
    
    private Map getResultAsMap(ConnectionRequest req) {
        NetworkManager.getInstance().addToQueueAndWait(req);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(req.getResponseData());
            InputStreamReader reader = new InputStreamReader(bais);
            return (Map)new JSONParser().parseJSON(reader);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    private List<Map> findStopsInRadius_(double lat, double lng, int radiusMetres) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(getRTTIUrl()+"stops");
        initConnection(req);
        req.addArgument("lat", String.valueOf(lat));
        req.addArgument("long", String.valueOf(lng));
        req.addArgument("radius", String.valueOf(radiusMetres));
        return getResultAsList(req);
    }
    
    private List<Map> findStopsAtSFU_() {
        List<Map> out = findStopsInRadius_(49.276883, -122.914845, 500); // SFU Burnaby
        //out.addAll(findStopsInRadius_(49.1875589, -122.8495448, 500));
        return out;
    }
    
    private List<Map> findRoutesAtStop_(String stopId) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(getRTTIUrl()+"routes");
        initConnection(req);
        req.addArgument("StopNo", stopId);
        return getResultAsList(req);
    }
    
    public List<Route> findRoutesAtStop(String stopId) {
        List<Map> res = findRoutesAtStop_(stopId);
        ArrayList<Route> out = new ArrayList<Route>();
        for (Map m : res) {
            Route r = new Route();
            r.setValues(m);
            out.add(r);
        }
        return out;
    }
    
    public List<Route> findRoutesAtSFU() {
        Set<Route> out = new HashSet<Route>();
        List<Stop> stops = findStopsAtSFU();
        for (Stop stop : stops) {
            List<Route> vals = findRoutesAtStop(stop.getStopNumber());
            for (Route val : vals) {
                out.add(val);
            }
        }
        ArrayList<Route> outList = new ArrayList<Route>();
        outList.addAll(out);
        return outList;
    }
    
    public List<Stop> findStopsAtSFU() {
        List<Map> res = findStopsAtSFU_();
        ArrayList<Stop> out = new ArrayList<Stop>();
        for (Map vals : res) {
            Stop stop = new Stop();
            stop.setValues(vals);
            out.add(stop);
        }
        return out;
    }
    
    public List<Stop> findStopsInRadius(double lat, double lng, int radiusMetres) {
        List<Map> res = findStopsInRadius_(lat, lng, radiusMetres);
        ArrayList<Stop> out = new ArrayList<Stop>();
        for (Map vals : res) {
            Stop stop = new Stop();
            stop.setValues(vals);
            out.add(stop);
        }
        return out;
    }
    
    private List<Map> findNextBusesForStop(String stopNumber, Integer count, Integer timeFrameMinutes) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(getRTTIUrl()+"stops/"+stopNumber+"/estimates");
        initConnection(req);
        if (count != null) {
            req.addArgument("count", String.valueOf(count));
        }
        if (timeFrameMinutes != null) {
            req.addArgument("timeFrame", String.valueOf(timeFrameMinutes));
        }
        //String out = getResultAsString(req);
        //System.out.println(out);
        return getResultAsList(req);
    }
    
    private List<Map> findNextBuses_(Integer count, Integer timeFrameMinutes) {
        Set<Stop> stopsList = defaultStops();
        
        List<Map> stops = new ArrayList<Map>();
        for (Stop s : stopsList) {
            stops.add(s.getValues());
        }
        List<Map> out = new ArrayList<Map>();
        System.out.println(stops);
        for (Map stop: stops) {
            List<Map> res = findNextBusesForStop(String.valueOf((int)(double)stop.get("StopNo")), count, timeFrameMinutes);
            for (Map m : res) {
                m.putAll(stop);
            }
            if (res != null) {
                out.addAll(res);
            }
        }
        
        return out;
    }
    
    public List<ScheduleItem> findNextBuses(Integer count, Integer timeFrameMinutes) {
        List<Map> res = findNextBuses_(count, timeFrameMinutes);
        List<ScheduleItem> out = new ArrayList<ScheduleItem>();
        for (Map item : res) {
            Stop stop = new Stop();
            stop.setValues(item);
            
            Route route = new Route();
            route.setValues(item);
            
            List<Map> schedules = (List<Map>)item.get("Schedules");
            
            for (Map schedItemMap : schedules) {
                ScheduleItem scheduleItem = new ScheduleItem();
                scheduleItem.setStop(stop);
                scheduleItem.setRoute(route);
                scheduleItem.setValues(schedItemMap);
                out.add(scheduleItem);
            }
        }
        Collections.sort(out, (a,b)-> {
            return (int)(a.getExpectedLeaveTime().getTime() - b.getExpectedLeaveTime().getTime());
        });
        //out.sort((a, b)-> {
        //    
        //    return -1;//
        //    
        //});
        
        return out;
        
    }
    
    
    
    public class ScheduleItem {
        private Route route;
        private Stop stop;
        private Date expectedLeaveTime;

        @Override
        public String toString() {
            return "Stop "+stop+" for route "+route+" Expected Leave time "+expectedLeaveTime;
        }
        
        
        
        private void setValues(Map vals) {
            try {
                Date now = new Date();
                String timeStr = (String)vals.get("ExpectedLeaveTime");
                if (timeStr != null) {
                    int colonPos = timeStr.indexOf(":");
                    int hours = Integer.parseInt(timeStr.substring(0, colonPos));
                    int minutes = Integer.parseInt(timeStr.substring(colonPos+1,colonPos+3));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    cal.set(Calendar.HOUR, hours);
                    cal.set(Calendar.MINUTE, minutes);
                    cal.set(Calendar.AM_PM, timeStr.indexOf("am") != -1 ? Calendar.AM : Calendar.PM);
                    expectedLeaveTime = cal.getTime();
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                
                System.out.println((String)vals.get("ExpectedLeaveTime"));
                throw new RuntimeException(ex.getMessage());
            }
        }

        /**
         * @return the route
         */
        public Route getRoute() {
            return route;
        }

        /**
         * @param route the route to set
         */
        public void setRoute(Route route) {
            this.route = route;
        }

        /**
         * @return the stop
         */
        public Stop getStop() {
            return stop;
        }

        /**
         * @param stop the stop to set
         */
        public void setStop(Stop stop) {
            this.stop = stop;
        }

        /**
         * @return the expectedLeaveTime
         */
        public Date getExpectedLeaveTime() {
            return expectedLeaveTime;
        }

        /**
         * @param expectedLeaveTime the expectedLeaveTime to set
         */
        public void setExpectedLeaveTime(Date expectedLeaveTime) {
            this.expectedLeaveTime = expectedLeaveTime;
        }
        
        
        public int getMinutesUntilExpectedLeaveTime() {
            long timeTilLeave = this.getExpectedLeaveTime().getTime() - System.currentTimeMillis();
            
            return (int)(timeTilLeave / 60000l);
        }
        
    }
    
    
    public static class Route implements Externalizable {
        private String routeNumber;
        private String destination;

        @Override
        public String toString() {
            return routeNumber +" to "+ destination;
        }
        
        
        
        private void setValues(Map vals) {
            routeNumber = vals.containsKey("routeNumber") ? (String)vals.get("routeNumber") : (String)vals.get("RouteNo");
            if (vals.containsKey("destination")) {
                destination = (String)vals.get("destination");
            } else {
                if (vals.containsKey("Schedules")) {
                    List<Map> schedules = (List<Map>)vals.get("Schedules");
                    if (!schedules.isEmpty()) {
                        destination = (String)schedules.get(0).get("Destination");
                    }
                }
            }
        }

        /**
         * @return the routeNumber
         */
        public String getRouteNumber() {
            return routeNumber;
        }

        /**
         * @param routeNumber the routeNumber to set
         */
        public void setRouteNumber(String routeNumber) {
            this.routeNumber = routeNumber;
        }

        /**
         * @return the destination
         */
        public String getDestination() {
            return destination;
        }

        /**
         * @param destination the destination to set
         */
        public void setDestination(String destination) {
            this.destination = destination;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public void externalize(DataOutputStream out) throws IOException {
            Map m = new HashMap();
            m.put("destination", destination);
            m.put("routeNumber", routeNumber);
            Util.writeObject(m, out);
        }

        @Override
        public void internalize(int version, DataInputStream in) throws IOException {
            Map m = (Map)Util.readObject(in);
            setValues(m);
        }

        @Override
        public String getObjectId() {
            return "Route";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Route) {
                Route r = (Route)obj;
                return (r.destination == null ? destination == null : r.destination.equals(destination)) && r.routeNumber == routeNumber;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.routeNumber.hashCode();
            hash = 79 * hash + this.destination.hashCode();
            return hash;
        }
        
        
               
    }
    
    public static class Stop implements Externalizable {
        private String stopNumber;
        private String stopName;

        @Override
        public String toString() {
            return stopNumber + " " + stopName;
        }
        
        
        
        private void setValues(Map vals) {
            System.out.println("Vals is "+vals);
            stopNumber = vals.containsKey("stopNumber") ? (String)vals.get("stopNumber") : String.valueOf((int)(double)vals.get("StopNo"));
            stopName = vals.containsKey("stopName") ? (String)vals.get("stopName") : (String)vals.get("Name");
        }

        /**
         * @return the stopNumber
         */
        public String getStopNumber() {
            return stopNumber;
        }

        /**
         * @param stopNumber the stopNumber to set
         */
        public void setStopNumber(String stopNumber) {
            this.stopNumber = stopNumber;
        }

        /**
         * @return the stopName
         */
        public String getStopName() {
            return stopName;
        }

        /**
         * @param stopName the stopName to set
         */
        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        public Map getValues() {
            Map m = new HashMap();
            m.put("stopName", stopName);
            m.put("StopNo", Double.parseDouble(stopNumber));
            m.put("Name", stopName);
            m.put("stopNumber", stopNumber);
            return m;
        }
        
        @Override
        public void externalize(DataOutputStream out) throws IOException {
            
            Util.writeObject(getValues(), out);
        }

        @Override
        public void internalize(int version, DataInputStream in) throws IOException {
            Map m = (Map)Util.readObject(in);
            setValues(m);
        }

        @Override
        public String getObjectId() {
            return "Stop";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Stop) {
                Stop s = (Stop)obj;
                return (s.stopNumber == null ? stopNumber == null : s.stopNumber.equals(stopNumber));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + this.stopNumber.hashCode();
            return hash;
        }
        
        
    }
    
    
}
