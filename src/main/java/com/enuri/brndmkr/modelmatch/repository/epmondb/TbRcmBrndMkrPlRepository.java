package com.enuri.brndmkr.modelmatch.repository.epmondb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.epmondb.TbRcmBrndMkrPl;

@Repository
public interface TbRcmBrndMkrPlRepository extends JpaRepository<TbRcmBrndMkrPl, Long> {

	@Modifying
	@Query(value = "TRUNCATE TABLE TB_RCM_BRND_MKR_PL", nativeQuery = true)
	@Transactional
	public int truncateTable();

	@Modifying
	@Transactional
	@Query(value = "delete TbRcmBrndMkrPl p where p.cateCd = :cateCd ")
	int deleteByCateCd(@Param("cateCd") String cateCd);
}