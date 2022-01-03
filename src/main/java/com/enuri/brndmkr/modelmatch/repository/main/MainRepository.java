package com.enuri.brndmkr.modelmatch.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblGoods;
import com.enuri.brndmkr.modelmatch.repository.main.interfaces.MainRepositoryCustom;

@Repository
public interface MainRepository extends JpaRepository<TblGoods, Integer>, MainRepositoryCustom {
}