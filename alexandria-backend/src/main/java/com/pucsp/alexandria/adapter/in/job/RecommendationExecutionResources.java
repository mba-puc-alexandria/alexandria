package com.pucsp.alexandria.adapter.in.job;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Independent worker, discovery, maintenance and heartbeat executors, all owned. */
public final class RecommendationExecutionResources implements AutoCloseable {
  private final ThreadPoolExecutor workers;
  private final ThreadPoolExecutor control;
  private final ThreadPoolExecutor maintenance;
  private final ScheduledThreadPoolExecutor heartbeats;
  private final Set<String> discoveryKeys=ConcurrentHashMap.newKeySet();
  private final Duration shutdownGrace;
  public RecommendationExecutionResources(int workerCount,int queueCapacity){
    this(workerCount,queueCapacity,Duration.ofSeconds(10));
  }
  public RecommendationExecutionResources(int workerCount,int queueCapacity,Duration shutdownGrace){
    this(workerCount,queueCapacity,shutdownGrace,factory("recommendation-worker-"));
  }
  RecommendationExecutionResources(int workerCount,int queueCapacity,Duration shutdownGrace,ThreadFactory workerThreads){
    this.shutdownGrace=shutdownGrace;
    workers=new ThreadPoolExecutor(workerCount,workerCount,0,TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<>(queueCapacity),workerThreads,new ThreadPoolExecutor.AbortPolicy());
    control=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(1),
        factory("recommendation-control-"),new ThreadPoolExecutor.AbortPolicy());
    maintenance=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(1),
        factory("recommendation-maintenance-"),new ThreadPoolExecutor.AbortPolicy());
    heartbeats=new ScheduledThreadPoolExecutor(1,factory("recommendation-heartbeat-"));
    heartbeats.setRemoveOnCancelPolicy(true);
    heartbeats.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    heartbeats.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
  }
  public boolean submit(Runnable task){
    try{workers.execute(task);return true;}catch(RejectedExecutionException ex){return false;}
  }
  public boolean discover(String key,Runnable task){
    return coalesce(control,"discovery:"+key,task);
  }
  public boolean observe(String key,Runnable task){
    return coalesce(maintenance,"maintenance:"+key,task);
  }
  private boolean coalesce(ThreadPoolExecutor executor,String key,Runnable task){
    if(isStopping())return false;
    if(!discoveryKeys.add(key))return true;
    try{executor.execute(()->{
      try{task.run();}
      catch(RuntimeException failure){org.slf4j.LoggerFactory.getLogger(RecommendationExecutionResources.class)
          .warn("Recommendation background operation failed; it will be retried");}
      finally{discoveryKeys.remove(key);}
    });return true;}
    catch(RejectedExecutionException ex){discoveryKeys.remove(key);return false;}
  }
  public ScheduledFuture<?> heartbeat(Runnable task,Duration interval){
    return heartbeats.scheduleAtFixedRate(task,interval.toMillis(),interval.toMillis(),TimeUnit.MILLISECONDS);
  }
  public int queued(){return workers.getQueue().size();}
  public int active(){return workers.getActiveCount();}
  public boolean isStopping(){return workers.isShutdown();}
  private static ThreadFactory factory(String prefix){
    var counter=new AtomicInteger();
    return task->{var thread=new Thread(task,prefix+counter.incrementAndGet());thread.setDaemon(false);return thread;};
  }
  @Override public void close(){
    control.shutdownNow();
    maintenance.shutdownNow();
    workers.shutdown();
    try{if(!workers.awaitTermination(shutdownGrace.toMillis(),TimeUnit.MILLISECONDS))cancelQueued(workers.shutdownNow());}
    catch(InterruptedException ex){cancelQueued(workers.shutdownNow());Thread.currentThread().interrupt();}
    heartbeats.shutdownNow();
    try{control.awaitTermination(2,TimeUnit.SECONDS);maintenance.awaitTermination(2,TimeUnit.SECONDS);workers.awaitTermination(2,TimeUnit.SECONDS);heartbeats.awaitTermination(2,TimeUnit.SECONDS);}
    catch(InterruptedException ex){Thread.currentThread().interrupt();}
  }
  private static void cancelQueued(java.util.List<Runnable> abandoned){
    for(var task:abandoned)if(task instanceof AutoCloseable closeable){
      try{closeable.close();}catch(Exception ignored){/* Each work owns idempotent cleanup. */}
    }
  }
}
