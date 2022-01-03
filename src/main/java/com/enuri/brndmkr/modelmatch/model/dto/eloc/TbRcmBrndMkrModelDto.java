package com.enuri.brndmkr.modelmatch.model.dto.eloc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TbRcmBrndMkrModelDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private int modelNo;
	private int groupModelNo;
	private String modelNm;
	private String cateCd;
	private Integer popularRank;
	private int mkrId;
	private String mkrNm;
	private int brndId;
	private String brndNm;

	private List<TbRcmBrndMkrPlDto> pls = new ArrayList<>();
}