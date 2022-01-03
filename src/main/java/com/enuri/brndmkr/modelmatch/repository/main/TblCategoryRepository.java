package com.enuri.brndmkr.modelmatch.repository.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblCategory;

@Repository
public interface TblCategoryRepository extends JpaRepository<TblCategory, String> {

	@Query(value = """
			SELECT m
			  FROM TblCategory m
			 WHERE m.cSeqno > 0
			""")
	@Transactional(readOnly = true)
	List<TblCategory> findByUseAllCate();

	@Query(value = """
			SELECT m
			  FROM TblCategory m
			 WHERE m.cSeqno > 0
			   AND m.caCode LIKE :caCode
			ORDER BY m.caCode
			""")
	@Transactional(readOnly = true)
	List<TblCategory> findByFirstnameLikeCate(@Param("caCode") String caCode);
}