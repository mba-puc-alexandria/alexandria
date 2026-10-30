package com.pucsp.alexandria.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;
import org.springframework.jdbc.core.JdbcTemplate;

/** Deterministic synthetic data, test classpath only; never callable from a production endpoint. */
public final class RecommendationCapacityFixture {
  public static final int BOOKS=100_000;
  public static final int READERS=1_000;
  public static final int LIBRARY_SIZE=1_000;
  private static final int BATCH_SIZE=500;
  private static final int AUTHORS=100;
  private RecommendationCapacityFixture() {}

  public record SeedSummary(int books,int readers,long libraryLinks,long durationMillis) {}

  public static List<Long> readerIds() {return LongStream.rangeClosed(1,READERS).boxed().toList();}

  public static long bookId(long userId,int slot) {
    if(userId<1||userId>READERS||slot<1||slot>LIBRARY_SIZE)throw new IllegalArgumentException("Invalid capacity fixture coordinates");
    return 1+(((userId-1)*137+slot-1)%BOOKS);
  }

  /** Requires an empty isolated schema. Multi-row prepared batches do not depend on driver rewrite flags. */
  public static SeedSummary seed(JdbcTemplate jdbc) {
    long started=System.nanoTime();
    if(jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM users)+(SELECT COUNT(*) FROM books)",Long.class)!=0L)
      throw new IllegalStateException("Capacity seed requires an empty isolated test schema");
    List<Object[]> rows=new ArrayList<>(BATCH_SIZE);
    for(int id=1;id<=READERS;id++) {
      rows.add(new Object[]{id,"capacity-"+id,"Leitor","Ensaio "+id,"capacity-"+id+"@example.test","test-token-only","USER"});
      flushIfFull(jdbc,"INSERT INTO users(id,username,first_name,last_name,email,password,role) VALUES ",rows);
    }
    flush(jdbc,"INSERT INTO users(id,username,first_name,last_name,email,password,role) VALUES ",rows);
    for(int id=1;id<=AUTHORS;id++)rows.add(new Object[]{id,"Autor de ensaio "+id});
    flush(jdbc,"INSERT INTO authors(id,name) VALUES ",rows);
    for(int id=1;id<=BOOKS;id++) {
      rows.add(new Object[]{id,"Livro de ensaio "+id,"GUTENDEX","https://example.test/capacity/"+id+".epub",
          "Assunto "+(id%40)+"; Geral",id%3==0?"pt,en":id%3==1?"pt":"en",id%10_000});
      flushIfFull(jdbc,"INSERT INTO books(id,title,source,download_url,subjects,languages,download_count) VALUES ",rows);
    }
    flush(jdbc,"INSERT INTO books(id,title,source,download_url,subjects,languages,download_count) VALUES ",rows);
    for(int id=1;id<=BOOKS;id++) {
      rows.add(new Object[]{id,1+(id%AUTHORS)});
      flushIfFull(jdbc,"INSERT INTO book_authors(book_id,author_id) VALUES ",rows);
    }
    flush(jdbc,"INSERT INTO book_authors(book_id,author_id) VALUES ",rows);
    int[] ratings={0,1,3,4,5};
    for(long userId:readerIds()) {
      for(int slot=1;slot<=LIBRARY_SIZE;slot++) {
        String status=slot%4==1?"reading":slot%4==2?"toread":"done";
        Integer rating="done".equals(status)?ratings[slot%ratings.length]:null;
        Integer progress="reading".equals(status)?50:null;
        rows.add(new Object[]{userId,bookId(userId,slot),status,rating,progress});
        flushIfFull(jdbc,"INSERT INTO user_books(user_id,book_id,status,rating,progress) VALUES ",rows);
      }
    }
    flush(jdbc,"INSERT INTO user_books(user_id,book_id,status,rating,progress) VALUES ",rows);
    return new SeedSummary(BOOKS,READERS,(long)READERS*LIBRARY_SIZE,(System.nanoTime()-started)/1_000_000);
  }

  private static void flushIfFull(JdbcTemplate jdbc,String prefix,List<Object[]> rows) {
    if(rows.size()==BATCH_SIZE)flush(jdbc,prefix,rows);
  }
  private static void flush(JdbcTemplate jdbc,String prefix,List<Object[]> rows) {
    if(rows.isEmpty())return;
    String tuple="("+String.join(",",Collections.nCopies(rows.get(0).length,"?"))+")";
    String values=String.join(",",Collections.nCopies(rows.size(),tuple));
    List<Object> arguments=new ArrayList<>(rows.size()*rows.get(0).length);
    for(Object[] row:rows)Collections.addAll(arguments,row);
    jdbc.update(prefix+values,arguments.toArray());rows.clear();
  }
}
