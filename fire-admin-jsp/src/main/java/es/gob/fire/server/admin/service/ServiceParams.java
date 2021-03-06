package es.gob.fire.server.admin.service;

public class ServiceParams {

	public static final String PARAM_OP = "op"; //$NON-NLS-1$

	public static final String PARAM_NAMESRV = "name-srv"; //$NON-NLS-1$

	public static final String PARAM_URL = "url"; //$NON-NLS-1$

	public static final String PARAM_VERIFY_SSL = "verifyssl"; //$NON-NLS-1$

	public static final String PARAM_FILENAME = "fname";//$NON-NLS-1$

	public static final String PARAM_NLINES = "nlines";//$NON-NLS-1$

	public static final String PARAM_TXT2SEARCH = "search_txt";//$NON-NLS-1$

	public static final String PARAM_SEARCHDATE = "search_date";//$NON-NLS-1$

	public static final String PARAM_CHARSET = "Charset";//$NON-NLS-1$

	public static final String PARAM_PARAM_LEVELS = "Levels";//$NON-NLS-1$

	public static final String PARAM_DATE = "Date";//$NON-NLS-1$

	public static final String PARAM_TIME = "Time";//$NON-NLS-1$

	public static final String PARAM_DATETIME = "DateTimeFormat";//$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String PARAM_START_DATETIME = "start_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la fecha y hora de b&uacute;squeda */
	static final String PARAM_END_DATETIME = "end_date"; //$NON-NLS-1$

	/***Par&aacute;metro que indica el texto de b&uacute;squeda */
	static final String PARAM_LEVEL = "level"; //$NON-NLS-1$

	/***Par&aacute;metro que indica si se tiene que reiniciar*/
	public static final String PARAM_RESET ="reset"; //$NON-NLS-1$

	/***Par&aacute;metro que indica menasaje de texto a mostrar*/
	public static final String PARAM_MSG ="msg"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de un JSON de error. */
	public static final String SESSION_ATTR_ERROR_JSON = "ERROR_JSON"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de una respuesta JSON. */
	public static final String SESSION_ATTR_JSON = "JSON"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado de un JSON con la informaci&oacute;n del log. */
	public static final String SESSION_ATTR_JSON_LOGINFO = "JSON_LOGINFO"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del objeto de consulta de logs. */
	public static final String SESSION_ATTR_LOG_CLIENT = "LOG_CLIENT"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del valor bandera que indica que el usuario
	 * completo correctamente la autenticaci&oacute;n. */
	public static final String SESSION_ATTR_INITIALIZED = "initializedSession"; //$NON-NLS-1$

	/** Atributo de sesi&oacute;n para el guardado del nombre de usuario logueado. */
	public static final String SESSION_ATTR_USER = "user"; //$NON-NLS-1$

	/***Par&aacute;metro que indica la consulta seleccionada para las estad&iacute;sticas */
	public static final String PARAM_SELECT_QUERY = "select_query";//$NON-NLS-1$



}
