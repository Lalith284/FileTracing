package com.example.statusupdate.listener;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

public class StepZeroListener implements StepExecutionListener {

	public ExitStatus afterStep(StepExecution arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub

		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		String fileName = "sales";
		jobContext.putString("FileName", fileName);

	}

}
