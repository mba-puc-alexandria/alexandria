package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.domain.recommendation.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BoundedRecommendationGenerationQueue implements RecommendationGenerationQueue {
  private final RecommendationExecutionResources resources;
  private final Set<Key> enqueued=ConcurrentHashMap.newKeySet();
  public BoundedRecommendationGenerationQueue(RecommendationExecutionResources resources){this.resources=resources;}
  private record Key(long userId,LocalDate date){}
  @Override public OfferResult offer(long userId,LocalDate date,GenerationOrigin origin,Work task){
    var key=new Key(userId,date);
    if(!enqueued.add(key))return OfferResult.ALREADY_RUNNING;
    var work=new QueuedWork(key,task);
    if(resources.submit(work))return OfferResult.ACCEPTED;
    work.close();return OfferResult.FULL;
  }
  private final class QueuedWork implements Runnable,AutoCloseable {
    private final Key key;private final Work work;private final AtomicBoolean closed=new AtomicBoolean();
    QueuedWork(Key key,Work work){this.key=key;this.work=work;}
    @Override public void run(){try{work.run();}finally{close();}}
    @Override public void close(){if(closed.compareAndSet(false,true)){try{work.close();}finally{enqueued.remove(key);}}}
  }
  public int registered(){return enqueued.size();}
}
