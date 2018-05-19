// Initialize Variables
$(document).ready(function(){
	var button = $("#voice-icon");
	var overlay = $("#overlay");
    var popup = $("#popup");
	$(button).on('click', function(){
		$(overlay).css('display','block');
        $(popup).css('display','block');
        var closeIcon = '<i class="fa fa-times-circle-o pull-right"></i>';
	});
});