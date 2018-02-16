-- 1 Crear la tablas nuevas 
CREATE TABLE "tb_aplicaciones_new" (
  "id" VARCHAR2(48) NOT NULL,
  "nombre" VARCHAR2(45) NOT NULL,
  "responsable" VARCHAR2(45) NOT NULL,
  "resp_correo" VARCHAR2(45),
  "resp_telefono" VARCHAR2(30) ,
  "fecha_alta" TIMESTAMP NOT NULL,
  "fk_certificado" NUMBER,
  constraint  "tb_aplicaciones_new_pk" primary key ("id")
) /

CREATE table "TB_CERTIFICADOS" (
    "ID_CERTIFICADO"   NUMBER NOT NULL,
    "NOMBRE_CERT"      VARCHAR2(45) NOT NULL,
    "FEC_ALTA"         TIMESTAMP NOT NULL,
    "CERT_PRINCIPAL"   BLOB,
    "CERT_BACKUP"      BLOB,
    "HUELLA_PRINCIPAL" VARCHAR2(45),
    "HUELLA_BACKUP"    VARCHAR2(45),
    constraint  "TB_CERTIFICADOS_PK" primary key ("ID_CERTIFICADO")
)
/

CREATE sequence "TB_CERTIFICADOS_SEQ" 
/

CREATE trigger "BI_TB_CERTIFICADOS"  
  before insert on "TB_CERTIFICADOS"              
  for each row 
begin  
  if :NEW."ID_CERTIFICADO" is null then
    select "TB_CERTIFICADOS_SEQ".nextval into :NEW."ID_CERTIFICADO" from dual;
  end if;
end;
/  


CREATE TABLE  "TB_USUARIOS" 
   (	"ID_USUARIO" NUMBER NOT NULL ENABLE, 
	"NOMBRE_USUARIO" VARCHAR2(30) NOT NULL ENABLE, 
	"CLAVE" VARCHAR2(45) NOT NULL ENABLE, 
	"NOMBRE" VARCHAR2(45) NOT NULL ENABLE, 
	"APELLIDOS" VARCHAR2(120) NOT NULL ENABLE, 
	"CORREO_ELEC" VARCHAR2(45), 
	"FEC_ALTA" TIMESTAMP (6) NOT NULL ENABLE, 
	"TELF_CONTACTO" VARCHAR2(45), 
	"ROL" VARCHAR2(45), 
	"USU_DEFECTO" NUMBER(1,0) NOT NULL ENABLE, 
	 CONSTRAINT "TB_USUARIOS_PK" PRIMARY KEY ("ID_USUARIO") ENABLE, 
	 CONSTRAINT "TB_USUARIOS_UK1" UNIQUE ("NOMBRE_USUARIO") ENABLE
   ) ;


CREATE OR REPLACE TRIGGER  "BI_TB_USUARIOS" 
  before insert on "TB_USUARIOS"               
  for each row  
begin   
  if :NEW."ID_USUARIO" is null then 
    select "TB_USUARIOS_SEQ".nextval into :NEW."ID_USUARIO" from dual; 
  end if; 
end; 

/
ALTER TRIGGER  "BI_TB_USUARIOS" ENABLE;
ALTER TABLE  "TB_USUARIOS" modify
("USU_DEFECTO" NUMBER(1,0) default 0);
/
ALTER TABLE  "TB_USUARIOS" modify
("FEC_ALTA" TIMESTAMP default SYSDATE);
/
ALTER TABLE  "TB_CERTIFICADOS" modify
("FEC_ALTA" TIMESTAMP default SYSDATE);
/

ALTER TABLE  "tb_aplicaciones_new" modify
("fecha_alta" TIMESTAMP default SYSDATE);
/

-- FIN CREAR TABLAS NUEVAS----------

-- INSERTAR DATOS--------------

-- USUARIO POR DEFECTO --------
INSERT INTO tb_usuarios (nombre_usuario,clave,nombre,apellidos,usu_defecto) 

VALUES('admin_pass','D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=','default name','default surnames',1);

-- CERTIFICADOS-------
INSERT INTO tb_certificados (nombre_cert,fec_alta,cert_principal,huella_principal) 
SELECT CONCAT('CERT_',a.id)AS nombre_cert,a.fecha_alta,a.cer,a.huella FROM tb_aplicaciones a;



-- APLICACIONES NEW-----------

INSERT INTO tb_aplicaciones_new (id,nombre,fecha_alta,responsable,resp_correo,resp_telefono,fk_certificado)
SELECT a.id, a.nombre,a.fecha_alta,a.responsable,a.resp_correo,a.resp_telefono ,c.id_certificado 
FROM tb_aplicaciones a, tb_certificados c 
WHERE  SUBSTR(c.nombre_cert,INSTR(c.nombre_cert,'_',1,1))=a.id;

CREATE TABLE tb_aplicaciones_old SELECT * FROM tb_aplicaciones;

DROP table tb_aplicaciones ;

ALTER TABLE  "tb_aplicaciones_new" rename
to "TB_APLICACIONES"


alter table "TB_APLICACIONES" add constraint
"TB_APLICACIONES_FK" foreign key ("fk_certificado") references "TB_CERTIFICADOS" ("ID_CERTIFICADO")


