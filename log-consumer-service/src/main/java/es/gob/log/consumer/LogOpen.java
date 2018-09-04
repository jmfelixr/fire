package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.log.consumer.service.ConfigManager;


public class LogOpen {

	private static final Logger LOGGER = Logger.getLogger(LogOpen.class.getName());

	private final Path path;
	private  LogInfo linfo;
	private  LogReader reader;
	private AsynchronousFileChannel channel;

	/**
	 *
	 */
	public LogOpen (final String path) {
		this.path = Paths.get(path);
	}

	/**
	 * Busca el fichero con extensi&oacute;n .loginfo cuyo nombre m&aacute;s se le parezca al fichero seleccionado y si lo encuentra lo carga con las
	 *	propiedades leidas de dicho fichero loginfo, en caso contrario al crear el objeto loginfo lo inicializa
	 *	con las propiedades por defecto.
	 * @return Devuelve: array de bytes con la informaci&oacute;n obtenida del fichero .loginfo perteneciente o
	 * asociado al fichero de log indicado como par&aacute;metro,  {@code null}
	 *  si la ruta no existente al fichero de log indicada en el fichero log_consumer_config.properties
	 *  en la propiedad logs.dir no existe.
	 * @throws IOException
	 */
	public final byte[] openFile(final String logFileName) throws IOException  {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final String [] fileNamesLoginfo = {null,null,null,null,null} ;
		File f;
		try {
			f = ConfigManager.getInstance().getLogsDir().getCanonicalFile();
		} catch (final IOException e1) {
			LOGGER.log(Level.SEVERE, "Error al leer el fichero en la ruta indicada ".concat(e1.getMessage())); //$NON-NLS-1$
			throw new IOException();
		}

		String nameLogInfo = logFileName.replace(LogConstants.FILE_EXT_LOG, ""); //$NON-NLS-1$

		if(f.exists()) {
			//Obtenemos un listado filtrado con s&oacute;lo los ficheros con extensi&oacute;n .loginfo
			final File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
						final int dot = name.lastIndexOf('.');
		                final String ext = name.substring(dot);
		                if(ext.equalsIgnoreCase(LogConstants.FILE_EXT_LOGINFO)) {
		                	return true;
		                }
		                return false;
					}
					return false;
				}
			});


			if(files.length > 0) {
				//Obtenemos el nombre del fichero .loginfo que mas se aproxime al nombre del fichero de log
				for (int i = 0; i < files.length; i++){

					if (logFileName.startsWith(files[i].getName().replace(LogConstants.FILE_EXT_LOGINFO, ""))) { //$NON-NLS-1$
						fileNamesLoginfo[0] = files[i].getName();
					}
					else if(files[i].getName().matches("^[0-9][A-Z][a-z]".concat(nameLogInfo).concat("*.loginfo"))) {//coincide 100% //$NON-NLS-1$ //$NON-NLS-2$
						fileNamesLoginfo[1] = files[i].getName();
					}
					else if(files[i].getName().matches("^[0-9][A-Z][a-z]"+ nameLogInfo.trim().replace("_", "") +"*.loginfo") ) {//coincide 75% //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						fileNamesLoginfo[2] = files[i].getName();
					}
					else if(files[i].getName().matches("^[0-9][A-Z][a-z]"+nameLogInfo.trim()+"*.loginfo") && files[i].getName().replace(LogConstants.FILE_EXT_LOG, "").length() == nameLogInfo.length() ) {//coincide 50% //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						fileNamesLoginfo[3] = files[i].getName();
					}
					else if(files[i].getName().matches("^[0-9][A-Z][a-z]*.loginfo")) {//coincide 0% //$NON-NLS-1$
						fileNamesLoginfo[4] = files[i].getName();
					}

				}

				if( fileNamesLoginfo.length > 0) {
					for(int i = 0; i < fileNamesLoginfo.length; i++) {
						if(fileNamesLoginfo[i] != null && !"".equals(fileNamesLoginfo[i])) { //$NON-NLS-1$
							nameLogInfo = fileNamesLoginfo[i];
							break;
						}
					}
				}
			}
			// leer fichero loginfo y crear entidad LogInfo con los datos obtenidos del fichero
			try {
				this.linfo = new LogInfo();
				final String pathLogInfo = ConfigManager.getInstance().getLogsDir().getAbsolutePath().concat(File.separator).concat(nameLogInfo);
				final File fLogInfo =  new File(pathLogInfo).getCanonicalFile();
				if(fLogInfo.exists()) {
					// Abrir fichero log, se inicializa el canal y el lector.
					try(final FileInputStream fis = new FileInputStream(fLogInfo);){
						this.linfo.load(fis);
						fis.close();
					}
				}

				this.channel = AsynchronousFileChannel.open(this.path,StandardOpenOption.READ);
				this.reader = new FragmentedFileReader(this.channel, this.linfo.getCharset());
				this.reader.load(0L);
			}
			catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "Error al leer el fichero .loginfo en la ruta indicada ".concat(e.getMessage())); //$NON-NLS-1$
				throw new IOException();
			}

			//Generamos el resultado en formato JSON de la salida
			final StringWriter writer = new StringWriter();
			String charset = ""; //$NON-NLS-1$
			if(this.linfo.getCharset() != null && this.linfo.getCharset().name() != null && !"".equals(this.linfo.getCharset().name())) { //$NON-NLS-1$
				charset = this.linfo.getCharset().name();
			}
			String levels = ""; //$NON-NLS-1$

			if(this.linfo.getLevels() != null && this.linfo.getLevels().length > 0) {
				for (int i = 0; i < this.linfo.getLevels().length; i++) {
					if(i < this.linfo.getLevels().length - 1) {
						levels += this.linfo.getLevels()[i].concat(","); //$NON-NLS-1$
					}
					else {
						levels += this.linfo.getLevels()[i];
					}
				}
			}
			String dateTimeFormat = ""; //$NON-NLS-1$
			if (this.linfo.getDateFormat() != null && !"".equals(this.linfo.getDateFormat())) { //$NON-NLS-1$
				dateTimeFormat = this.linfo.getDateFormat();
			}
			final String date = this.linfo.hasDateComponent() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
			final String time = this.linfo.hasTimeComponent() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$

				data.add(Json.createObjectBuilder()
						.add("Charset",charset) //$NON-NLS-1$
						.add("Levels", levels) //$NON-NLS-1$
						.add("Date", date) //$NON-NLS-1$
						.add("Time", time) //$NON-NLS-1$
						.add("DateTimeFormat", dateTimeFormat)); //$NON-NLS-1$
				jsonObj.add("LogInfo", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(writer);
		        jw.writeObject(jsonObj.build());
		        jw.close();


		    try {
				return writer.toString().getBytes(this.linfo.getCharset().name());
			} catch (final UnsupportedEncodingException e) {
				LOGGER.log(Level.SEVERE, "Error al pasar a bytes el resultado :".concat(e.getMessage())); //$NON-NLS-1$
				throw new UnsupportedEncodingException();
			}
		}

		return null;
	}

	/**
	 * Obtiene la entidad LogInfo con la informaci&oacute;n cargada
	 * @return
	 */
	public final LogInfo getLinfo() {
		return this.linfo;
	}

	/**
	 * Obtiene  LogReader del fichero abierto en modo lectura, inicializado en la posici&oacute;n 0
	 * @return
	 */
	public final LogReader getReader() {
		return this.reader;
	}

	/**
	 * Obtiene  AsynchronousFileChannel, canal de fichero abierto en modo lectura
	 * @return
	 */
	public final AsynchronousFileChannel getChannel() {
		return this.channel;
	}




}