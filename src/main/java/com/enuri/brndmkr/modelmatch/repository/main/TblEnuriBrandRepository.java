package com.enuri.brndmkr.modelmatch.repository.main;

import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblEnuriBrand;
import com.enuri.brndmkr.modelmatch.repository.main.interfaces.TblEnuriBrandRepositoryCustom;

@Repository
public interface TblEnuriBrandRepository extends JpaRepository<TblEnuriBrand, Integer>, TblEnuriBrandRepositoryCustom {

	@Query(value = """
			SELECT   b.makerId AS makerId,
					 m.makerNm AS makerNm,
			         b.brandId AS brandId,
			         b.brandNm AS brandNm
			FROM     TblEnuriBrand b inner join TblEnuriMaker m on b.makerId = m.makerId
			WHERE    b.brandId = :brandId
			AND      b.delYn = 'N'
			AND 	 ROWNUM = 1
			ORDER BY b.brandId
		""")
	Map<String, Object> findTop1ByBrandId(@Param("brandId") int brandId);
}