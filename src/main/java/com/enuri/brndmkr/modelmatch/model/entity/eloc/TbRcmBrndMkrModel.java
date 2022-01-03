package com.enuri.brndmkr.modelmatch.model.entity.eloc;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table
@NoArgsConstructor
@AllArgsConstructor
public class TbRcmBrndMkrModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int modelNo;
	private String modelNm;
	private String cateCd;
	private Integer pprRnk;
	private int mkrId;
	private String mkrNm;
	private int brndId;
	private String brndNm;
	private String useYn;
	private int mtcPlCnt;
}
