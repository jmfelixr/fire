package es.gob.log.consumer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletResponse;

public class LogFiles {

	private static final String FILE_EXT_LOGINFO = LogConstants.FILE_EXT_LOGINFO;
	private static final String FILE_EXT_LCK = LogConstants.FILE_EXT_LCK;
	private static final String DIR_LOGS = LogConstants.DIR_FILE_LOG;
	private static final Logger LOGGER = Logger.getLogger(LogFiles.class.getName());

	public LogFiles() {

	}
	/**
	 * M&eacute;todo de consulta de ficheros de logs.
	 * @return Array de bytes que contiene cadena de caracteres en formato JSON indicando el nombre de los ficheros log,
	 *  fecha de &uacute;ltima actualizaci&oacute;n sin formato (long) y su tama&ntilde;o en bytes
	 * @throws IOException
	 */
	public  byte[] getLogFiles()  {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		byte[] result = null;
		final File f = new File(DIR_LOGS);
		if(f.exists()) {
			final File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
						final int dot = name.lastIndexOf('.');
		                final String ext = name.substring(dot);
		                if(!ext.equalsIgnoreCase(FILE_EXT_LOGINFO) && !ext.equalsIgnoreCase(FILE_EXT_LCK)) {
		                	return true;
		                }
		                return false;
					}
					return false;
				}
			});
			if( files.length > 0) {
				for (int i = 0; i < files.length; i++){
					data.add(Json.createObjectBuilder()
							.add("Name",files[i].getName()) //$NON-NLS-1$
							.add("Date",files[i].lastModified()) //$NON-NLS-1$
							.add("Size",files[i].length()) //$NON-NLS-1$
					);
				}
				jsonObj.add("FileList", data); //$NON-NLS-1$
				final StringWriter writer = new StringWriter();
				try(final JsonWriter jw = Json.createWriter(writer)) {
			        jw.writeObject(jsonObj.build());
			        jw.close();
			    }
				result = writer.toString().getBytes(Charset.defaultCharset());
			}
			else {
				data.add(Json.createObjectBuilder()
						.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
						.add("Message", "No se ha podido obtener la lista de ficheros log.")); //$NON-NLS-1$//$NON-NLS-2$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final StringWriter writer = new StringWriter();
				try(final JsonWriter jw = Json.createWriter(writer)) {
			        jw.writeObject(jsonObj.build());
			        jw.close();
			    }
				result = writer.toString().getBytes(Charset.defaultCharset());

			}

		}

		return result;
	}
}