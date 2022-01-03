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
@Table(name = "tbl_enuri_brand")
@NoArgsConstructor
@AllArgsConstructor
public class TblEnuriBrand implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int brandId;
	private int makerId;
	private String brandNm;
	private String delYn;
}