package com.enuri.brndmkr.modelmatch.repository.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblPricelist;

@Repository
public interface TblPricelistRepository extends JpaRepository<TblPricelist, Long> {

	List<TblPricelist> findByModelnoAndStatus(int modelno, String status);
}