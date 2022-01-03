package com.enuri.brndmkr.modelmatch.repository.main.interfaces;

import java.util.List;

import com.enuri.brndmkr.modelmatch.model.dto.main.BrndListDto;

public interface TblEnuriBrandRepositoryCustom {
	List<BrndListDto> findByCateCode(String cateCode);
}