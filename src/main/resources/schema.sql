CREATE TABLE IF NOT EXISTS API_REQ (
  ID_REQ     BIGINT NOT NULL,
  VA_API_URI VARCHAR(500) NOT NULL,
  VA_API_MTH VARCHAR(8) NOT NULL, 
  VA_API_HDR CLOB(1K),  --JSON : Map<String,String>
  VA_API_BDY CLOB(1M),  --JSON
  VA_API_CHR VARCHAR(10) NOT NULL,
  VA_API_NME VARCHAR(50),
  VA_API_DSC VARCHAR(500),
  VA_ASR_PRL BOOLEAN NOT NULL, --parallel
  VA_ASR_STR BOOLEAN NOT NULL, --strict
  VA_ASR_ENB BOOLEAN NOT NULL, --enable
  VA_ASR_DBG BOOLEAN NOT NULL, --debug
  VA_ASR_EXL VARCHAR(500),
  PRIMARY KEY (ID_REQ)
);

CREATE TABLE IF NOT EXISTS ASR_REQ (
  ID_ASR     BIGINT NOT NULL,
  ID_REQ     BIGINT, --nullable => local test files
  VA_EXT_HST VARCHAR(200) NOT NULL,
  VA_ACT_HST VARCHAR(200) NOT NULL,
  DH_EXT_STR TIMESTAMP(3) NOT NULL,
  DH_EXT_END TIMESTAMP(3) NOT NULL,
  DH_ACT_STR TIMESTAMP(3) NOT NULL,
  DH_ACT_END TIMESTAMP(3) NOT NULL,
  VA_REQ_STT VARCHAR(5) NOT NULL,
  VA_REQ_STP VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS ASR_GRP (
  ID_ASR     BIGINT NOT NULL,
  VA_HST_USR VARCHAR(50),
  VA_HST_OS  VARCHAR(20),
  VA_HST_ADR VARCHAR(15),
  VA_API_APP VARCHAR(50) NOT NULL,
  VA_EXT_ENV VARCHAR(20) NOT NULL,
  VA_ACT_ENV VARCHAR(20) NOT NULL,
  VA_GRP_STT VARCHAR(7) NOT NULL,
  PRIMARY KEY (ID_ASR)
);

CREATE TABLE IF NOT EXISTS API_ENV (
  ID_ENV     BIGINT NOT NULL,
  VA_API_HST VARCHAR(200) NOT NULL,
  VA_API_PRT INT NOT NULL,
  VA_API_AUT_HST VARCHAR(200),
  VA_API_AUT_MTH VARCHAR(10),
  VA_API_APP VARCHAR(50) NOT NULL,
  VA_API_ENV VARCHAR(20) NOT NULL,
  VA_API_PRD BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS API_REQ_GRP (
  ID_REQ     BIGINT NOT NULL,
  VA_API_APP VARCHAR(50) NOT NULL,
  VA_API_ENV VARCHAR(20) NOT NULL
);