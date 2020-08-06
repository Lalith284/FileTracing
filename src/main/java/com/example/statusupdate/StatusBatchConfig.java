package com.example.statusupdate;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.statusupdate.listener.DbConfig;
import com.example.statusupdate.listener.JobListener;
//import com.example.statusupdate.listener.DbConfig;
//import com.example.statusupdate.listener.ReaderListener;
import com.example.statusupdate.listener.StepListener;
import com.example.statusupdate.listener.StepTwoListener;
import com.example.statusupdate.listener.StepZeroListener;

@Configuration
@ComponentScan
public class StatusBatchConfig {

	@Autowired
	private DbConfig dbConfig;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, ItemWriter<Sales> itemWritter,
			ItemProcessor<Sales, Sales> itemProcessor, ItemReader<Sales> itemReader)

	{
		Step step1 = stepBuilderFactory.get("CSV-FILE-LOAD").<Sales, Sales>chunk(1).reader(itemReader)
				.processor(processor()).writer(itemWritter).listener(stepListener())
				// .taskExecutor(taskExecutor())
				// .listener(promotionListener())
				.build();

		return jobBuilderFactory.get("CSV-LOAD").incrementer(new RunIdIncrementer()).listener(jobListener(jdbcTemplate))
				.flow(step()).on("*").to(step1).on("*").to(step2()).end().build();

	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("Initial-Status-Update").tasklet(new InitialUpdateTable(jdbcTemplate))
				.listener(stepZeroListener()).build();
	}

	@Bean
	public Step step2() {

		return stepBuilderFactory.get("Final-Status-Update").tasklet(new UpdateTable(jdbcTemplate))
				.listener(stepTwoListener()).build();
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dbConfig.dataSource());
		return jdbcTemplate;
	}

	@Bean
	public UpdateTable updateTable(JdbcTemplate jdbcTemplate) {
		return new UpdateTable(jdbcTemplate);
	}

	@Bean
	public JobListener jobListener(JdbcTemplate jdbcTemplate) {
		return new JobListener(jdbcTemplate);
	}

	// @Bean
	// public ReaderListener readerListener() {
	// return new ReaderListener();
	// }
	//

	@Bean
	public StepZeroListener stepZeroListener() {
		return new StepZeroListener();
	}

	@Bean
	public StepTwoListener stepTwoListener() {
		return new StepTwoListener();
	}

	@Bean
	public StepListener stepListener() {
		return new StepListener();
	}

	// @Bean
	// public ExecutionContextPromotionListener promotionListener() {
	// ExecutionContextPromotionListener listener = new
	// ExecutionContextPromotionListener();
	// listener.setKeys(new String[] { "StepStatus" });
	// return listener;
	// }

	@Bean
	public ExecutionContext executionContext() {
		return new ExecutionContext();
	}

	// @Bean
	// public StepExecutionContext jobExecutionContext() {
	// return new StepExecutionContext();
	// }

	// @Bean
	// public JobRepository jobRepository() throws Exception {
	// JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
	// factory.setDataSource( dbConfig.newDataSource());
	// factory.setTransactionManager(platformTransactionManager);
	// factory.setValidateTransactionState(true);
	// factory.setIsolationLevelForCreate(ISOLATION_REPEATABLE_READ);
	// // factory.setIncrementerFactory(customIncrementerFactory());
	// factory.afterPropertiesSet();
	// return factory.getObject();
	// }

	@Bean
	public JobRegistry jobRegistry() throws Exception {
		MapJobRegistry jobRegistry;
		return jobRegistry = new MapJobRegistry();
	}

	@Bean
	public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
		SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
		simpleJobLauncher.setJobRepository(jobRepository);
		return simpleJobLauncher;
	}

	// @Bean
	// public JobExplorer jobExplorer() throws Exception {
	// System.err.println("EXPLORER");
	// final JobExplorerFactoryBean bean = new JobExplorerFactoryBean();
	// bean.setDataSource( dbConfig.newDataSource());
	// bean.setTablePrefix("BATCH_");
	// bean.setJdbcOperations(new JdbcTemplate( dbConfig.newDataSource()));
	// bean.afterPropertiesSet();
	// return bean.getObject();
	// }

	@Bean
	public ItemProcessor<Sales, Sales> processor() {
		return new Processor();
	}

	@Bean
	public FlatFileItemReader<Sales> itemReader() {
		FlatFileItemReader<Sales> flatFileItemReader = new FlatFileItemReader<Sales>();
		flatFileItemReader.setResource(
				new FileSystemResource("C:/Users/ELCOT/Desktop/status-update/src/main/resources/sales.csv"));
		flatFileItemReader.setName("CSV_READER");
		flatFileItemReader.setLineMapper(lineMapper());
		return flatFileItemReader;
	}

	@Bean
	public LineMapper<Sales> lineMapper() {
		DefaultLineMapper<Sales> defaultLineMapper = new DefaultLineMapper<Sales>();
		DelimitedLineTokenizer lineTockenizer = new DelimitedLineTokenizer();

		lineTockenizer.setDelimiter(",");
		lineTockenizer.setStrict(false);
		defaultLineMapper.setLineTokenizer(lineTockenizer);
		defaultLineMapper.setFieldSetMapper(new SalesFieldSetMapper());
		return defaultLineMapper;
	}

	@Bean
	public JdbcBatchItemWriter<Sales> itemWriter(DataSource dataSource) {

		return new JdbcBatchItemWriterBuilder<Sales>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Sales>())
				.sql("INSERT INTO salesrecord (orderid,region,country,itemtype,saleschannel,orderpriority,orderdate,shipdate,unitssold,unitprice,unitcost,totalrevenue,totalcost,totalprofit) VALUES (:orderId, :region, :country, :itemType, :salesChannel, :orderPriority, :orderDate, :shipDate, :unitsSold, :unitPrice, :unitCost, :totalRevenue, :totalCost, :totalProfit)")
				.dataSource(dataSource).build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(10);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	// @Bean
	// public TaskExecutor taskExecutor() {
	// SimpleAsyncTaskExecutor asyncTaskExecutor = new
	// SimpleAsyncTaskExecutor("spring_batch");
	// asyncTaskExecutor.setConcurrencyLimit(4);
	// return asyncTaskExecutor;
	// }

}
