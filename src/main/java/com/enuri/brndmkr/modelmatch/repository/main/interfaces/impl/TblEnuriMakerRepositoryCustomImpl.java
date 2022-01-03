package com.enuri.brndmkr.modelmatch.repository.main.interfaces.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;

import com.enuri.brndmkr.modelmatch.model.dto.main.MkrListDto;
import com.enuri.brndmkr.modelmatch.repository.main.interfaces.TblEnuriMakerRepositoryCustom;

public class TblEnuriMakerRepositoryCustomImpl implements TblEnuriMakerRepositoryCustom {

	@PersistenceContext(unitName = "main")
	private EntityManager mainEm;

	//카테고리별 제조사의 동의어를 추출하여 가장 긴거부터 나열
	@Override
	public List<MkrListDto> findByCateCode(String cateCode) {
		String sql = """
				SELECT mkr.maker_id                       AS makerId,
				       Nvl(d.enuri_keyword, mkr.maker_nm) AS makerNm,
				       d.p_ek_no                          AS pEkNo,
				       CASE WHEN d.ek_no IS NULL OR d.p_ek_no = d.ek_no THEN 1 ELSE 2 END sort
				FROM   (SELECT DISTINCT a.maker_id,
				                        a.maker_nm
				        FROM   tbl_enuri_maker a
				               inner join tbl_cate_factory_list b
				                       ON a.maker_nm = b.factory
				        WHERE  a.del_yn = 'N'
				               AND b.ca_code LIKE :cateCode) mkr
				       left outer join tbl_enuri_keyword c
				                    ON mkr.maker_nm = c.enuri_keyword
				       left outer join tbl_enuri_keyword d
				                    ON c.p_ek_no = d.p_ek_no
				ORDER  BY sort, makerid, makernm DESC
				""";

		JpaResultMapper jpaResultMapper = new JpaResultMapper();	// result -> DTO 가능
		Query query = mainEm.createNativeQuery(sql);
		query.setParameter("cateCode", cateCode);
		return jpaResultMapper.list(query, MkrListDto.class);
	}
}