package com.enuri.brndmkr.modelmatch.model.entity.main;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TblCateBrandListPk implements Serializable {

	private static final long serialVersionUID = 1L;

	private String caCode;
	private String brand;
}
