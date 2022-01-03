package com.enuri.brndmkr.modelmatch.repository.epmondb;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.enuri.brndmkr.modelmatch.model.entity.epmondb.PopularGroupModel;

@Repository
public interface PopularGroupModelRepository extends JpaRepository<PopularGroupModel, Integer> {

	@Query(value = "TRUNCATE TABLE popular_group_model", nativeQuery = true)
	@Modifying
	@Transactional
	int truncateTable();

	@Query(value = "SELECT m FROM PopularGroupModel m WHERE m.modelNo = :modelNo")
	Optional<PopularGroupModel> findTop1ById(@Param("modelNo") int modelNo);

	@Modifying
	@Transactional
	int deleteByCateCdStartingWith(String cateCd);
}