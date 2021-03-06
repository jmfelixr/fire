/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.dao;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.Application;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Hexify;



/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public class AplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(AplicationsDAO.class.getName());

	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	private static final String MD_ALGORITHM = "SHA-256"; //$NON-NLS-1$

	private static final String HMAC_ALGORITHM = "HmacMD5"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_CONFIG_VALUE = "SELECT valor FROM tb_configuracion WHERE parametro = ?"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS = "SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta, fk_certificado  FROM tb_aplicaciones ORDER BY nombre"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS_PAG = "SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta, fk_certificado  FROM tb_aplicaciones ORDER BY nombre limit ?,?"; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_BYCERT = "SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta, fk_certificado  FROM tb_aplicaciones a, tb_certificados c where a.fk_certificado=c.id_certificado and c.id_certificado=? ORDER BY nombre "; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATIONS_COUNT = "SELECT count(*) FROM tb_aplicaciones"; //$NON-NLS-1$

	private static final String ST_SELECT_APPLICATIONS_COUNT_BYCERT = "SELECT count(*) FROM tb_aplicaciones a, tb_certificados c where a.fk_certificado=c.id_certificado and c.id_certificado=?"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_APPLICATION_BYID = "SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta,fk_certificado FROM tb_aplicaciones WHERE id= ?"; //$NON-NLS-1$

	private static final String STATEMENT_INSERT_APPLICATION = "INSERT INTO tb_aplicaciones(id, nombre, responsable, resp_correo, resp_telefono, fecha_alta,fk_certificado ) VALUES (?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

	private static final String STATEMENT_REMOVE_APPLICATION = "DELETE FROM tb_aplicaciones WHERE id = ?"; //$NON-NLS-1$

	private static final String STATEMENT_UPDATE_APPLICATION = "UPDATE tb_aplicaciones SET nombre=?, responsable = ?, resp_correo = ?, resp_telefono = ?, fk_certificado=?  WHERE id = ?";//$NON-NLS-1$

	private static final String KEY_ADMIN_PASS = "admin_pass"; //$NON-NLS-1$

	/**
	 * Comprueba contra base de datos que la contrase&ntilde;a indicada se corresponda
	 * con la del administrador del sistema.
	 * @param psswd Contrase&ntilde;a.
	 * @return {@code true} si la contrase&ntilde;a es del administrador, {@code false} en caso contrario.
	 * @throws SQLException Cuando ocurre un error al comprobar los datos contra la base de datos.
	 */
	public static boolean checkAdminPassword(final String psswd) throws SQLException {

		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(psswd.getBytes(DEFAULT_CHARSET));
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", e); //$NON-NLS-1$
			return false;
		} catch (final UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Codificacion incorrecta", e); //$NON-NLS-1$
			return false;
		}

		boolean result = true;
		final String keyAdminB64 = getConfigValue(KEY_ADMIN_PASS);
		if (keyAdminB64 ==  null || !keyAdminB64.equals(Base64.encode(md))) {
			LOGGER.severe("Se ha insertado una contrasena de administrador no valida"); //$NON-NLS-1$
			result = false;
		}
		return result;
	}

	/**
	 * Obtiene el valor de una clave de la tabla de par&aacute;metros de configuraci&oacute;n.
	 * @param conn Conexi&oacute;n con base de datos.
	 * @param param Clave del par&aacute;metro a obtener.
	 * @return Valor del par&aacute;metro de configuraci&oacute;n.
	 * @throws SQLException Cuando ocurre un error en el acceso al par&aacute;metro.
	 */
	private static String getConfigValue(final String param) throws SQLException {
		String value = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_CONFIG_VALUE);
			st.setString(1, param);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				value = rs.getString(1);
			}

			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al acceder a la base datos", e); //$NON-NLS-1$
			throw new SQLException(e);
		}
		return value;
	}

	/**
	 * Obtiene una estructura JSON con el listado de aplicaciones.
	 * @return Estructura JSON.
	 */
	public static List<Application> getApplications() throws SQLException {

		final List<Application> result = new ArrayList<>();

		/*"SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta, fk_certificado  FROM tb_aplicaciones ORDER BY nombre*/
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			final Application app = new Application();
			app.setId(rs.getString(1));
			app.setNombre(rs.getString(2));
			app.setResponsable(rs.getString(3));
			app.setCorreo(rs.getString(4));
			app.setTelefono(rs.getString(5));
			app.setAlta(rs.getDate(6));
			app.setFk_certificado(rs.getString(7));
			result.add(app);
		}
		rs.close();
		st.close();

		return result;
	}

	/**
	 * Consulta que obtiene todos los registros de la tabla tb_aplicaciones.
	 * @return Devuelve los datos como un string en formato JSON
	 * @throws SQLException
	 */
	public static String getApplicationsJSON() throws SQLException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS);
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Date date= null;
				final Timestamp timestamp = rs.getTimestamp(6);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}

				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("responsable", rs.getString(3)) //$NON-NLS-1$
						.add("correo", rs.getString(4)!= null ? rs.getString(4) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("telefono", rs.getString(5) != null ? rs.getString(5) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(6))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(7)) //$NON-NLS-1$
						);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componente la estructura JSON con el listado de aplicaciones", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}


	/**
	 * Obtiene un JSON con el n&uacute;mero de aplicaciones.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsCount() {
		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS_COUNT);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				count = rs.getInt(1);
			}
			jsonObj.add("count", count);  //$NON-NLS-1$
			rs.close();
			st.close();

		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
			jw.writeObject(jsonObj.build());
			jw.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el numero de aplicaciones", e); //$NON-NLS-1$
		}

	    return writer.toString();


	}
	/**
	 * Obtiene una estructura JSON con el numero de aplicaciones que utilizan un certificado.
	 * @param id Identificador del certificado.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsCountByCertificate(final String id) {

		int count = 0;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_COUNT_BYCERT);
			st.setInt(1, Integer.parseInt(id));
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			jsonObj.add("count", count);  //$NON-NLS-1$
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o certificados", e); //$NON-NLS-1$
		}

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}

	    return writer.toString();
	}
	/**
	 * Obtiene una estructura JSON con una p&aacute;gina del listado de aplicaciones.
	 * @param start Elemento por el cual empezar a la p&aacute;gina.
	 * @param total N&uacute;mero de elementos de la p&aacute;gina.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsPag(final String start, final String total) {


		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATIONS_PAG);
			st.setInt(1,Integer.parseInt(start));
			st.setInt(2, Integer.parseInt(total));
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Date date= null;
				final Timestamp timestamp = rs.getTimestamp(6);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}

				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("responsable", rs.getString(3)) //$NON-NLS-1$
						.add("correo", rs.getString(4)!= null ? rs.getString(4) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("telefono", rs.getString(5) != null ? rs.getString(5) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(6))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(7)) //$NON-NLS-1$
						);

			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
		}

		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
			jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}

	    return writer.toString();

	}

	/**
	 * Obtiene la estructura JSON con todas las aplicaciones que utilizan el certificado indicado.
	 * @param id Identificador del certificado.
	 * @return Estructura JSON.
	 */
	public static String getApplicationsByCertificateJSON(final String id) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_APPLICATIONS_BYCERT);
			st.setInt(1, Integer.parseInt(id));

			final ResultSet rs = st.executeQuery();
			while (rs.next()) {

				Date date = null;
				final Timestamp timestamp = rs.getTimestamp(6);
				if (timestamp != null) {
					date = new Date(timestamp.getTime());
				}
				data.add(Json.createObjectBuilder()
						.add("id", rs.getString(1)) //$NON-NLS-1$
						.add("nombre", rs.getString(2)) //$NON-NLS-1$
						.add("responsable", rs.getString(3)) //$NON-NLS-1$
						.add("correo", rs.getString(4)!= null ? rs.getString(4) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("telefono", rs.getString(5) != null ? rs.getString(5) : "") //$NON-NLS-1$ //$NON-NLS-2$
						.add("alta", es.gob.fire.server.admin.tool.Utils.getStringDateFormat(date !=null ? date : rs.getDate(6))) //$NON-NLS-1$
						.add("fk_certificado", rs.getString(7)) //$NON-NLS-1$
						);
			}
			rs.close();
			st.close();
		}
		catch(final Exception e){
			LOGGER.log(Level.SEVERE, "Error al leer los registros en la tabla de aplicaciones y/o certificados", e); //$NON-NLS-1$
		}
		jsonObj.add("AppList", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try  {
			final JsonWriter jw = Json.createWriter(writer);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	    }
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al componer la estructura JSON con el listado de aplicaciones que usan un certificado", e); //$NON-NLS-1$
		}
	    return writer.toString();
	}

	/**
	 * Agrega una nueva aplicaci&oacute;n al sistema.
	 * @param nombre Nombre de la aplicacion.
	 * @param responsable Repsonsable de la aplicaci&oacute;n.
	 * @param email Correo electr&oacute;nico de la aplicaci&oacute;n.
	 * @param telefono N&uacute;mero de te&eacute;lefono de la aplicaci&oacute;n.
	 * @param fkCer certificado en base 64 asignado a la la aplicaci&oacute;n.
	 * @throws SQLException Cuando no se puede insertar la nueva aplicacion en base de datos.
	 * @throws GeneralSecurityException  Cuando no se puede generar el identificador aleatorio de la aplicaci&oacute;n.
	 * @SQL INSERT INTO tb_aplicaciones(id, nombre, responsable, resp_correo, resp_telefono, fecha_alta,fk_certificado ) VALUES (?, ?, ?, ?, ?, ?, ?)
	 */
	public static void createApplication(final String nombre, final String responsable, final String email, final String telefono, final String fkCer)  throws SQLException, GeneralSecurityException {

		final String id = generateId();
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_INSERT_APPLICATION);

		st.setString(1, id);
		st.setString(2, nombre);
		st.setString(3, responsable);
		st.setString(4, email);
		st.setString(5, telefono);
		st.setDate(6, new Date(new java.util.Date().getTime()));
	    st.setString(7, fkCer);

		LOGGER.info("Damos de alta la aplicacion '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$
		st.execute();
		st.close();
	}

	private static String generateId() throws GeneralSecurityException {

		Mac mac;
		try {
			final KeyGenerator kGen = KeyGenerator.getInstance(HMAC_ALGORITHM);
			final SecretKey hmacKey = kGen.generateKey();

			mac = Mac.getInstance(hmacKey.getAlgorithm());
			mac.init(hmacKey);
		}
		catch (final GeneralSecurityException e) {
			LOGGER.severe("No ha sido posible generar una clave aleatoria como identificador de aplicacion: " + e); //$NON-NLS-1$
			throw e;
		}

		return Hexify.hexify(mac.doFinal(), "").substring(0, 12); //$NON-NLS-1$
	}

	/**
	 * Elimina una aplicaci&oacute;n de la base de datos.
	 * @param id Identificador de la aplicaci&oacute;n.
	 * @throws SQLException Cuando ocurre un error durante la operaci&oacute;n.
	 */
	public static void removeApplication(final String id) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_REMOVE_APPLICATION);
		st.setString(1, id);
		LOGGER.info("Damos de baja la aplicacion con el ID: " + id); //$NON-NLS-1$
		st.execute();
		st.close();
	}

	/**
	 * Devuelve una aplicaci&oacute;n registrada en el sistema dado su id.
	 * @param id de la aplicaci&oacute;n a encontrar.
	 * @return Aplicaci&oacute;n encontrada o {@code null} si no se encontr&oacute;.
	 */
	public static Application selectApplication(final String id) {
		Application result = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATION_BYID);
			st.setString(1, id);
			final ResultSet rs = st.executeQuery();
			if (rs.next()){
				result = new Application();
				result.setId(rs.getString(1));
				result.setNombre(rs.getString(2));
				result.setResponsable(rs.getString(3));
				result.setCorreo(rs.getString(4));
				result.setTelefono(rs.getString(5));
				result.setAlta(rs.getDate(6));
				result.setFk_certificado(rs.getString(7));
			}
			rs.close();
			st.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo leer la tabla con el listado de aplicaciones", e); //$NON-NLS-1$
			result = null;
		}
		return result;

	}

	/**
	 * Actualizamos una aplicaci&oacute;n existente.
	 * @param id Id de la aplicaci&oacute;n.
	 * @param nombre Nombre de la aplicaci&oacute;n.
	 * @param responsable Responsable de la aplicaci&oacute;n.
	 * @param email Correo del responasable de la aplicaci&oacute;n.
	 * @param telefono tel&eacute;fono del responsable de la aplicaci&oacute;n.
	 * @param fkCer certificado en base 64 asignado a la la aplicaci&oacute;n.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 */
	public static void updateApplication (final String id, final String nombre, final String responsable, final String email, final String telefono, final String fkCer) throws SQLException{
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_UPDATE_APPLICATION);

		st.setString(1, nombre);
		st.setString(2, responsable);
		st.setString(3, email);
		st.setString(4, telefono);
	    st.setString(5, fkCer);
	    st.setString(6, id);

		LOGGER.info("Actualizamos la aplicacion '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$
		st.execute();
		st.close();
	}


}
