package com.example.statusupdate;

import java.sql.Types;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

public class UpdateTable implements Tasklet {

	private static final String sql = "UPDATE tracking SET status = ? WHERE filename= ?";

	private JdbcTemplate jdbcTemplate;

	public UpdateTable(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Bean
	public RepeatStatus execute(StepContribution arg0, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub

		StepContext stepContext = chunkContext.getStepContext();
		StepExecution stepExecution = stepContext.getStepExecution();
		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext jobContext = jobExecution.getExecutionContext();

		String status = jobContext.getString("StepStatus");
		String fileName = jobContext.getString("FileName");

		Object[] params = { status, fileName };
		int[] types = { Types.VARCHAR, Types.VARCHAR };
		int count = jdbcTemplate.update(sql, params, types);

		// System.err.println("Count " + count);

		return RepeatStatus.FINISHED;

	}

}
