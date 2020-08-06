package com.example.statusupdate;

import org.springframework.batch.item.ItemProcessor;

public class Processor implements ItemProcessor<Sales, Sales> {

	public Sales process(Sales sales) throws Exception {
		// k++;
		// if(k==20000) {
		// throw new Exception("Exception created and program stopped");
		// }
		sales.setCountry("INDIA");
		return sales;

	}

}
