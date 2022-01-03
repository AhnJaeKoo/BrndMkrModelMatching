CREATE UNLOGGED TABLE category (
 cate_cd varchar(12) NOT NULL,
 CONSTRAINT category_pk PRIMARY KEY (cate_cd)
);

COMMENT ON COLUMN dbenuri.category.cate_cd IS '카테고리코드';

CREATE UNLOGGED TABLE popular_group_model (
	model_no numeric(10) NOT NULL,
	cate_cd varchar(12) NOT NULL,
	sum_popular numeric(10),
	popular_rank numeric(6),
	CONSTRAINT popular_group_model_pk PRIMARY KEY (model_no)
);
CREATE INDEX idx_popular_group_model_01 ON popular_group_model USING btree (cate_cd, popular_rank);

COMMENT ON COLUMN dbenuri.popular_group_model.model_no IS '모델번호';
COMMENT ON COLUMN dbenuri.popular_group_model.cate_cd IS '카테고리 코드';
COMMENT ON COLUMN dbenuri.popular_group_model.sum_popular IS '총인기도';
COMMENT ON COLUMN dbenuri.popular_group_model.popular_rank IS '인기순위';


CREATE UNLOGGED TABLE popular_model (
	model_no numeric(10) NOT NULL,
	model_nm varchar(400) NOT NULL,
	group_model_no numeric(10) NOT NULL,
	cate_cd varchar(12) NOT NULL,
	constrain varchar(1) NOT NULL,
	sum_popular numeric(10),
	CONSTRAINT popular_model_pk PRIMARY KEY (model_no)
);
CREATE INDEX idx_popular_model_01 ON popular_model USING btree (cate_cd, constrain);
CREATE INDEX idx_popular_model_02 ON popular_model USING btree (group_model_no);

COMMENT ON COLUMN dbenuri.popular_model.model_no IS '모델번호';
COMMENT ON COLUMN dbenuri.popular_model.model_nm IS '모델명';
COMMENT ON COLUMN dbenuri.popular_model.group_model_no IS '그룹모델번호';
COMMENT ON COLUMN dbenuri.popular_model.cate_cd IS '카테고리코드';
COMMENT ON COLUMN dbenuri.popular_model.constrain IS '모델상태코드';
COMMENT ON COLUMN dbenuri.popular_model.sum_popular IS '총인기도';

CREATE UNLOGGED TABLE sug_brnd_mkr_model (
	model_no numeric(10) NOT NULL,
	model_nm varchar(140) NOT NULL,
	cate_cd varchar(8) NOT NULL,
	popular_rank numeric(3),
	mkr_id numeric(10),
	mkr_nm varchar(100),
	brnd_id numeric(10),
	brnd_nm varchar(100),
	use_flag varchar(1),
	pl_cnt numeric(3)
	CONSTRAINT sug_brnd_mkr_model_pk PRIMARY KEY (model_no)
);

CREATE INDEX idx_sug_brnd_mkr_model_01 ON sug_brnd_mkr_model USING btree (cate_cd, popular_rank);
CREATE INDEX idx_sug_brnd_mkr_model_02 ON sug_brnd_mkr_model USING btree (cate_cd, model_nm);
CREATE INDEX idx_sug_brnd_mkr_model_03 ON sug_brnd_mkr_model USING btree (cate_cd, mkr_nm);
CREATE INDEX idx_sug_brnd_mkr_model_04 ON sug_brnd_mkr_model USING btree (cate_cd, brnd_nm);

COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.model_no IS '모델번호';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.model_nm IS '모델명';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.cate_cd IS '카테고리코드';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.popular_rank IS '인기순위';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.mkr_id IS '제조사ID';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.mkr_nm IS '제조사명';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.brnd_id IS '브랜드ID';
COMMENT ON COLUMN dbenuri.sug_brnd_mkr_model.brnd_nm IS '브랜드명';


CREATE UNLOGGED TABLE TB_RCM_BRND_MKR_PL (
	pl_no numeric(12) NOT NULL,
	goods_nm varchar(400) NOT NULL,
	model_no numeric(10) NOT NULL,
	brnd_mkr_keyword varchar(100),
	CONSTRAINT TB_RCM_BRND_MKR_PL_pk PRIMARY KEY (pl_no)
);

CREATE INDEX idx_TB_RCM_BRND_MKR_PL_01 ON TB_RCM_BRND_MKR_PL USING btree (model_no);

COMMENT ON COLUMN dbenuri.TB_RCM_BRND_MKR_PL.pl_no IS '상품번호';
COMMENT ON COLUMN dbenuri.TB_RCM_BRND_MKR_PL.goods_nm IS '상품명';
COMMENT ON COLUMN dbenuri.TB_RCM_BRND_MKR_PL.model_no IS '모델번호';
COMMENT ON COLUMN dbenuri.TB_RCM_BRND_MKR_PL.brnd_mkr_keyword IS '추출 제조사/브랜드 키워드';

