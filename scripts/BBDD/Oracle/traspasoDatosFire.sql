-- 1 Crear la tablas nuevas 
CREATE TABLE "TB_APLICACIONES_NEW" (
  "ID" VARCHAR2(48) NOT NULL,
  "NOMBRE" VARCHAR2(45) NOT NULL,
  "RESPONSABLE" VARCHAR2(45) NOT NULL,
  "RESP_CORREO" VARCHAR2(45),
  "RESP_TELEFONO" VARCHAR2(30) ,
  "FECHA_ALTA" TIMESTAMP NOT NULL,
  "FK_CERTIFICADO" NUMBER,
  constraint  "TB_APLICACIONES_NEW_PK" primary key ("ID")
) 

CREATE table "TB_CERTIFICADOS" (
    "ID_CERTIFICADO"   NUMBER NOT NULL,
    "NOMBRE_CERT"      VARCHAR2(45) NOT NULL,
    "FEC_ALTA"         TIMESTAMP NOT NULL,
    "CERT_PRINCIPAL"   CLOB,
    "CERT_BACKUP"      CLOB,
    "HUELLA_PRINCIPAL" VARCHAR2(45),
    "HUELLA_BACKUP"    VARCHAR2(45),
    constraint  "TB_CERTIFICADOS_PK" primary key ("ID_CERTIFICADO")
)


CREATE sequence "TB_CERTIFICADOS_SEQ" 


CREATE trigger "BI_TB_CERTIFICADOS"  
  before insert on "TB_CERTIFICADOS"              
  for each row 
begin  
  if :NEW."ID_CERTIFICADO" is null then
    select "TB_CERTIFICADOS_SEQ".nextval into :NEW."ID_CERTIFICADO" from dual;
  end if;
end;
 


CREATE TABLE  "TB_USUARIOS" 
   ("ID_USUARIO" NUMBER NOT NULL ENABLE, 
	"NOMBRE_USUARIO" VARCHAR2(30) NOT NULL ENABLE, 
	"CLAVE" VARCHAR2(45) NOT NULL ENABLE, 
	"NOMBRE" VARCHAR2(45) NOT NULL ENABLE, 
	"APELLIDOS" VARCHAR2(120) NOT NULL ENABLE, 
	"CORREO_ELEC" VARCHAR2(45), 
	"FEC_ALTA" TIMESTAMP (6) NOT NULL ENABLE, 
	"TELF_CONTACTO" VARCHAR2(45), 
	"ROL" VARCHAR2(45) DEFAULT ('admin') NOT NULL , 
	"USU_DEFECTO" NUMBER(1,0) NOT NULL ENABLE, 
	 CONSTRAINT "TB_USUARIOS_PK" PRIMARY KEY ("ID_USUARIO") ENABLE, 
	 CONSTRAINT "TB_USUARIOS_UK1" UNIQUE ("NOMBRE_USUARIO") ENABLE
   ) ;

CREATE sequence "TB_USUARIOS_SEQ"; 
   
CREATE OR REPLACE TRIGGER  "BI_TB_USUARIOS" 
  before insert on "TB_USUARIOS"               
  for each row  
begin   
  if :NEW."ID_USUARIO" is null then 
    select "TB_USUARIOS_SEQ".nextval into :NEW."ID_USUARIO" from dual; 
  end if; 
end; 


ALTER TRIGGER  "BI_TB_USUARIOS" ENABLE;


ALTER TABLE  "TB_USUARIOS" modify("USU_DEFECTO" NUMBER(1,0) default 0);


ALTER TABLE  "TB_USUARIOS" modify
("FEC_ALTA" TIMESTAMP default SYSDATE);


ALTER TABLE  "TB_CERTIFICADOS" modify
("FEC_ALTA" TIMESTAMP default SYSDATE);


ALTER TABLE  "TB_APLICACIONES_NEW" modify
("FECHA_ALTA" TIMESTAMP default SYSDATE);


-- FIN CREAR TABLAS NUEVAS----------

-- INSERTAR DATOS--------------

-- USUARIO POR DEFECTO --------
INSERT INTO tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto) 
VALUES('admin','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1);

-- CERTIFICADOS-------
INSERT INTO tb_certificados (nombre_cert,fec_alta,cert_principal,huella_principal) 
SELECT CONCAT('CERT_',a.id)AS nombre_cert,a.fecha_alta,TO_CLOB(a.cer),a.huella FROM tb_aplicaciones a;



-- APLICACIONES NEW-----------

INSERT INTO TB_APLICACIONES_NEW (id,nombre,fecha_alta,responsable,resp_correo,resp_telefono,fk_certificado)
SELECT a.ID, a.NOMBRE,a.FECHA_ALTA,a.RESPONSABLE,a.RESP_CORREO,a.RESP_TELEFONO ,c.ID_CERTIFICADO 
FROM TB_APLICACIONES a, TB_CERTIFICADOS c 
WHERE  SUBSTR(c.NOMBRE_CERT,INSTR(c.NOMBRE_CERT,'_',1,1) + 1) = a.ID;

CREATE TABLE TB_APLICACIONES_OLD AS SELECT * FROM TB_APLICACIONES;

DROP TABLE TB_APLICACIONES;

ALTER TABLE  "TB_APLICACIONES_NEW" rename
to "TB_APLICACIONES";


ALTER TABLE "TB_APLICACIONES" add constraint
"TB_APLICACIONES_FK" foreign key ("FK_CERTIFICADO") references "TB_CERTIFICADOS" ("ID_CERTIFICADO");

