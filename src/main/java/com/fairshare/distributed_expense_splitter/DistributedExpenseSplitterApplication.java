package com.fairshare.distributed_expense_splitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.fairshare.distributed_expense_splitter","org.openapitools.api"})
public class DistributedExpenseSplitterApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedExpenseSplitterApplication.class, args);
	}

}
