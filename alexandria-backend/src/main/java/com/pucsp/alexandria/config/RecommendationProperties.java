package com.pucsp.alexandria.config;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="recommendations")
public class RecommendationProperties {
  private ZoneId zone=ZoneId.of("America/Sao_Paulo");
  private Duration lease=Duration.ofMinutes(5);
  private Duration heartbeat=Duration.ofSeconds(60);
  private Duration overdueThreshold=Duration.ofMinutes(60);
  private int workers=2;
  private int queueCapacity=100;
  private int pageSize=100;
  @PostConstruct public void validate(){
    if(zone==null||lease==null||lease.isNegative()||lease.isZero()||heartbeat==null
        ||heartbeat.isNegative()||heartbeat.toMillis()<1||lease.toMillis()<1||heartbeat.compareTo(lease)>=0
        ||overdueThreshold==null||overdueThreshold.isNegative()||overdueThreshold.isZero()
        ||workers<1||queueCapacity<1||pageSize<1||pageSize>1000)
      throw new IllegalArgumentException("Invalid recommendations execution properties");
  }
  public ZoneId getZone(){return zone;} public void setZone(ZoneId zone){this.zone=zone;}
  public Duration getLease(){return lease;} public void setLease(Duration value){lease=value;}
  public Duration getHeartbeat(){return heartbeat;} public void setHeartbeat(Duration value){heartbeat=value;}
  public Duration getOverdueThreshold(){return overdueThreshold;} public void setOverdueThreshold(Duration value){overdueThreshold=value;}
  public int getWorkers(){return workers;} public void setWorkers(int value){workers=value;}
  public int getQueueCapacity(){return queueCapacity;} public void setQueueCapacity(int value){queueCapacity=value;}
  public int getPageSize(){return pageSize;} public void setPageSize(int value){pageSize=value;}
}
