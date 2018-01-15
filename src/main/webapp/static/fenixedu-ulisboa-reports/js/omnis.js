function messageAlert(title, message) {
	bootbox.dialog({
		title : title,
		message : message,
		buttons : {
			success : {
				label : "Ok",
				className : "btn-primary",
			}
		}
	});
}

window.jQueryClosures = []

function initSelect2(element_id, elements_data_source, element_init_val) {
	var func = function() {
		$(element_id).select2({
			data : elements_data_source
		}).select2('val', element_init_val);
	};
	window.jQueryClosures.push(func);
}

function initSelect2Multiple(element_id, elements_data_source, element_init_values) {

	var func = function() {
		var select2 = $(element_id).select2({
			data : elements_data_source
		});
		if (element_init_values != undefined)
		{
			select2.select2('val', element_init_values);
		}
	};
	window.jQueryClosures.push(func);
}


$(document).ready(function() {
	for (var i = 0; i < window.jQueryClosures.length; i++) {
		window.jQueryClosures[i].call();
	}
});

function createAngularPostbackFunction(angular_scope) {
	return function(model) {

		angular_scope.$apply();
		var form = $('form[name="' + angular_scope.form.$name + '"]');
		var previousActionURL = form.attr("action");
		form.submit = function(e) {
			var postData = $(this).serializeArray();
			var formURL = $(this).attr("action");
			$.ajax({
				url : formURL,
				type : "POST",
				data : postData,
				success : function(data, textStatus, jqXHR) {
					angular_scope.object = data;
					angular_scope.isUISelectLoading = undefined;
					angular_scope.$apply();
				},
				error : function(jqXHR, textStatus, errorThrown) {
					messageAlert("Erro", jqXHR.responseText);
				},
			});
		};

		form.attr("action", form.find('input[name="postback"]').attr('value'));
		form.submit();
		form.attr("action", previousActionURL);
	};
}


function createDataTables(tableid, showsearchbox, showtools, pagination, pagecontext,i18nurl) {
    createDataTablesWithSortSwitch(tableid, showsearchbox, showtools, pagination, true /* sortable */, pagecontext, i18nurl);
}

function createDataTablesWithSortSwitch(tableid, showsearchbox, showtools, pagination, sortable, pagecontext,i18nurl) {
	var dom = "";
	if (showsearchbox == true && showtools == true) {
		dom = '<"col-sm-5"l><"col-sm-3"f><"col-sm-3"B>rtip'; //FilterBox = YES && ExportOptions = YES
	} else if (showsearchbox == true && showtools == false) {
		dom = '<"col-sm-6"l><"col-sm-6"f>rtip'; // FilterBox = YES && ExportOptions = NO
	} else if (showsearchbox == false && showtools == true) {
		dom = 'T<"clear">lrtip'; // FilterBox = NO && ExportOptions = YES
	} else {
		dom = '<"col-sm-6"l>rtip'; // FilterBox = NO && ExportOptions = NO
	}
	var table = $('#'+tableid)
			.DataTable({language : {
				url : i18nurl,			
			},
			"bSort" : sortable,
			"bDeferRender" : true,
			"bPaginate" : pagination,
			"dom" : dom, 
            "buttons": [
                'copyHtml5',
                'excelHtml5',
                'csvHtml5',
                'pdfHtml5'
            ],
			"tableTools" : {
				"sSwfPath" : pagecontext + "/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
			}
	});
	
	table.columns.adjust().draw();
	
	$('#' + tableid +' tbody').on('click', 'tr', function() {
		$(this).toggleClass('selected');
	});
}
