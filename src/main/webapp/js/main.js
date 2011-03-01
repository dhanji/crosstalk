
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
		$('#roomList > li')
			.removeClass('active')
			.removeClass('future')
			.addClass('inactive')
		
		var APPEAR_TIME = 1000;
	
		var roomListItems = $('#roomList > li > .anchor');
		
		// Add click handlers to act as anchors for each card
		roomListItems.click(function() {
			window.location = $('a', this).get(0).href;
			
			return false;
		});
	
		// Truncate text in room titles	
		$('> a', roomListItems).each(function() {
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
		
    // Setup switcher
    $('#switcher').click(switchContent)

	}
	else {
		var roomListItems = $('#roomList > li > .anchor');

		// Add click handlers to act as anchors for each card
		roomListItems.click(function() {
			window.location = $('a', this).get(0).href;
			
			return false;
		});
	}
}

function switchContent() {
  var ul = $(this);

  if ($('li:first-child', ul).hasClass('on')) {
    $('li', ul).removeClass('on');
    $('li:last-child', ul).addClass('on');
    $('#roomContent').removeClass('on');
    setTimeout(initTopics, 650);
  }
  else {
    $('li', ul).removeClass('on');
    $('li:first-child', ul).addClass('on');
    $('#roomContent').addClass('on');
    $('#topicContent').removeClass('on');
  }

  return false;
}

var WORD_LETTERS = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '];

var topicData = [
	{
		title: "HTML5",
		rank: 1
	},
	{
		title: "Gmail",
		rank: 2
	},
	{
		title: "Docs",
		rank: 2
	},
	{
		title: "Blogger",
		rank: 3
	},
	{
		title: "Hubs",
		rank: 3
	},
	{
		title: "JAPAC",
		rank: 3
	},
	{
		title: "Neon Indian",
		rank: 4
	},
	{
		title: "Eng Summit",
		rank: 4
	},
	{
		title: "Google Maps",
		rank: 4
	},
	{
		title: "MacBook Pro",
		rank: 4
	},
	{
		title: "Alan Eustace",
		rank: 4
	},
	{
		title: "Short",
		rank: 4
	},
	{
		title: "Text",
		rank: 4
	},
	{
		title: "Clock",
		rank: 4
	}
];

var snippetTimer = null;
var callToActionTimer = null;

function initTopics() {
  clearTopics();
  $.ajax({
    url: '/r/async/topics',
    type: 'POST',
    data: {},
    success: buildTopicCloud,
    failure: function() {
      alert("Error: could not contact server.")
    }
  });
	setTimeout(fetchSnippet, 2000);

  $('#topicContent .callToAction').removeClass('off');

	// Fade out the call to action after 20 seconds (so that it doesn't stay on-screen while projecting)
	callToActionTimer = setTimeout(function() {
	  $('#topicContent .callToAction').addClass('off');
	}, 20000);

  $('#topicContent').addClass('on')
}

function clearTopics() {
  clearTimeout(snippetTimer);
  clearTimeout(callToActionTimer);

  $('#topicList').html('');
  $('#topicContent .snippet').remove();
}

function buildTopicCloud(data) {
	var topicList = $('#topicList');
	var positioned = [];

  // Loaded from server.
  topicData = eval('(' + data + ')');

	// For each topic, position it so it doesn't overlap any others, but is close to the center
	for (var i = 0; i < topicData.length; i++) {
		var newTopic = $('<li class="rank' + topicData[i].rank + '"><a href="/r/chat/'
        + topicData[i].room + '">' + topicData[i].title + '</a></li>');

		newTopic.appendTo(topicList);

		var x = topicList.outerWidth() / 2 - newTopic.outerWidth() / 2;
		var y = topicList.outerHeight() / 2 - newTopic.outerHeight() / 2;


		var angle = Math.PI * 2 / topicData.length * i;
		var minSteps = null;
		var minX = x;
		var minY = y;
		var RETRIES = 3;

		// Try x = RETRIES different angles and see which allows the topic to be closest to the center
		for (var k = 0; k < RETRIES; k++) {
			newTopic
				.css('left', x)
				.css('top', y);

			var numSteps = 0;
			var newX = null;
			var newY = null;

			// While the topic is still intersecting with an existing topic
			while (true) {
				numSteps++;

				var intersects = false;

				// Check to see whether the topic is intersecting with any of the existing topics, if not: finish the while(true) loop
				for (var j = 0; j < positioned.length; j++) {
					if (elementsIntersect(positioned[j], newTopic)) {
						intersects = true;

						break;
					}
				}

				if (!intersects) {
					break;
				}
				else {
					if (newX == null) {
						newX = x;
						newY = y;
					}

					newX += Math.cos(angle + Math.PI * 2 / RETRIES * k) * 10;
					newY += Math.sin(angle + Math.PI * 2 / RETRIES * k) * 10;

					newTopic
						.css('left', newX)
						.css('top', newY)
				}
			}

			if ((minSteps == null || numSteps < minSteps) && newX != null) {
				minSteps = numSteps;
				minX = newX;
				minY = newY;
			}
		}

		newTopic
			.css('left', minX)
			.css('top', minY)

		positioned.push(newTopic);
	}
}

function elementsIntersect(el1, el2) {
	var MARGIN = 13;
	var el1Offset = el1.offset();
	var el1x1 = el1Offset.left - MARGIN;
	var el1x2 = el1x1 + el1.outerWidth() + MARGIN * 2;
	var el1y1 = el1Offset.top - MARGIN;
	var el1y2 = el1y1 + el1.outerHeight() + MARGIN * 2;

	var el2Offset = el2.offset();
	var el2x1 = el2Offset.left;
	var el2x2 = el2x1 + el2.outerWidth();
	var el2y1 = el2Offset.top;
	var el2y2 = el2y1 + el2.outerHeight();

	// If overlapping on x axis
	if (((el1x1 >= el2x1 && el1x1 <= el2x2) || (el2x1 >= el1x1 && el2x1 <= el1x2))) {
		// If overlapping on y axis
		if (((el1y1 >= el2y1 && el1y1 <= el2y2) || (el2y1 >= el1y1 && el2y1 <= el1y2))) {
			return true;
		}
	}

	return false;
}

function fetchSnippet() {
  $.ajax({
    url: '/r/async/random_msg',
    type: 'POST',
    data: {},
    success: showSnippets,
    failure: function() {
      alert("Error: could not contact server.")
    }
  });
}

function showSnippets(data) {
	var topicListItems = $('#topicList li');
	var item = topicListItems.eq(Math.floor(Math.random() * topicListItems.length));

	topicListItems.removeClass('on');
	item.addClass('on');

  // Rooms are empty. Reset Timer and leave
  data = eval('(' + data + ')');
  if (!data.text) {
    snippetTimer = setTimeout(fetchSnippet, 2000);
    return;
  }

	var randomString = data.text;
	var author = data.author.displayName;

	$('.snippet').each(function() {
		if ($(this).css('opacity') == '0') {
			$(this).remove();
		}
	});

	$('.snippet').css('opacity', 0);

	var newSnippet = $('<div class="snippet"><img class="cloud" src="images/cloud.png" alt="" /><span class="cloudPointer1"></span><span class="cloudPointer2"></span><div class="content"><img class="avatar" src="images/avatar.png" width="48" height="48" alt="Avatar" /> <strong>' + author + '</strong> <span class="text">' + randomString + '</span></div>');
	newSnippet.appendTo('#topicContent');

	newSnippet
		.css('left', parseInt(item.css('left')) - newSnippet.outerWidth() / 2)
		.css('top', parseInt(item.css('top')) - newSnippet.outerHeight() - 75)

  var MIN_LEFT = 40;

  // Snippet is off left side of screen
	if (parseInt(newSnippet.css('left')) < MIN_LEFT) {
	  newSnippet.css('left', parseInt(newSnippet.css('left')) + (MIN_LEFT - parseInt(newSnippet.css('left'))));
	}

  var MIN_RIGHT = 30;

  // Snippet is off right side of screen
	if (parseInt(newSnippet.css('left')) + newSnippet.outerWidth() > $(window).width() - MIN_RIGHT) {
	  newSnippet.css('left', $(window).width() - newSnippet.outerWidth() - MIN_RIGHT);
	}

	// Snippet is off top of screen
	if (parseInt(newSnippet.css('top')) < 20) {
	  newSnippet.addClass('flipped');

	  newSnippet.css('left', parseInt(item.css('left')) + item.outerWidth() - newSnippet.outerWidth() / 2);
	  newSnippet.css('top', parseInt(item.css('top')) + item.outerHeight() + 75);
	}

	$('.cloudPointer1', newSnippet).css('opacity', 1);

	setTimeout(function() {
  	$('.cloudPointer2', newSnippet).css('opacity', 1);
	}, 700)

	setTimeout(function() {
  	$('.cloud', newSnippet).css('opacity', 1);
	}, 1400)

	setTimeout(function() {
  	$('.content', newSnippet).css('opacity', 1);
	}, 1800)

	setTimeout(function() {
  	$('.content .text span', newSnippet).each(function() {
  	  var self = this;
  	  setTimeout(function() {
  	    $(self).css('opacity', 1);
  	  }, Math.random() * 1000);
  	});
	}, 2100)

	snippetTimer = setTimeout(fetchSnippet, 8000);
}