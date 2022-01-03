package com.enuri.brndmkr.modelmatch.model.entity.main;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
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
public class TblGoods implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int modelno;
	private String modelnm;
	private String caCode;
	private String constrain;
	private Integer modelnoGroup;	//null 값 허용을 위해 Integer 사용
	private int enrMkrId;
	private int enrBrndId;
}
