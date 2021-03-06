
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.List" %>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%

	if (session == null || !Boolean.parseBoolean((String) session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED))) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String id = request.getParameter("id-app");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$
	final List<CertificateFire> lCert = CertificatesDAO.selectCertificateAll();
	CertificateFire cert=null;
	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nueva aplicacion
	// op = 2 -> editar aplicacion
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	String certDataPrincipal = "";//$NON-NLS-1$
	String certDataBkup = "";//$NON-NLS-1$
	switch (op) {
		case 0:
			title = "Ver la aplicaci&oacute;n " + id;//$NON-NLS-1$
			subTitle = ""; //$NON-NLS-1$
			break;
		case 1:
			title = "Alta de nueva aplicaci&oacute;n"; //$NON-NLS-1$
			subTitle = "Inserte los datos de la nueva aplicaci&oacute;n."; //$NON-NLS-1$
			break;
		case 2:
			title = "Editar la aplicaci&oacute;n " + id; //$NON-NLS-1$
			subTitle = "Modifique los datos que desee editar"; //$NON-NLS-1$
			break;
		default:
			response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
			return;
	}
	
	Application app = null;
	if (id != null) {
		app = AplicationsDAO.selectApplication(id);	
	}
	if (app == null) {
		app = new Application();	
	}

	if (app.getFk_certificado() != null && !"".equals(app.getFk_certificado())) { //$NON-NLS-1$
		cert=CertificatesDAO.selectCertificateByID(app.getFk_certificado());
		if (cert.getX509Principal() != null){
			final String[] datCertificate=cert.getX509Principal().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				certDataPrincipal += datCertificate[i] + "</br>";//$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = cert.getX509Principal().getNotAfter();		
			certDataPrincipal += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
		}
		if (cert.getX509Backup() != null) {
			final String[] datCertificate = cert.getX509Backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++){
				certDataBkup += datCertificate[i] + "</br>"; //$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = cert.getX509Backup().getNotAfter();		
			certDataBkup += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
		}
	}	
		
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script>var op=<%=  op%>;	
		$(function() {
			$("select").change(function (){
		    	var opSel="0";
		    	$("#cert-prin").empty();
	      		$("#cert-resp").empty();
		      	$("select option:selected").each(function(){
		      		opSel=$(this).val();
		      	});
		      	if(opSel!="0"){
		      		$.get("processAppRequest.jsp?requestType=getCertificateId&id-cert=" + opSel, function(data){	      		
			      		var certificados=data.split("§");		      		
			      		if(certificados[0] != null && typeof certificados[0] != "undefined" && certificados[0].trim() != "--"){		      			
			      			$("#cert-prin").html(certificados[0]);
			      		}
			      		if(certificados[1] != null && typeof certificados[1] != "undefined" && certificados[1].trim() != "--"){		      			
			      			$("#cert-resp").html(certificados[1]);
			      		}
			      	});
		      	}		      			      	
			});	          
		});
	</script>
	<script src="../resources/js/validateAplications.js" type="text/javascript"></script>
</head>

<body>
	<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%=  subTitle %>
			</p>
		</div>			
		<p>Los campos con * son obligatorios</p>
			<form id="frmApplication" method="POST" action="../newApp?iddApp=<%=  id %>&op=<%=  op %>"  onsubmit="isCert()">
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-app" style="color: #404040">* Nombre de aplicaci&oacute;n</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					
						<input id="nombre-app" class="edit-txt" type="text" name="nombre-app" style="width: 80%;margin-top:3px;" 
						value="<%= (request.getParameter("name") != null) ? request.getParameter("name") : (app.getNombre()!=null)?app.getNombre() : "" %>"> 
						
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-resp" style="color: #404040" >* Responsable</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
				<input id="nombre-resp" class="edit-txt" type="text" name="nombre-resp" style="width: 80%;margin-top:3px;"
						value="<%=  request.getParameter("res") != null ? request.getParameter("res") : (app.getResponsable() != null) ? app.getResponsable() : ""%>">				
				</div>						
			</div>	
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="email-resp" style="color: #404040">Correo electrónico</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="email-resp" class="edit-txt" type="text" name="email-resp" style="width: 80%;margin-top:3px;"
						value="<%=  request.getParameter("email") != null ? request.getParameter("email") : (app.getCorreo()!=null) ? app.getCorreo() : "" %>">											
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="telf-resp" style="color: #404040">Telf. Contacto</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
				
				<input id="telf-resp" class="edit-txt" type="text" name="telf-resp" style="width: 80%;margin-top:3px;" 
						value="<%=  request.getParameter("tel")!= null ? request.getParameter("tel") : (app.getTelefono() != null) ? app.getTelefono() : ""%>">									
				</div>						
			</div>
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<label for="id-certificate" style="color: #404040;">* Seleccionar certificado</label><br>	
					</div>	
					<div  style="display: inline-block; width: 30%;margin: 3px;">				
					<%if(op != 0){ %>							
						<select id="id-certificate" name="id-certificate" class="edit-txt">
							<option value="0"></option> 			
						<% for (CertificateFire cer:lCert){ 
							if(op == 1){%>													
							<option value="<%= cer.getId()%>"><%= cer.getNombre() %></option>  
							<%}
							else{%>
							<option value="<%= cer.getId()%>" <%= cert.getId().equals(cer.getId()) ? "selected='selected'" : "" %>><%= cer.getNombre() %></option>  
							<% }
						} %>			    		  			   
				  		</select>				  					
					<%}else{%>						
						<input id="id-certificate" class="edit-txt" type="text" name="id-certificate" style="width: 80%;margin-top:3px;" 
						value="<%= cert.getNombre()%>"/>																								
					<%} %>			
					</div>
			
				</div>
			
				<div style="margin: auto;width: 100%;padding: 3px;">
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>															
						</div>											
						<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<%if(certDataPrincipal!=null && !"".equals(certDataPrincipal)){ %>
								<p><%= certDataPrincipal %></p>							
							<%}%>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>																					
						</div>				
						<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<%if(certDataBkup!=null && !"".equals(certDataBkup)){ %>
								<p><%= certDataBkup %></p>						
							<%}%>
						</div>					
					</div>
				</div>									
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 60%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
				<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='AdminMainPage.jsp'"/>
<!-- 					<a class="menu-btn" href="AdminMainPage.jsp" >Volver a la p&aacute;gina de administraci&oacute;n</a> -->
				</div>
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear aplicaci&oacute;n" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$
					final String tit= (op == 1 ) ? "Crea nueva aplicación" : "Guarda las modificaciones realizadas";   //$NON-NLS-1$ //$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 35%;margin: 3px">
			   			<input class="menu-btn" name="add-app-btn" type="submit" value="<%= msg %>" title="<%= tit %>" >
			   		</div>
		   		<% } %>
			   					   		
		   	</div>	
		</fieldset>

		</form>
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("nombre-app").disabled = <%=  op == 0 %>
			document.getElementById("email-resp").disabled = <%=  op == 0 %>
			document.getElementById("nombre-resp").disabled = <%=  op == 0 %>
			document.getElementById("telf-resp").disabled = <%=  op == 0 %>
			document.getElementById("id-certificate").disabled = <%=  op == 0 %>
		
		</script>
   	</div>
</body>
</html>
