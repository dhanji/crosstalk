/**
 * WriteWire by Dhanji.
 */

var crosstalk = crosstalk || {};

/**
 * Noop function.
 */
crosstalk.noop = function() {};

$(document).ready(function() {

  // Callbacks from server.
  crosstalk.callbacks = {
    'receive': crosstalk.receiveMessage_,
    'join': crosstalk.joinRoom_
  };

  // Setup Comet channel.
  var token = $('#comet-token').html();

  // Set up coment channel.
  var channel = new goog.appengine.Channel(token);
  var socket = channel.open();
  socket.onopen = function() {};
  socket.onmessage = function(data) {
    // Receive as JSON.
    data = eval('(' + data.data + ')');

    crosstalk.callbacks[data.rpc](data);
  };
  socket.onerror = crosstalk.noop;
  socket.onclose = crosstalk.noop;

  // Initialize editor.
  crosstalk.init_();

});


/**
 * Initialize UI event handlers and such.
 */
crosstalk.init_ = function () {
  $('#post-message').click(crosstalk.post_);
  $('#talkbox').keypress(function(event) {
    // If enter key pressed, post.
    if (event.which == 13) {
      crosstalk.post_();
      return false;
    }
  });

  // Join room!
  crosstalk.send("join", { text: $('#room-id').text() }, crosstalk.noop);
};

/**
 * Posts a new local message and sends to server.
 */
crosstalk.post_ = function() {
  var talkbox = $('#talkbox');
  var text = talkbox.val();
  talkbox.val('');
  var token = $('#comet-token').html();

  crosstalk.insertMessage_({
    author: { username: 'michaelneale', avatar: ''}, // TODO make this a singleton
    text: text
  });

  // Send to the server.
  crosstalk.send("message", { text: text, token: token }, crosstalk.noop);
};

/**
 * Inserts message into dom and nothing else.
 */
crosstalk.insertMessage_ = function(post) {
  $('#stream').append('<div class="message">'
    + '<div class="author">' + post.author.username + '</div>'
    + '<img src="images/swish.png" class="avatar"/>'
    + '<div class="content">'
    + '  <div class="time">' + new Date() + '</div>'
    + post.text
    + '</div></div>');
};

crosstalk.send = function(rpc, args, callback) {
  args = { data: JSON.stringify(args) };

  $.ajax({
    url: '/r/async/' + rpc,
    type: 'POST',
    dataType: 'json',
    data: args,
    success: callback,
    failure: crosstalk.noop
  });
};

function log(log) {
  $('#debug').text(log);
}


/*** SERVER RPC CALLBACKS ***/
crosstalk.receiveMessage_ = function(data) {
  crosstalk.insertMessage_(data.post);
};

crosstalk.joinRoom_ = function(data) {
  // TODO render avatar.
  $('.current-contributor-avatars').append(data.joiner.username);
};