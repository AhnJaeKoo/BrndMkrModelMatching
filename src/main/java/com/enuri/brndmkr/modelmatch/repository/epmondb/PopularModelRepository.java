package com.enuri.brndmkr.modelmatch.repository.epmondb;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularModel;

@Repository
public interface PopularModelRepository extends JpaRepository<PopularModel, Integer> {

	@Modifying
	@Query(value = "TRUNCATE TABLE popular_model", nativeQuery = true)
	@Transactional
	int truncateTable();

	@Query(value = """
			SELECT DISTINCT model_no,
					        cate_cd,
					        sum_popular,
					        popularRank
			FROM   (SELECT model_no,
			               cate_cd,
			               sum_popular,
			               Row_number() over(PARTITION BY cate_cd ORDER BY sum_popular DESC, model_no DESC) AS popularRank
			        FROM   popular_model) a
			""",
			countQuery = """
					SELECT count(*)
					FROM   popular_model
					""",
			nativeQuery = true)
	Page<Map<String, Object>> selectPopularGroupModel(Pageable pageable);

	@Query(value = """
			SELECT 	distinct g
			FROM 	PopularModel g
			WHERE	g.cateCd = :cateCd
					AND g.constrain IN ('1', '5')
					AND (g.enrMkrId = :enrMkrId OR g.enrBrndId = :enrBrndId)
			""",
			countQuery = """
			SELECT  count(distinct g)
			FROM 	PopularModel g
			WHERE	g.cateCd = :cateCd
					AND g.constrain IN ('1', '5')
					AND (g.enrMkrId = :enrMkrId OR g.enrBrndId = :enrBrndId)
			""")
	@Transactional(readOnly = true)
	Page<PopularModel> findByCaCode(@Param("cateCd") String cateCd, @Param("enrMkrId") int enrMkrId, @Param("enrBrndId") int enrBrndId, Pageable pageable);

	@Modifying
	@Transactional
	int deleteByCateCdStartingWith(String cateCd);
}