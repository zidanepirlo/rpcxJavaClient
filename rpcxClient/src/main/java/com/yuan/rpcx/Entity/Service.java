package com.yuan.rpcx.Entity;

import com.yuan.rpcx.Utils.ZkUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Service
 *
 * @author yuanqing
 * @create 2018-10-03 下午2:27
 **/

//@Setter
//@Getter
public class Service {

    @Getter
    private String IP;

    @Getter
    private String Port;

    @Getter
    private String Protocal;

    @Getter
    private String IpAndPort;

    @Getter
    @Setter
    private AtomicBoolean isAvailable = new AtomicBoolean(false);

    private LongAdder SuccessAmt = new LongAdder();

    private LongAdder FailAmt = new LongAdder();




    public Service(String ip, String port, String protocal) {
        this.IP = ip;
        this.Port = port;
        this.Protocal = protocal;
        this.IpAndPort = ZkUtils.getIpPortKey(ip,port);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Service) {
            Service anotherObj = (Service) obj;
            return this.IP.equals(anotherObj.IP) && this.Port.equals(anotherObj.Port)
                    && this.Protocal.equals(anotherObj.Protocal)&& this.isAvailable.toString().equals(anotherObj.isAvailable.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + IP.hashCode();
        result = 31 * result + Port.hashCode();
        result = 31 * result + Protocal.hashCode();
        result = 31 * result + isAvailable.toString().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Service{" +
//                ", IP=" + IP +
//                ", Port=" + Port +
//                ", Protocal=" + Protocal +
                ", IpAndPort=" + IpAndPort +
                ", isAvailable='" + isAvailable + '\'' +
                '}';
    }

    public void addSuccessAmt(){
        SuccessAmt.increment();
    }

    public void addFailAmt(){
        FailAmt.increment();
    }
}