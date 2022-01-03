package com.enuri.brndmkr.modelmatch.batch.job;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import com.enuri.brndmkr.modelmatch.batch.cach.BrandMakerList;
import com.enuri.brndmkr.modelmatch.batch.item.process.ModelMatchingItemProcessor;
import com.enuri.brndmkr.modelmatch.batch.item.writer.MatchingModelItemWriter;
import com.enuri.brndmkr.modelmatch.model.dto.eloc.TbRcmBrndMkrModelDto;
import com.enuri.brndmkr.modelmatch.model.dto.main.BrndListDto;
import com.enuri.brndmkr.modelmatch.model.dto.main.MkrListDto;
import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularModel;
import com.enuri.brndmkr.modelmatch.repository.eloc.TbRcmBrndMkrModelRepository;
import com.enuri.brndmkr.modelmatch.repository.epmondb.PopularModelRepository;
import com.enuri.brndmkr.modelmatch.repository.epmondb.TbRcmBrndMkrPlRepository;
import com.enuri.gm.common.business.ExKeyWord;
import com.enuri.gm.common.business.StringBusiness;
import com.enuri.gm.common.business.enums.ExFlag;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MatchingJobConfig {

	@Value("${spring.batch.chunk-size}")
	private int chunkSize;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final PopularModelRepository popularModelRepository;
	private final TbRcmBrndMkrModelRepository tbRcmBrndMkrModelRepository;
	private final TbRcmBrndMkrPlRepository tbRcmBrndMkrPlRepository;
	private final MatchingModelItemWriter matchingModelItemWriter;
	private final BrandMakerList brandMakerList;
	private final ModelMatchingItemProcessor modelMatchingItemProcessor;

	@Resource
	private EntityManagerFactory mainEntityManagerFactory;
	@Resource
	private DataSource elocDataSource;
	@Resource
	private DataSource epMonDbDataSource;

	private List<MkrListDto> mkrList;
	private List<BrndListDto> brndList;
	private List<Map<String, Object>> removePromotionList;

	/*
	 * 대카테고리의 제조사(1)/브랜드(N) 정보 추출 (동의어 그룹도 추출) tasklet..
	 * 카테고리별로 제조사/브랜드 불명 유사모델을 조회한다..각 모델별로 매칭된 상품정보도 추출  reader  1:n entity
	 * 	상품명에서 모델명 제거 키워드 규칙 적용(ex_flag = d) process
	 * 	제조사/브랜드 키워드 상품명에서 규칙에 맞게 추출 process
	 *    	상품명에서 규칙에 맞는 키워드를 추출하여 매번마다 14 제조사 리스트에 던진다.
	 *        키워드 추출 규칙 - 1,2번 규칙은 복수개 나올경우 위치상 앞의 단어를 사용
	 *          1. 대괄호-[] 안의 단어 추출
	 *          2. 대괄호 없으면 소괄호-() 안의 단어 추출(띄어쓰기 전부 제거하여 붙임)
	 *          3. 대괄호, 소괄호 없으면 맨앞단어 추출
	 *        14카테 제조사 리스트는 동의어를 전부 포함하여 긴거부터 나열된 리스트 이다.
	 *      제조사를 찾았으면 제조사에 속해있는 브랜드를 전부 긴거부터 나열하여 모델명과 비교한다.
			모델명에 없으면 14카테에서 추출 키워드안의 브랜드를 넣고 이것마저 없으면 브랜드는 비워서 추천테이블에 보여준다.
	 *  가장 많이 매칭되는 추출키워드 확정 process
	 * 제조사가 나오면 id가 낮은 제조사를 우선으로 함 process
	 *  제조사의 브랜드로 모델명에서 비교 process
	 *  모델명에서 추출된 브랜드명으로 제조사/브랜드 ID 추출..브랜드 없으면 제조사로 된 브랜드명으로 ID 추출..없으면 제조사만 추출함 process
	 * 제조사 안나오면 브랜드로 추출한다..복수개면 ID 낮은걸로 제조사/브랜드 추출 process
	 * eloc에서 tbl_mcate_popular_rank 미카테 기준 인기순도 가져와야 한다. process
	 *
	 *
	 * 모델에 매칭된 상품정보 insert.. jdbc 구현한 writer
	 * 모델 정보 QA 테이블에 insert.. jdbcPagingWriter plno, 상품명, 추출 제조사, 브랜드 키워드
	 *   매칭되는 상품수 넣어야함
	 */

	@Bean
	public Job matchingJob() {
		return jobBuilderFactory.get("matchingJob")
				//.preventRestart()
				.start(getBrandMakerListStep(""))
				.next(unknownBrndMkrMatchingStep())
				.build();
	}

	@Bean
	@JobScope
	public Step getBrandMakerListStep(@Value("#{jobParameters[cateCode]}") String cateCode) {
		return stepBuilderFactory.get("getBrandMakerListStep").tasklet((contribution, chunkContext) -> {
			if (cateCode != null) {
				tbRcmBrndMkrPlRepository.deleteByCateCd(StringBusiness.cateCodeTo8Number(cateCode));
				tbRcmBrndMkrModelRepository.deleteByCateCd(StringBusiness.cateCodeTo8Number(cateCode));
				String lCate = cateCode.substring(0, 2) + "%";
				brndList  = brandMakerList.getBrndList(lCate);
				mkrList = brandMakerList.getMkrList(lCate);
				removePromotionList = ExKeyWord.getExKeyWord(cateCode, ExFlag.PROPOTION);
			}

			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean
	@JobScope
	public Step unknownBrndMkrMatchingStep() {
		return stepBuilderFactory.get("unknownBrndMkrMatchingStep")
				.<PopularModel, TbRcmBrndMkrModelDto>chunk(chunkSize)
				.reader(getModelListItemReader(""))	// 카테고리의 불명 유사모델 리스트 추출
				.processor(itemProcessor(""))
				.writer(matchingModelItemWriter)
				.build();
	}

	@Bean
	@StepScope
	public RepositoryItemReader<PopularModel> getModelListItemReader(@Value("#{jobParameters[cateCode]}") String cateCode) {
	    return new RepositoryItemReaderBuilder<PopularModel>()
	        .repository(popularModelRepository)
	        .methodName("findByCaCode")
	        .pageSize(chunkSize)
	        .arguments(StringBusiness.cateCodeTo8Number(cateCode), 2880, 28804)
	        .sorts(Collections.singletonMap("modelNo", Sort.Direction.ASC))
	        .name("getModelListItemReader")
	        .build();
	}

	@Bean
	@StepScope
	public FunctionItemProcessor<PopularModel, TbRcmBrndMkrModelDto> itemProcessor(@Value("#{jobParameters[cateCode]}") String cateCode) {
		modelMatchingItemProcessor.setMkrList(mkrList);
		modelMatchingItemProcessor.setBrndList(brndList);
		modelMatchingItemProcessor.setRemovePromotionList(removePromotionList);
		modelMatchingItemProcessor.setCateCode(cateCode);
		return modelMatchingItemProcessor.functionItemProcessor();
	}
}