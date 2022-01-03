package com.enuri.brndmkr.modelmatch.model.dto.eloc;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TbRcmBrndMkrPlDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private long plNo;
	private String goodsNm;
	private int modelNo;
	private String brndMkrKeyword;
	private String cateCd;

}