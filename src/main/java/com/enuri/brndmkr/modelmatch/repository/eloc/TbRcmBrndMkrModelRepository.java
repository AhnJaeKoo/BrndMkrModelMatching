package com.enuri.brndmkr.modelmatch.repository.eloc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.eloc.TbRcmBrndMkrModel;

@Repository
public interface TbRcmBrndMkrModelRepository extends JpaRepository<TbRcmBrndMkrModel, Integer> {

	@Modifying
	@Query(value = "TRUNCATE TABLE tb_rcm_brnd_mkr_model", nativeQuery = true)
	@Transactional
	int truncateTable();

	@Modifying
	@Transactional
	int deleteByCateCd(String cateCd);
}