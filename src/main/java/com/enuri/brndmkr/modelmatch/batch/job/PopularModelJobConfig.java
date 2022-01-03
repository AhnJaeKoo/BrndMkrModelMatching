package com.enuri.brndmkr.modelmatch.batch.job;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularGroupModel;
import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularModel;
import com.enuri.brndmkr.modelmatch.repository.epmondb.PopularGroupModelRepository;
import com.enuri.brndmkr.modelmatch.repository.epmondb.PopularModelRepository;
import com.enuri.brndmkr.modelmatch.repository.main.MainRepository;
import com.enuri.gm.common.util.MapUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PopularModelJobConfig {

	@Value("${spring.batch.chunk-size}")
	private int chunkSize;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final PopularGroupModelRepository popularGroupModelRepository;
	private final PopularModelRepository popularModelRepository;
	private final MainRepository mainRepository;

	@Resource
	private DataSource epMonDbDataSource;

	/*
	 * 초기화 - PopularModel, PopularGroupModel, TblCategory
	 * 서비스 모델 카테고리별로 조회하여 popular_model insert
	 * 그룹모델 조회하여 popular_group_model 에 insert
	 */

	@Bean
	public Job popularModelJob() {
		return jobBuilderFactory.get("popularModelJob")
				.start(clearTableStep(""))	// 테이블 초기화
				.next(setPopularModel())
				.next(setPopularGroupModel())
				.build();
	}

	@Bean
	@JobScope
	public Step clearTableStep(@Value("#{jobParameters[cateCode]}") String cateCode) {
		return stepBuilderFactory.get("clearTableStep").tasklet((contribution, chunkContext) -> {
			popularGroupModelRepository.deleteByCateCdStartingWith(cateCode);
			popularModelRepository.deleteByCateCdStartingWith(cateCode);
			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean
	@JobScope
	public Step setPopularModel() {
		return stepBuilderFactory.get("setPopularModel")
				.<Map<String, Object>, PopularModel>chunk(chunkSize)
				.reader(getCateGoodsItemReader(""))	// 서비스중인 카테고리의 모델 리스트 추출
				.processor(popularModelItemProcessor())
				.writer(popularModelItemWriter())
				.build();
	}

	@Bean
	@JobScope
	public Step setPopularGroupModel() {
		return stepBuilderFactory.get("setPopularGroupModel")
				.<Map<String, Object>, PopularGroupModel>chunk(chunkSize)
				.reader(getPopularGroupModelItemReader())	// 그룹모델 추출
				.processor(popularGroupModelItemProcessor())
				.writer(popularGroupModelItemWriter())
				.build();
	}

	@Bean
	@StepScope
	public RepositoryItemReader<Map<String, Object>> getCateGoodsItemReader(@Value("#{jobParameters[cateCode]}") String cateCode) {
	    return new RepositoryItemReaderBuilder<Map<String, Object>>()
	        .repository(mainRepository)
	        .methodName("findByCateCd")
	        .pageSize(chunkSize)
	        .arguments(cateCode + "%")
	        .sorts(Collections.singletonMap("modelno", Sort.Direction.ASC))
	        .name("getCateGoodsItemReader")
	        .build();
	}

	@Bean
	@StepScope
	public RepositoryItemReader<Map<String, Object>> getPopularGroupModelItemReader() {
	    return new RepositoryItemReaderBuilder<Map<String, Object>>()
	        .repository(popularModelRepository)
	        .methodName("selectPopularGroupModel")
	        .pageSize(chunkSize)
	        .sorts(Collections.singletonMap("model_no", Sort.Direction.ASC))
	        .name("getPopularGroupModelItemReader")
	        .build();
	}

	@Bean
	@StepScope
	public FunctionItemProcessor<Map<String, Object>, PopularModel> popularModelItemProcessor() {
		return new FunctionItemProcessor<> (map ->
			new PopularModel(Integer.parseInt(map.get("modelno").toString()),
					map.get("modelnm").toString(),
					Integer.parseInt(map.get("group_modelno").toString()),
					map.get("ca_code").toString(),
					map.get("constrain").toString(),
					Integer.parseInt(map.get("sum_popular").toString()),
					Integer.parseInt(map.get("enr_mkr_id").toString()),
					Integer.parseInt(map.get("enr_brnd_id").toString()))
		);
	}

	@Bean
	@StepScope
	public FunctionItemProcessor<Map<String, Object>, PopularGroupModel> popularGroupModelItemProcessor() {
		return new FunctionItemProcessor<> (map ->
			new PopularGroupModel(Integer.parseInt(map.get("model_no").toString()),
					map.get("cate_cd").toString(),
					Integer.parseInt(map.get("sum_popular").toString()),
					map.get("popularRank") == null ? null : Integer.parseInt(MapUtil.valueToStringOrEmpty(map, "popularRank")))
		);
	}

	@Bean
	@StepScope
	public JdbcBatchItemWriter<PopularModel> popularModelItemWriter() {
		JdbcBatchItemWriter<PopularModel> itemWriter = new JdbcBatchItemWriterBuilder<PopularModel>()
				.dataSource(epMonDbDataSource)
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()) //entity 자동으로 파라미터로 생성할 수 있는 설정
				.sql("""
						INSERT INTO popular_model
						            (model_no,
						             model_nm,
						             group_model_no,
						             cate_cd,
						             constrain,
						             sum_popular,
						             enr_mkr_id,
						             enr_brnd_id)
						VALUES     (:modelNo,
									:modelNm,
						            :groupModelNo,
						            :cateCd,
						            :constrain,
						            :sumPopular,
						            :enrMkrId,
						            :enrBrndId)
						""")
				.build();
		itemWriter.afterPropertiesSet();
		return itemWriter;
	}

	@Bean
	@StepScope
	public JdbcBatchItemWriter<PopularGroupModel> popularGroupModelItemWriter() {
		JdbcBatchItemWriter<PopularGroupModel> itemWriter = new JdbcBatchItemWriterBuilder<PopularGroupModel>()
				.dataSource(epMonDbDataSource)
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()) //entity 자동으로 파라미터로 생성할 수 있는 설정
				.sql("""
						INSERT INTO popular_group_model
						            (model_no,
						             cate_cd,
						             sum_popular,
						             popular_rank)
						VALUES     (:modelNo,
									:cateCd,
						            :sumPopular,
						            :popularRank)
						""")
				.build();
		itemWriter.afterPropertiesSet();
		return itemWriter;
	}
}