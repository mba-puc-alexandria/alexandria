package com.pucsp.alexandria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlexandriaApplication {

  public static void main(String[] args) {
    SpringApplication.run(AlexandriaApplication.class, args);
  }

}
