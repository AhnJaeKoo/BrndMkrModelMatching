package com.enuri.brndmkr.modelmatch.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblEnuriMaker;
import com.enuri.brndmkr.modelmatch.repository.main.interfaces.TblEnuriMakerRepositoryCustom;

@Repository
public interface TblEnuriMakerRepository extends JpaRepository<TblEnuriMaker, Integer>, TblEnuriMakerRepositoryCustom {
}