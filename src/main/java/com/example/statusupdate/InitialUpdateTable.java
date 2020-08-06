package com.example.statusupdate;

import java.sql.Types;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

public class InitialUpdateTable implements Tasklet {
	
	//Setting as Started in this Tasklet Before our job step starts 

	private static final String sql = "UPDATE tracking SET status = ? WHERE filename= ?";

	private JdbcTemplate jdbcTemplate;

	public InitialUpdateTable(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public RepeatStatus execute(StepContribution arg0, ChunkContext chunkContext) throws Exception {

		StepContext stepContext = chunkContext.getStepContext();
		StepExecution stepExecution = stepContext.getStepExecution();
		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		String fileName = jobContext.getString("FileName");
		String status = "Started";
		Object[] params = { status, fileName };
		int[] types = { Types.VARCHAR, Types.VARCHAR };
		int count = jdbcTemplate.update(sql, params, types);
		System.err.println("InitialCount " + count);

		return RepeatStatus.FINISHED;
	}

}
