package com.enuri.brndmkr.modelmatch.batch.cach;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.enuri.brndmkr.modelmatch.model.dto.main.BrndListDto;
import com.enuri.brndmkr.modelmatch.model.dto.main.MkrListDto;
import com.enuri.brndmkr.modelmatch.repository.main.TblEnuriBrandRepository;
import com.enuri.brndmkr.modelmatch.repository.main.TblEnuriMakerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrandMakerList {

	private final TblEnuriMakerRepository tblEnuriMakerRepository;
	private final TblEnuriBrandRepository tblEnuriBrandRepository;

	@Cacheable(value = "cateBrndList", key = "#cateCd", unless="#result == \"\"")
	public List<BrndListDto> getBrndList(String cateCd) {
		return tblEnuriBrandRepository.findByCateCode(cateCd);
	}

	@Cacheable(value = "cateMakerList", key = "#cateCd", unless="#result == \"\"")
	public List<MkrListDto> getMkrList(String cateCd) {
		return tblEnuriMakerRepository.findByCateCode(cateCd);
	}
}
