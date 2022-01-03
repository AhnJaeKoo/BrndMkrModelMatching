package com.enuri.brndmkr.modelmatch.batch.item.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.enuri.brndmkr.modelmatch.model.dto.eloc.TbRcmBrndMkrModelDto;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingModelItemWriter implements ItemWriter<TbRcmBrndMkrModelDto> {

	@Resource
	private DataSource elocDataSource;
	@Resource
	private DataSource epMonDbDataSource;

	@Override
	public void write(List<? extends TbRcmBrndMkrModelDto> items) throws Exception {
		String modelSql = """
				INSERT INTO tb_rcm_brnd_mkr_model
				            (model_no,
				             model_nm,
				             cate_cd,
				             ppr_rnk,
				             mkr_id,
				             mkr_nm,
				             brnd_id,
				             brnd_nm,
				             use_yn,
				             mtc_pl_cnt,
				             chg_cate_cd,
				             gr_model_no)
				VALUES      (?,
				             ?,
				             ?,
				             ?,
				             ?,
				             ?,
				             ?,
				             ?,
				             'Y',
				             ?,
				             ?,
				             ?)
				""";

		String plSql = """
				INSERT INTO tb_rcm_brnd_mkr_pl
				            (pl_no,
				             goods_nm,
				             model_no,
				             brnd_mkr_keyword,
				             cate_cd)
				VALUES      (?,
				             ?,
				             ?,
				             ?,
				             ?)
				""";

		@Cleanup Connection elocConn = elocDataSource.getConnection();
		@Cleanup Connection epMonDbConn = epMonDbDataSource.getConnection();
		@Cleanup PreparedStatement modelPstmt = elocConn.prepareStatement(modelSql);
		@Cleanup PreparedStatement plPstmt = epMonDbConn.prepareStatement(plSql);

		items.forEach(model -> {
			try {
				modelPstmt.clearParameters();
				modelPstmt.setInt(1, model.getModelNo());
				modelPstmt.setString(2, model.getModelNm());
				modelPstmt.setString(3, model.getCateCd());
				if (model.getPopularRank() ==  null) {
					modelPstmt.setNull(4, Types.INTEGER);
				} else {
					modelPstmt.setInt(4, model.getPopularRank());
				}
				modelPstmt.setInt(5, model.getMkrId());
				modelPstmt.setString(6, model.getMkrNm());
				modelPstmt.setInt(7, model.getBrndId());
				modelPstmt.setString(8, model.getBrndNm());
				modelPstmt.setInt(9, model.getPls().size());
				modelPstmt.setString(10, model.getCateCd());
				modelPstmt.setInt(11, model.getGroupModelNo());
				modelPstmt.addBatch();

				model.getPls().forEach(pl -> {
					try {
						plPstmt.clearParameters();
						plPstmt.setLong(1, pl.getPlNo());
						plPstmt.setString(2, pl.getGoodsNm());
						plPstmt.setInt(3, pl.getModelNo());
						plPstmt.setString(4, pl.getBrndMkrKeyword());
						plPstmt.setString(5, pl.getCateCd());
						plPstmt.addBatch();
					} catch (SQLException e) {
						log.error("pl = {}", pl.toString(), e);
					}
				});
			} catch (SQLException e) {
				log.error("model = {}", model.toString(), e);
			}
		});

		modelPstmt.executeBatch();
		modelPstmt.clearBatch();
		plPstmt.executeBatch();
		plPstmt.clearBatch();
	}
}
