package com.example.statusupdate.listener;

import java.math.BigInteger;
import java.sql.Types;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class JobListener extends JobExecutionListenerSupport {
	
    private static final String sql="UPDATE batch_step_execution SET status = 'FAILED' WHERE step_execution_id= ?";
    
	private JdbcTemplate jdbcTemplate;

	public JobListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		String status = jobContext.getString("StepStatus");
		Long failedStepId = jobContext.getLong("FailedStepId");
		//Class.java.lang.Long longFailedStepId=jobContext.getLong("FailesStepId");
		//System.err.println("StringFailedStepId "+failedStepId);
		BigInteger failedStepIdSql = BigInteger.valueOf(failedStepId);
		System.err.println(status);
		if(status=="FAILED") {
			jobExecution.setStatus(BatchStatus.FAILED);
			jobExecution.setExitStatus(ExitStatus.FAILED);
			
			Object[] params = { failedStepId };
			int[] types = { Types.BIGINT};
			int count = jdbcTemplate.update(sql,params, types);
			System.err.println("JOBLISTENER COUNT "+count);
					}
		//System.err.println("JobExecution "+jobExecution);
	}

}
