package com.enuri.brndmkr.modelmatch.repository.main;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblGoods;

@Repository
public interface TblGoodsRepository extends JpaRepository<TblGoods, Integer> {
	@Query(value = """
			SELECT 	g
			FROM 	TblGoods g INNER JOIN TblGoodsSum s ON g.modelno = s.modelno
			WHERE	g.caCode = :caCode
					AND g.constrain IN ('1', '5')
					AND (g.enrMkrId = :enrMkrId OR g.enrBrndId = :enrBrndId)
					AND s.mallcnt > 0
			""",
			countQuery = """
			SELECT  count(g)
			FROM 	TblGoods g INNER JOIN TblGoodsSum s ON g.modelno = s.modelno
			WHERE	g.caCode = :caCode
					AND g.constrain IN ('1', '5')
					AND (g.enrMkrId = :enrMkrId OR g.enrBrndId = :enrBrndId)
					AND s.mallcnt > 0
			""")
	@Transactional(readOnly = true)
	Page<TblGoods> findByCaCode(@Param("caCode") String caCode, @Param("enrMkrId") int enrMkrId, @Param("enrBrndId") int enrBrndId, Pageable pageable);
}