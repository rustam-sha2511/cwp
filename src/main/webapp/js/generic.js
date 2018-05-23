// Initialize Variables
$(document).ready(function(){
	var button = $("#voice-icon");
	var overlay = $("#overlay");
    var popup = $("#popup");
    var minimize = $("#minimizeAlice");
	$(button).on('click', function(){
		$(overlay).css('display','block');
        $(popup).css('display','block');
	});
	
	$(minimize).on('click', function(){
		$(overlay).css('display','none');
        $(popup).css('display','none');
        $(button).css('display','block');
	});
});