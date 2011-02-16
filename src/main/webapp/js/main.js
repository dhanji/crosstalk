
var TRANSLATE3D = false;

if (navigator.userAgent.indexOf('iPhone') >= 0 || navigator.userAgent.indexOf('iPad') >= 0) {
	TRANSLATE3D = true;
	
	$('html').removeClass('notIos');
	$('html').addClass('ios');
}

var touch = null;

$(init);

function init() {
	if ($('#mobileCheck').css('display') != 'none') {
		var APPEAR_TIME = 1000;
	
		var roomListItems = $('#roomList > li > a');
	
		// Truncate text in room titles	
		$('h1', roomListItems).each(function() {
			var CHAR_LIMIT = 22;
			
			var originalText = $(this).text();
			var modifiedText = originalText;
			modifiedText = modifiedText.substring(0, CHAR_LIMIT);
			
			if (modifiedText.length < originalText.length) {
				modifiedText = modifiedText.replace(/ [^ ]*$/, '');
				modifiedText += '&hellip;';
			}
			
			$(this).html(modifiedText);
		});
		
		createNav();
	
		// Vertically center stuff
		orientationChange();
		
		// Focus on active cards
		var activeRooms = $('#roomList .active');
		moveRoomListTo(activeRooms.eq(0), activeRooms.eq(activeRooms.length - 1));
	
		// Fade-in cards	
		roomListItems.each(function(index) {
			var self = this;
			
			setTimeout(function() {
				$(self).parents('li').animate({opacity: 1}, APPEAR_TIME);
			}, index * 100);
		});
	
		$('#content').get(0).addEventListener('touchmove', touchMove, false);
		$('#content').get(0).addEventListener('touchstart', touchStart, false);
		$('#content').get(0).addEventListener('touchend', touchEnd, false);
		$('#roomList a').each(function() {
			this.addEventListener('touchstart', cancelEvent, false);
			this.addEventListener('touchend', cancelEvent, false);
		});
		
		window.onorientationchange = orientationChange;
	}
}

// Center stuff when orientation changes
function orientationChange() {
	// Vertically center cards on page
	var contentTop = ($(window).height() - $('header').outerHeight() - $('#content').outerHeight() - 200) * 0.6;	
	$('#content').css('top', contentTop + $('header').outerHeight());
	
	// Vertically center call to action
	var headerHeight = $('header').outerHeight();
	$('#callToAction').css('top', headerHeight + contentTop * 0.4);
}

function cancelEvent(event) {
	event.preventDefault();
}

function touchStart(event) {
	event.preventDefault();

	$('#content').addClass('touching');

	if (touch == null) {
		touch = {
			id: event.touches[0].identifier,
			startX: event.touches[0].pageX,
			startContentX: $('#content').data('targetX') || 0,
			startTime: new Date().getTime()
		}
	}
}

function touchEnd(event) {
	event.preventDefault();

	for (var i = 0; i < event.changedTouches.length; i++) {
		if (touch.id == event.changedTouches[i].identifier) {
			$('#content').removeClass('touching');
			$('#content').addClass('touchGlide');
			
			if (typeof touch.lastX != 'undefined' && typeof touch.secondLastX != 'undefined') {
				moveRoomListTo((touch.secondLastX - touch.lastX) * 10);
				setTimeout(function() {
					$('#content').removeClass('touchGlide');
				}, 500);
			}
			
			touch = null;
		}
	}
}

function touchMove(event) {
	event.preventDefault();

	for (var i = 0; i < event.touches.length; i++) {
		if (touch.id == event.touches[i].identifier) {
			var targetX = touch.startContentX + (event.touches[i].pageX - touch.startX);

			$('#content').css('-webkit-transform', 'translate3d(' + targetX + 'px,0px,0px)');
			$('#content').data('targetX', targetX);
			
			if (typeof touch.lastX != 'undefined') {
				touch.secondLastX = touch.lastX;
			}
			
			touch.lastX = event.touches[i].pageX;
			
			break;
		}
	}
}

function createNav() {
	var roomListItems = $('#roomList > li');
	var navOl = $('nav ol');
	var currDay = null;
	
	roomListItems.each(function(index) {
		var self = this;
		
		var listItem = $('<li><a href="#" title="' + $('h1', this).text() + '">' + $('h1', this).text() + '</a></li>');
		
		if ($(this).hasClass('active')) {
			listItem.addClass('active');
		}
		
		listItem.prependTo(navOl);
		
		var time = $('time', this).attr('datetime');
		var year = time.match(/^([^\-]+)/)[1];
		var month = time.match(/^[^\-]+-([^-]+)/)[1];
		var day = time.match(/^[^\-]+-[^-]+-([^T]+)/)[1];
		var hour = time.match(/T([^:]+)/)[1];
		var minute = time.match(/:([^+]+)/)[1];
		
		// Create time marker for every 4th card
		if (index % 4 == 0) {
			var adjustedHour = parseInt(hour.replace(/^0/, ''));
			var adjustedMinute = '';
			
			if (minute != '00') {
				adjustedMinute = ':' + minute;
			}
			
			if (adjustedHour <= 12) {
				timeName = adjustedHour + adjustedMinute + 'am';
			}
			else {
				timeName = (adjustedHour - 12) + adjustedMinute + 'pm';
			}
			
			var timeMarker = $('<time datetime="' + time + '">' + timeName + '</time>');
			timeMarker
				.css('left', index * 100 / (roomListItems.length - 1) + '%')
				.appendTo(listItem);
		}
		
		// Create day marker for every new day
		if (currDay != day) {
			currDay = day;
			
			var date = new Date(year, parseInt(month) - 1, day, hour, minute);
			var dayName = 'Sunday';
			
			switch (date.getDay())
			{
				case 1: dayName = 'Monday'; break;
				case 2: dayName = 'Tuesday'; break;
				case 3: dayName = 'Wednesday'; break;
				case 4: dayName = 'Thursday'; break;
				case 5: dayName = 'Friday'; break;
				case 6: dayName = 'Saturday'; break;
			}
			
			var monthName = 'Jan';
			
			switch (date.getMonth())
			{
				case 1: monthName = 'Feb'; break;
				case 1: monthName = 'Mar'; break;
				case 1: monthName = 'Apr'; break;
				case 1: monthName = 'May'; break;
				case 1: monthName = 'Jun'; break;
				case 1: monthName = 'Jul'; break;
				case 1: monthName = 'Aug'; break;
				case 1: monthName = 'Sep'; break;
				case 1: monthName = 'Oct'; break;
				case 1: monthName = 'Nov'; break;
				case 1: monthName = 'Dec'; break;
			}
			
			var timeMarker = $('<time class="day" datetime="' + time + '">' + dayName + ', ' + monthName + ' ' + day + '</time>');
			timeMarker
				.css('left', index * 100 / (roomListItems.length - 1) + '%')
				.appendTo(listItem);
		}
		
		$('a', listItem)
			.css('left', index * 100 / (roomListItems.length - 1) + '%')
			.click(function() {
				moveRoomListTo($(self));
				
				return false;
			})
	});

	$('nav > div').css('maxWidth', $('li a', navOl).outerWidth() * roomListItems.length * 1.15);
	
	$('nav .prev').click(function() {
		moveRoomListTo(-$(window).width() * 0.6);
		
		return false;
	});

	$('nav .next').click(function() {
		moveRoomListTo($(window).width() * 0.6);
		
		return false;
	});
}

function moveRoomListTo(first, second) {
	if ($('#content').data('targetX') == null) {
		$('#content').data('targetX', 0);
	}
	
	var targetX = $('#content').data('targetX');
	
	if (typeof first == 'number') {
		targetX -= first;
	}
	else {
		if (typeof second == 'undefined') {
			second = first;
		}
		
		var firstX = first.offset().left + -$('#content').offset().left;
		var secondX = second.offset().left + second.outerWidth() + -$('#content').offset().left;
		
		if (secondX - firstX > $(window).width()) {
			targetX = -firstX + (first.outerWidth() - first.width());
		}
		else {
			targetX = -(firstX - ($(window).width() - (secondX - firstX)) / 2);
		}
	}
	
	if (targetX > 0) {
		targetX = 0;
	}
	else if (targetX < -($('#content').outerWidth() - $(window).width())) {
		targetX = -($('#content').outerWidth() - $(window).width());
	}

	if (typeof first != 'number' && first == second) {
		var flashTimeout = 1000;

		if (targetX == $('#content').data('targetX')) {
			flashTimeout = 0;
		}
				
		// Flash card when animation finishes
		setTimeout(function() {
			first.addClass('hilite');
			
			setTimeout(function() {
				first.removeClass('hilite');
			}, 2000);
		}, flashTimeout);
	}

	if (TRANSLATE3D) {
		$('#content').css('-webkit-transform', 'translate3d(' + targetX + 'px,0px,0px)');
	}
	else {
		$('#content').css('left', targetX);
	}
	
	$('#content').data('targetX', targetX);

	// Position nav indicator correctly
	var navIndicator = $('#navIndicator');
	var indicatorWidth = $(window).width() / $('#content').outerWidth();
	
	if (indicatorWidth >= 1) {
		$('nav').addClass('noIndicator');
	}
	else {
		$('nav').removeClass('noIndicator');
		
		navIndicator
			.css('width', indicatorWidth * ($('nav > div').width() + $('nav ol a').width()) - 2)
		
		// Account for the extra padding that the navIndicator needs to have at either end of the nav area
		var totalContentMovement = $('#content').outerWidth() - $(window).width();
		var extra = 0;
		
		if (-targetX > totalContentMovement / 2) {
			extra = 0;
		}
		else {
			extra = 0;
		}

		if (TRANSLATE3D) {
			$('#content').css('-webkit-transform', 'translate3d(' + targetX + 'px,0px,0px)');
			navIndicator
				.css('-webkit-transform', 'translate3d(' + (-targetX / totalContentMovement * ($('nav > div').width() - navIndicator.width() + $('nav ol a').width() / 2 + 6 + 7) - 7) + 'px,0px,0px)')
		}
		else {
			navIndicator
				.css('left', -targetX / totalContentMovement * ($('nav > div').width() - navIndicator.width() + $('nav ol a').width() / 2 + 6 + 7) - 7)
		}
	}
}