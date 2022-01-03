package com.enuri.brndmkr.modelmatch.batch.item.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enuri.brndmkr.modelmatch.model.dto.eloc.TbRcmBrndMkrModelDto;
import com.enuri.brndmkr.modelmatch.model.dto.eloc.TbRcmBrndMkrPlDto;
import com.enuri.brndmkr.modelmatch.model.dto.main.BrndListDto;
import com.enuri.brndmkr.modelmatch.model.dto.main.MkrListDto;
import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularGroupModel;
import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularModel;
import com.enuri.brndmkr.modelmatch.repository.epmondb.PopularGroupModelRepository;
import com.enuri.brndmkr.modelmatch.repository.main.TblEnuriBrandRepository;
import com.enuri.brndmkr.modelmatch.repository.main.TblPricelistRepository;
import com.enuri.gm.common.util.ChkNull;
import com.enuri.gm.common.util.ReplaceStr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelMatchingItemProcessor {

	@Value("${spring.batch.chunk-size}")
	private int chunkSize;

	private final TblPricelistRepository tblPricelistRepository;
	private final PopularGroupModelRepository popularGroupModelRepository;
	private final TblEnuriBrandRepository tblEnuriBrandRepository;

	private List<MkrListDto> mkrList;
	private List<BrndListDto> brndList;
	private List<Map<String, Object>> removePromotionList;
	private String cateCode;

	public void setMkrList(List<MkrListDto> mkrList) {
		this.mkrList = mkrList;
	}

	public void setBrndList(List<BrndListDto> brndList) {
		this.brndList = brndList;
	}

	public void setRemovePromotionList(List<Map<String, Object>> removePromotionList) {
		this.removePromotionList = removePromotionList;
	}

	public void setCateCode(String cateCode) {
		this.cateCode = cateCode;
	}

	public FunctionItemProcessor<PopularModel, TbRcmBrndMkrModelDto> functionItemProcessor() {
		AtomicInteger index = new AtomicInteger();

		return new FunctionItemProcessor<> (models -> {
			if (index.incrementAndGet() % (chunkSize / 10) == 0) {
				log.info("ItemProcessor {} - count {}", cateCode, index.get());
			}

			Map<String, Integer> brndMkrKeywordMap = new HashMap<>();
			List<TbRcmBrndMkrPlDto> pls = new ArrayList<>();

			// 모델에 속한 상품의 대표 키워드 추출
			tblPricelistRepository.findByModelnoAndStatus(models.getModelNo(), "0").forEach(p -> {
				//상품명에서 모델명 제거 키워드 규칙 적용(ex_flag = d) process
				String goodsNm = removePromotion(removePromotionList, p.getGoodsnm());
				if (!goodsNm.isEmpty()) {	//상품명이 남아있을때
					String brndMkrKeyword = getBrndMkrKeyword(goodsNm);	// 제조사/브랜드 키워드 추출
					if (brndMkrKeyword.length() > 50) {	//키워드가 50글자 넘으면 안쓴다.
						return;
					}

					brndMkrKeywordMap.put(brndMkrKeyword, brndMkrKeywordMap.getOrDefault(brndMkrKeyword, 0) + 1);	// 기존키가 있을경우 + 1, 없으면 0 + 1

					TbRcmBrndMkrPlDto tbRcmBrndMkrPlDto = new TbRcmBrndMkrPlDto();
					tbRcmBrndMkrPlDto.setPlNo(p.getPlNo());
					tbRcmBrndMkrPlDto.setGoodsNm(p.getGoodsnm().replace("\u0000", ""));
					tbRcmBrndMkrPlDto.setModelNo(p.getModelno());
					tbRcmBrndMkrPlDto.setBrndMkrKeyword(brndMkrKeyword.replace("\u0000", ""));
					tbRcmBrndMkrPlDto.setCateCd(models.getCateCd());
					pls.add(tbRcmBrndMkrPlDto);
				}
			});

			if (pls.isEmpty()) {	//상품이 없으면 종료
				return null;
			}

			Map<String, Integer> sortDescMap = getSortDescMap(brndMkrKeywordMap);	// 내림차순 정렬
			String mostBrndMkrKeyword = getMostBrndMkrKeyword(sortDescMap);	// 가장 많은 수의 키 추출
			return createTbRcmBrndMkrModelDto(models, pls, mostBrndMkrKeyword);	//modelDto 생성
		});
	}

	private String removePromotion(List<Map<String, Object>> removePromotionList, String strGoodsnm) {

		String goodsNm = strGoodsnm; // 원본 상품명
		String bracketGoodsNm = ""; // 괄호 안의 상품명

		try {
			int sIndex = 0; // 괄호 시작위치
			int eIndex = 0; // 괄호 종료위치
			int chkIndex = 0; // 추출한 상품명 안에 괄호 시작위치 확인
			int breakCnt1 = 0; // 무한반복 방지위해
			int breakCnt2 = 0;
			boolean isRemove = false;
			var list = new ArrayList<String[]>();
			String[] arrBracket = { "[", "]" };
			String[] arrParenthesis = { "(", ")" };
			list.add(arrBracket);
			list.add(arrParenthesis);

			// [], () 에 대해서 처리한다.
			for (String[] arr : list) {
				while (true) {
					// 상품명 [] 찾는데 ]가 [보다 뒤에 있는것을 찾는다.
					if (goodsNm.indexOf(arr[0], sIndex) >= 0) {
						if (goodsNm.indexOf(arr[1], sIndex) > goodsNm.indexOf(arr[0], sIndex)) {
							sIndex = goodsNm.indexOf(arr[0], sIndex); // 괄호 시작위치
							eIndex = goodsNm.indexOf(arr[1], sIndex); // 괄호 종료위치

							while (true) {
								// []괄호 안의 글자 담는다.
								bracketGoodsNm = goodsNm.substring(goodsNm.indexOf(arr[0], sIndex) + 1,
										goodsNm.indexOf(arr[1], eIndex));
								// 괄호 안에 괄호가 또 있으면 닫는 괄호도 다음것을 확인한다.
								chkIndex = bracketGoodsNm.indexOf(arr[0], chkIndex);
								if (chkIndex > -1) {
									eIndex = goodsNm.indexOf(arr[1], eIndex + 1); // 괄호 종료위치
									if (eIndex == -1) { // 닫는 괄호가 제대로 안맞으면 해당 제품명은 안쓴다.
										goodsNm = "";
										break;
									}
									chkIndex++;
								} else { // 더이상 괄호가 없으면 break 시킨다.
									chkIndex = 0;
									break;
								}

								if (breakCnt2 > 5) {
									log.warn("무한 반복 상품명 : " + strGoodsnm);
									break;
								} else {
									breakCnt2++;
								}
							}

							for (Map<String, Object> map : removePromotionList) {
								// 1-대괄호, 2-소괄호 같이 한다.
								String exDelimiter = map.get("ex_delimiter").toString();
								String exKeyword = ChkNull.chkStr(map.get("ex_keyword").toString());

								if ("1".equals(exDelimiter) || "2".equals(exDelimiter)) {
									String replaceGoodsNm = bracketGoodsNm.replaceAll(exKeyword, ""); // 괄호안의 글자중 제거
									// 제외키워드로변경된것 or 제외키워드적용될예정 => 걸리는게 있으면 상품명에서 괄호와 안의 내용 전부 삭제한다.
									if (!replaceGoodsNm.equals(bracketGoodsNm) || bracketGoodsNm.matches(exKeyword)) {
										goodsNm = ReplaceStr.replace(goodsNm, arr[0] + bracketGoodsNm + arr[1], "");
										isRemove = true;
										break;
									}
								}
							}

							// 지운게 있으면 ++ 안하고 false 처리만 한다.
							if (isRemove) {
								isRemove = false;
							} else {
								sIndex = ++eIndex;
							}
						} else { // 시작은 있는데 끝이 없으면 잘못된 상품명 이므로 공백처리
							goodsNm = "";
						}
					} else { // 더이상 할게 없으면 종료
						break;
					}

					if (breakCnt1 > 5) {
						log.warn("무한 반복 상품명 : " + strGoodsnm);
						break;
					} else {
						breakCnt1++;
					}
				}
				sIndex = 0;
				eIndex = 0;
			}

			// 3-그외
			if (!"".equals(goodsNm)) {
				for (Map<String, Object> map : removePromotionList) {
					String exDelimiter = map.get("ex_delimiter").toString();
					String exKeyword = ChkNull.chkStr(map.get("ex_keyword").toString());

					if ("3".equals(exDelimiter)) {
						goodsNm = goodsNm.replaceAll(exKeyword, "");
					}
				}
			}

		} catch (Exception e) {
			goodsNm = "";
			log.error("", e);
		}

		goodsNm = goodsNm.trim();
		return goodsNm.trim();
	}

	private String getBrndMkrKeyword(String goodsNm) {
		var keyword = new StringBuilder();

		// 괄호 안의 키 추출 Consumer
		Consumer<String> consumer = (s -> {
			Pattern p = Pattern.compile(s);
			Matcher m = p.matcher(goodsNm);

			while (m.find()) {
				if (keyword.isEmpty()) {
					String str = m.group(1).replaceAll("\\p{Z}", "");

					if (!"".equals(str)) {	//글자가 없으면 다음 괄호를 찾는다.
						keyword.append(m.group(1).replaceAll("\\p{Z}", "")); // 안의 텍스트 공백 전부 제거해야함
						break;
					}
				}
			}
		});

		List.of("\\[(.*?)\\]", "\\((.*?)\\)").forEach(consumer);	//대괄호, 소괄호 순으로 키워드 추출

		// 괄호안에 단업가 없으면 첫번째 단어 사용
		String goods = goodsNm.replaceAll("[\\[\\]\\(\\)\\<\\>\\{\\}\\/\\_\\-]", "").trim();	// 잔여 기호 제거
		return keyword.isEmpty() ? List.of(goods.split(" ")).get(0) : keyword.toString();
	}

	private Map<String, Object> getMakerInfo(String keyword) {
		Map<String, Object> result = new HashMap<>();

		Optional<MkrListDto> opt = mkrList.stream()
				.filter(dto -> keyword.equals(dto.getMakerNm()))
				.findFirst();

		opt.ifPresent(dto -> {
			result.put("maker_nm", dto.getMakerNm());
			result.put("maker_id", dto.getMakerId().intValue());
		});
		return result;
	}

	private int getBrandId(String keyword) {
		AtomicInteger result = new AtomicInteger();
		result.set(0);

		Optional<BrndListDto> opt = brndList.stream()
				.filter(dto -> keyword.equals(dto.getBrandNm()))
				.findFirst();

		opt.ifPresent(dto -> result.set(dto.getBrandId().intValue()));
		return result.get();
	}

	private Map<String, Object> getBrndMkrInfo(String modelNm, Map<String, Object> mkrMap) {
		Map<String, Object> result = new HashMap<>();
		String mkrNm = mkrMap.get("maker_nm").toString();
		int mkrId = ChkNull.chkInt(mkrMap.get("maker_id").toString());

		// 제조사id에 해당되는 브랜드 추출
		List<BrndListDto> brndInfoList = brndList.stream()
				.filter(dto -> dto.getMakerId().intValue() == mkrId)
				.collect(Collectors.toList());

		Consumer<List<Object>> consumer = list -> {
			result.put("brandId", list.get(0));
			result.put("brandNm", list.get(1));
			result.put("makerId", list.get(2));
			result.put("makerNm", list.get(3));
		};

		// 모델명에서 브랜드명 있는것 추출
		brndInfoList.forEach(dto -> {
			Pattern p = Pattern.compile("(?i)" + dto.getBrandNm());
			Matcher m = p.matcher(modelNm);

			while (m.find()) {
				if (result.isEmpty()) {
					consumer.accept(List.of(dto.getBrandId().intValue(), dto.getBrandNm(), mkrId, dto.getMakerNm()));
					break;
				}
			}
		});

		// 브랜드가 검색 안되면 제조사명과 동일한 브랜드를 찾아 ID 넣는다.
		if (result.isEmpty()) {
			Optional<BrndListDto> opt = brndInfoList.stream()
					.filter(dto -> dto.getBrandNm().equals(dto.getMakerNm()))
					.findFirst();

			opt.ifPresent(dto -> consumer.accept(List.of(dto.getBrandId().intValue(), dto.getBrandNm(), mkrId, dto.getMakerNm())));
		}

		// 그래도 브랜드가 없으면 제조사만 넣고 브랜드는 skip
		if (result.isEmpty()) {
			consumer.accept(List.of(0, "", mkrId, mkrNm));
		}

		return result;
	}

	private Map<String, Integer> getSortDescMap(Map<String, Integer> map) {
		return map.entrySet().stream()
			       .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
			       .limit(2)
			       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private String getMostBrndMkrKeyword(Map<String, Integer> map) {
		int firstValue = 0;
		int secondValue = 0;
		String key = "";
		String result = "";

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
		    int value = entry.getValue();

		    if (firstValue == 0) {
		    	firstValue = value;
		    	key = entry.getKey();
		    } else {
		    	secondValue = value;
		    }
		}

		if (firstValue > secondValue) {	// 가장많이 나온 1,2위가 동수가 아니면 1위 키사용
			result = key;
		}

		return result;
	}

	private TbRcmBrndMkrModelDto createTbRcmBrndMkrModelDto(PopularModel models, List<TbRcmBrndMkrPlDto> pls, String mostBrndMkrKeyword) {
		TbRcmBrndMkrModelDto result = null;

		if (!mostBrndMkrKeyword.isEmpty()) {
			Map<String, Object> brndMkrMap = new HashMap<>();
			Map<String, Object> mkrMap = getMakerInfo(mostBrndMkrKeyword);	// 추출된 키워드와 같은 제조사 추출
			int makerId = 0;
			int brandId = 0;
			String makerNm = "";
			String brandNm = "";

			if (!mkrMap.isEmpty()) {	// 제조사 ID가 있을경우 모델명으로 해당 제조사의 브랜드로 검색
				brndMkrMap = getBrndMkrInfo(models.getModelNm(), mkrMap);	// 모델명을 통한 제조사/브랜드 ID 추출

			} else {	// 제조사가 없으면
				int brndId = getBrandId(mostBrndMkrKeyword);	// 추출된 키워드와 같은 브랜드 추출

				if (brndId > 0) {
					brndMkrMap = tblEnuriBrandRepository.findTop1ByBrandId(brndId);
				}
			}

			if (!brndMkrMap.isEmpty()) {
				makerId = ChkNull.chkInt(brndMkrMap.get("makerId").toString());
				brandId = ChkNull.chkInt(brndMkrMap.get("brandId").toString());
				makerNm = brndMkrMap.get("makerNm").toString();
				brandNm = brndMkrMap.get("brandNm").toString();

				result = new TbRcmBrndMkrModelDto();
				result.setModelNo(models.getModelNo());
				result.setModelNm(models.getModelNm());
				result.setCateCd(models.getCateCd());
				result.setPopularRank(getPopularRank(models.getModelNo()));
				result.setGroupModelNo(models.getGroupModelNo());
				result.setMkrId(makerId);
				result.setMkrNm(makerNm);
				result.setBrndId(brandId);
				result.setBrndNm(brandNm);
				result.setPls(pls);
			}
		}

		return result;
	}

	//그룹모델 인기순위
	private Integer getPopularRank(int modelNo) {
		AtomicInteger popularRank = new AtomicInteger(0);	// AtomicInteger은 null 허용이 안되기에 0일때는 null로 결과 전달한다.
		Optional<PopularGroupModel> opt = popularGroupModelRepository.findTop1ById(modelNo);

		opt.ifPresent(p -> {
			if (p.getPopularRank() == null) {
				popularRank.set(0);
			} else {
				popularRank.set(p.getPopularRank());
			}
		});

		return popularRank.get() > 0 ? popularRank.get() : null;
	}
}
