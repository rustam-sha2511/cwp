function Factory() {
    this.createObject = function(type) {
        var factoryObject;
 
        if (type === "CUSTOM_FILTER") {
            factoryObject = new CustomCaseFilter();
        } else if (type === "OWNER_FILTER") {
            factoryObject = new OwnerCaseFilter();
        } else if (type === "DISPLAY_CASE") {
            factoryObject = new DisplayCaseFilter();
        } else if (type === "VIEW_APPOINTMENTS") {
            factoryObject = new ViewAppointmentsFilter();
        }
 
        factoryObject.type = type;
 
        /*employee.say = function () {
            log.add(this.type + ": rate " + this.hourly + "/hour");
        }*/
		
		/*factoryObject.perform = function(msg) {
			
		}*/		
        return factoryObject;
    }
}
    
var CustomCaseFilter = function () {
	return {
        execute: function (actingObject) { 
			console.log('Executing for display custom case filter logic');
			
		}
    }
};

var OwnerCaseFilter = function () {
	return {
        execute: function (actingObject) { 
			console.log('Executing for display owner case filter logic');
			var currentConvObj = [];
			$('.app-content .item-container,.app-content .time-indicator').each(function( index ) {
			  if($(this).hasClass('item-container-bot')){
				console.log( index + " Bot: " + $( this ).text() );
				item = {};
		        item ["message"] = $( this ).text();
		        item ["messageType"] = "ITEM_BOT";
		        item ["messageDataType"] = "TEXT";
		        currentConvObj.push(item);
			  } else if($(this).hasClass('item-container-user')){
				console.log( index + " User: " + $( this ).text() );
				item = {};
		        item ["message"] = $( this ).text();
		        item ["messageType"] = "TIME_INDICATOR";
		        item ["messageDataType"] = "TEXT";
		        currentConvObj.push(item);
			  } else if($(this).hasClass('time-indicator')){
				console.log( index + " Time: " + $( this ).text() );
				item = {};
		        item ["message"] = $( this ).text();
		        item ["messageType"] = "ITEM_USER";
		        item ["messageDataType"] = "DATE";
		        currentConvObj.push(item);
			  }				  
			});
			
			$.ajax({
				method: "POST",
				url: "/CaseWorkerPortal/saveAliceConversation",
				data: JSON.stringify(currentConvObj),
				contentType:"application/json"
			})
			.done(function( msg ) {
				console.log("success when alice conversation object created");
				// Add filter post successful navigation
				$('#inputTabularData_filter input[type="search"]').val(actingObject).triggerHandler('keydown');
			});
		}
    }
};
 
var DisplayCaseFilter = function () {
    return {
        execute: function (actingObject) { 
			console.log('Executing for display case filter logic');
			
		}
    }
};
 
var ViewAppointmentsFilter = function () {
    return {
        execute: function (actingObject) { 
			console.log('Executing for view appointment filter logic');
			
		}
    }
};

// Trigger ALICE command for UI 
function runAliceCommand(command) {
    var factory = new Factory();
	var factoryInfo = findFactory.extractInfo(command);
	factory.createObject(factoryInfo.objName).execute();
    
}

function findInParsed(html, selector){
    var check = $(selector, html).get(0);
    if(check)
        return $(check);
    check = $(html).filter(selector).get(0)
    return (check)? $(check) : false;
}