package es.gob.log.consumer.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

public class LogFilteredServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogFilteredServiceManager.class.getName());

	public final static byte[] process(final HttpServletRequest req) {

		byte[] result = null;
		final Criteria crit = new Criteria();
		// Obtenemos los datos enviados al servicio.
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);

		if(req.getParameter(ServiceParams.START_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.START_DATETIME))) { //$NON-NLS-1$
			final Long start_dateTime = new Long( Long.parseLong(req.getParameter(ServiceParams.START_DATETIME)));
			if(start_dateTime.longValue() > 0L) {
				crit.setStartDate(start_dateTime.longValue());
			}
		}
		if(req.getParameter(ServiceParams.END_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.END_DATETIME))) { //$NON-NLS-1$
			final Long end_datetime = new Long( Long.parseLong(req.getParameter(ServiceParams.END_DATETIME)));
			if(end_datetime.longValue() > 0L) {
				crit.setEndDate(end_datetime.longValue());
			}
		}
		if(req.getParameter(ServiceParams.LEVEL) != null && !"".equals(req.getParameter(ServiceParams.LEVEL))){ //$NON-NLS-1$
			final String level = req.getParameter(ServiceParams.LEVEL);
			crit.setLevel(Integer.parseInt(level));
		}
		//Obtenemos los datos guardados de sesion
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$

		try {
			if(filePosition != null && filePosition != Long.valueOf(0L)) {
				reader.load(filePosition.longValue());
			}
			final LogFilter filter = new LogFilter(info);
			filter.load(reader);
			filter.setCriteria(crit);
			result = filter.filter(Integer.parseInt(sNumLines));
			session.setAttribute("FilePosition", Long.valueOf(0L)); //$NON-NLS-1$
			session.setAttribute("Reader", reader); //$NON-NLS-1$

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvalidPatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

}