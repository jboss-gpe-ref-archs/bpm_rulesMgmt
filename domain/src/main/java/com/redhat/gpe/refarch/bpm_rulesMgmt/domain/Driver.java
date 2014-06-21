package com.redhat.gpe.refarch.bpm_rulesMgmt.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Driver", propOrder = {
    "driverId",
    "driverName"
})
@XmlRootElement(name="Driver")
public class Driver implements java.io.Serializable{

    private int driverId;
    private String driverName;

    public Driver() {}

    public Driver(int driverId, String driverName){
        this.driverId = driverId;
        this.driverName = driverName;
    }

    public String toString(){
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("driverId = ");
        sBuilder.append(driverId);
        sBuilder.append("\tdriverName = ");
        sBuilder.append(driverName);
        return sBuilder.toString();
    }

    public int getDriverId() {
        return driverId;
    }
    public void setDriverId(int x) {
        driverId = x;
    }
    public String getDriverName() {
        return driverName;
    }
    public void setDriverName(String x) {
        driverName = x;
    }
}
