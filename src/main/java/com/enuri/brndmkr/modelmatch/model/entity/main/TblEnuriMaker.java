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
@Table(name = "tbl_enuri_maker")
@NoArgsConstructor
@AllArgsConstructor
public class TblEnuriMaker implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int makerId;
	private String makerNm;
	private String delYn;
}
