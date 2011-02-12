
$(init);

function init() {
	var APPEAR_TIME = 1000;
	
	var roomListItems = $('#roomList > li > a');
	var totalWidth = 0;

	roomListItems.each(function() {
		totalWidth += $(this).outerWidth();
	});
	
	var windowWidth = $(window).width();
	var spacing = (windowWidth - totalWidth) / (roomListItems.length + 1) / windowWidth;
	var currWidth = 0;
	
	roomListItems.each(function(index) {
		var self = this;
		var width = $(this).outerWidth() / windowWidth;
		var height = $(this).outerHeight() / $('#content').outerHeight();
		
		$(this)
			.css('left', (currWidth + spacing) * 100 + '%')
			.css('top', ($('#content').outerHeight() - $(this).outerHeight()) / 2);

		setTimeout(function() {
			$(self).parents('li').animate({opacity: 1}, APPEAR_TIME);
		}, index * 250);
	
		currWidth += spacing + width;
	});
}