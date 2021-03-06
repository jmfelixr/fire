/**
 * 
 */
  $(document).ready(function(){
	  
	  	

            var totalRecords;
            var recordsPerPage=5;
            var recordsToFetch=recordsPerPage;
            var totalPages;
            var currentPage=1;
            var currentIndex=0;
            
  
                
                /********************** SIN PAGINACIÓN *******************************/
                $.get("processAppRequest.jsp?requestType=countRecords",function(data){
              
                    var JSONData = JSON.parse(data);
              
                    totalRecords = JSONData.count;
               
                    if(totalRecords > 0){
                    	$("#data").html("");
                    	$.get("processAppRequest.jsp?requestType=All", function(data){
                        	var JSONData = JSON.parse(data);                
                        	  printApplicationTable(JSONData, totalRecords);                        

                        });
                    }
                    else{                    	
                    	return;
                    }              
                   
                });
                
                
                
            /********************** SIN PAGINACIÓN FIN*******************************/
            function printApplicationTable(JSONData, recordsToFetch){
            
            	var htmlTableHead = "<table id='appTable' class='admin-table'><thead><tr><td>Aplicaci&oacute;n</td><td>ID</td><td>Responsable</td><td>Fecha Alta</td><td id='acciones'>Acciones</td></tr></thead><tbody>";
            	var htmlTableBody = "";
            	var htmlTableFoot = "</tbody></table>";
       		        		
            	for(i = 0; i < recordsToFetch; ++i){
            		htmlTableBody = htmlTableBody + "<tr><td>" + dataUndefined(JSONData.AppList[i].nombre) + "</td><td>" + dataUndefined(JSONData.AppList[i].id) + 
            		"</td><td>" + dataUndefined(JSONData.AppList[i].responsable) + "<br>";
            		
        		if ( JSONData.AppList[i].correo != null && dataUndefined(JSONData.AppList[i].correo) != "" && JSONData.AppList[i].correo != "") { 
        			htmlTableBody=htmlTableBody + "<a href='mailto://" + JSONData.AppList[i].correo + "'>" + JSONData.AppList[i].correo + "</a>";
        			} 
        		if (JSONData.AppList[i].telefono != null && dataUndefined(JSONData.AppList[i].telefono) != "" && JSONData.AppList[i].telefono != "") { 
        			htmlTableBody=htmlTableBody + "(<a href='tel://" + JSONData.AppList[i].telefono + "'>" + JSONData.AppList[i].telefono + "</a>)";
        			} 
//        		fecAlta=new Date(JSONData.AppList[i].alta);      		
//        		htmlTableBody=htmlTableBody+"</td><td>"+convertDateFormat(fecAlta)+"</td>";
        		htmlTableBody = htmlTableBody + "</td><td>" + JSONData.AppList[i].alta + "</td>";
        		htmlTableBody = htmlTableBody + "<td>";
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?id-app=" + JSONData.AppList[i].id + "&op=0'><img src='../resources/img/details_icon.png'/></a>";
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?id-app=" + JSONData.AppList[i].id + "&op=2'><img src='../resources/img/editar_icon.png'/></a>";        		
        		htmlTableBody = htmlTableBody + "<a href='../deleteApp?id-app=" + JSONData.AppList[i].id + "'><img src='../resources/img/delete_icon.png' onclick='return confirmar(\"" + JSONData.AppList[i].nombre + "\")'/></a>";        		
        		htmlTableBody = htmlTableBody + "</td></tr>"; 
        		
        		currentIndex++;
            	}
            	$("#data").append(htmlTableHead + htmlTableBody + htmlTableFoot); 
            	
            }
            
            function  convertDateFormat(date){
            	 var dd = date.getDate();
                 var mm = date.getMonth() + 1; 
                 var yyyy = date.getFullYear();
                 if(dd < 10) {
                     dd = '0' + dd;
                 } 
                 if(mm < 10) {
                     mm = '0' + mm;
                 } 
                 return dd + "/" + mm + "/" + yyyy;
            }
              
            function dataUndefined(data){
            	if(typeof data === "undefined"){
            		return "";
            	}
            	else{
            		return data;
            	}
            }
                     	  
	 
        });
  
  function confirmar(nombreApp) { 
	   if (confirm('¿Está seguro de eliminar la aplicación '+ nombreApp + '?')) {
		
		   document.tuformulario.submit();     
	      return true;
	   }
	   return false;
}