package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 같은 패키지(com.example.demo)에 있는 모든 컴포넌트들을 스캔함
public class Main {
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
