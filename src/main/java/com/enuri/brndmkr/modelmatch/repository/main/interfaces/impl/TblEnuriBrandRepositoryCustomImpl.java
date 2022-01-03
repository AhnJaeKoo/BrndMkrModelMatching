package com.enuri.brndmkr.modelmatch.repository.main.interfaces.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.qlrm.mapper.JpaResultMapper;

import com.enuri.brndmkr.modelmatch.model.dto.main.BrndListDto;
import com.enuri.brndmkr.modelmatch.repository.main.interfaces.TblEnuriBrandRepositoryCustom;

public class TblEnuriBrandRepositoryCustomImpl implements TblEnuriBrandRepositoryCustom {

	@PersistenceContext(unitName = "main")
	private EntityManager mainEm;

	//카테고리별 제조사의 동의어를 추출하여 가장 긴거부터 나열
	@Override
	public List<BrndListDto> findByCateCode(String cateCode) {
		String sql = """
				SELECT brnd.brand_id                       AS brandId,
				       brnd.maker_id                       AS makerId,
				       Nvl(d.enuri_keyword, brnd.brand_nm) AS brandNm,
				       cate_brnd.maker_nm                  AS makerNm,
				       d.p_ek_no                           AS pEkNo,
				       CASE WHEN d.ek_no IS NULL OR d.p_ek_no = d.ek_no THEN 1 ELSE 2 END sort
				FROM   tbl_enuri_brand brnd,
				       (SELECT a.brand_id,
				               c.maker_nm
				        FROM   tbl_enuri_brand a
				               inner join tbl_cate_brand_list b
				                       ON a.brand_nm = b.brand
				               inner join tbl_enuri_maker c
				                       ON a.maker_id = c.maker_id
				        WHERE  a.del_yn = 'N'
				               AND b.ca_code LIKE :cateCode
				        GROUP  BY a.brand_id,
				                  c.maker_nm) cate_brnd,
				       tbl_enuri_keyword c,
				       tbl_enuri_keyword d
				WHERE  del_yn = 'N'
				       AND brnd.brand_id = cate_brnd.brand_id
				       AND brnd.brand_nm = c.enuri_keyword(+)
				       AND c.p_ek_no = d.p_ek_no(+)
				ORDER  BY sort, brandid, brandnm DESC
				""";

		JpaResultMapper jpaResultMapper = new JpaResultMapper();	// result -> DTO 가능
		Query query = mainEm.createNativeQuery(sql);
		query.setParameter("cateCode", cateCode);
		return jpaResultMapper.list(query, BrndListDto.class);
	}
}