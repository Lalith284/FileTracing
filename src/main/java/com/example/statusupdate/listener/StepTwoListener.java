package com.example.statusupdate.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

public class StepTwoListener implements StepExecutionListener{

	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		
		String status = jobContext.getString("StepStatus");
		//System.err.println(status);
		if(status=="FAILED") {
			stepExecution.setStatus(BatchStatus.FAILED);
			stepExecution.setExitStatus(ExitStatus.FAILED);
		}

		return ExitStatus.COMPLETED;
	}

	public void beforeStep(StepExecution arg0) {
		// TODO Auto-generated method stub
		
	}

}
