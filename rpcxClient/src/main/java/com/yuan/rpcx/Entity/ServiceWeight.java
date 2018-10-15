package com.yuan.rpcx.Entity;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServiceWeight
 *
 * @author yuanqing
 * @create 2018-10-06 下午7:58
 **/
public class ServiceWeight {

    @Getter
    private Service service;

    @Getter
    @Setter
    private AtomicInteger weight ;

    @Getter
    @Setter
    private AtomicInteger effectWeight ;

    @Getter
    @Setter
    private AtomicInteger currentWeight;

    @Getter
    @Setter
    private AtomicInteger successAmt;

    @Getter
    @Setter
    private AtomicInteger failAmt;


    public ServiceWeight(Service service, AtomicInteger weight) {
        this.service = service;
        this.weight = weight;
        this.effectWeight = new AtomicInteger(0);
        this.currentWeight = new AtomicInteger(0);
        this.successAmt = new AtomicInteger(0);
        this.failAmt = new AtomicInteger(0);
    }


    @Override
    public String toString() {
        return "ServiceWeight{" +
                ", service=" + service.toString() +
                ", weight=" + weight.intValue() +
                ", effectWeight=" + effectWeight.intValue() +
                ", currentWeight=" + currentWeight.intValue() +
                ", successAmt=" + successAmt.intValue() +
                ", failAmt='" + failAmt.intValue() + '\'' +
                '}';
    }
}
