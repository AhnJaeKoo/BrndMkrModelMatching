package com.enuri.brndmkr.modelmatch.repository.main.interfaces;

import java.util.List;

import com.enuri.brndmkr.modelmatch.model.dto.main.MkrListDto;

public interface TblEnuriMakerRepositoryCustom {
	List<MkrListDto> findByCateCode(String cateCode);
}