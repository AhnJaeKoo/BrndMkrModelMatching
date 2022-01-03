package com.enuri.brndmkr.modelmatch.repository.main.interfaces;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MainRepositoryCustom {
	@Query(value = """
				SELECT /*+ full(b) parallel(b 8)*/
					   a.modelno,
				       b.modelnm,
				       b.ca_code,
				       b.constrain,
				       a.popular,
				       CASE WHEN b.modelno_group IS NULL THEN a.popular ELSE a.sum_popular END sum_popular,
				       NVL(b.modelno_group, 0) AS group_modelno,
				       b.enr_mkr_id,
				       b.enr_brnd_id
				FROM   tbl_goods_sum a
				       inner join tbl_goods b
				       		 ON a.modelno = b.modelno
				WHERE  a.mallcnt > 0
				       AND b.ca_code LIKE :cateCd
				       AND (b.modelno_group IS NULL OR
				       		b.modelno = b.modelno_group)
				""",
		countQuery = """
				SELECT count(*)
				FROM   tbl_goods_sum a
				       inner join tbl_goods b
				       		 ON a.modelno = b.modelno
				WHERE  a.mallcnt > 0
				       AND b.ca_code LIKE :cateCd
				       AND (b.modelno_group IS NULL OR
				       		b.modelno = b.modelno_group)
				""",
		nativeQuery = true)
	Page<Map<String, Object>> findByCateCd(@Param("cateCd") String cateCd, Pageable pageable);
}