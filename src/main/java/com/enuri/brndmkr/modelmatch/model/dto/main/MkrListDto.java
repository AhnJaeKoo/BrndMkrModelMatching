package com.enuri.brndmkr.modelmatch.model.dto.main;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MkrListDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private BigDecimal makerId;
	private String makerNm;
	private BigDecimal pEkNo;
	private BigDecimal sort;
}