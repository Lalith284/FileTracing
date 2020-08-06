package com.example.statusupdate.listener;

import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

public class StepListener implements StepExecutionListener {

	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		List<Throwable> exceptions = stepExecution.getFailureExceptions();
		String status;
		if (exceptions.isEmpty()) {
			status = "COMPLETED";
		} else {
			status = "FAILED";
			 
		}
		
		
		long failedStepExecution =stepExecution.getId();
		jobContext.putString("StepStatus", status);
		jobContext.putLong("FailedStepId",failedStepExecution);


		return ExitStatus.COMPLETED;
	}

	public void beforeStep(StepExecution arg0) {

	}

}
