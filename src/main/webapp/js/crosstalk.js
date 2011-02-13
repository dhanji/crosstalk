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

  // Size the dropzone on the talkbox
  var talkbox = $('#talkbox');
  $('#dropzone').css({
    left: talkbox.left,
    top: talkbox.top
  }).width(talkbox.width())
    .height(talkbox.height() - 40);

  // Set up file uploader.
  crosstalk.uploader_ = new qq.FileUploader({
    element: $('#dropzone')[0],
    action: "/r/upload",
    onComplete: function(id, file, response) {
      if (response && response.success) {
        // Remember file attachment name.
        $('#talkbox').data('attachment', file);
      }
    }
  });


  // Read current user information.
  crosstalk.currentUserInfo = {
    displayName: $('#current-user').html(),
    username: $('#current-user').html(),
    avatar: $('#current-user-avatar').html()
  };

  // Join room!
  crosstalk.send("join", { room: $('#room-id').text() }, crosstalk.noop);
};

/**
 * Posts a new local message and sends to server.
 */
crosstalk.post_ = function() {
  var talkbox = $('#talkbox');
  var text = talkbox.val();
  talkbox.val('');
  var token = $('#comet-token').html();

  // escape html:
  text = $('<div/>').text(text).html();

  var now = new Date();
  crosstalk.insertMessage_({
    author: crosstalk.currentUserInfo,
    postedOn: now.getHours() + ':' + now.getMinutes(),
    text: text
  });

  // Send to the server.
  crosstalk.send("message", { room: $('#room-id').text(), text: text, token: token },
      crosstalk.noop);
};

/**
 * Inserts message into dom and nothing else.
 */
crosstalk.insertMessage_ = function(post) {
  var linkset = crosstalk.linkify(post.text);
  $('#stream').append('<div class="message">'
    + '<div class="author">' + post.author.username + '</div>'
    + '<img class="avatar" src="' + post.author.avatar + '"/>'
    + '<div class="content">'
    + '  <div class="time">' + post.postedOn + '</div>'
    + linkset.text
    + '<div class="images"></div><div class="oembed"></div></div></div>');

  var msg = $('#stream .message:last'); 
  var target = $('.images', msg);

  for (var i = 0; i < linkset.images.length; i ++) {
    var image = linkset.images[i];
    target.append('<img style="width: 200px" src="' + image + '"/>');
  }

  target = $('.oembed', msg);
  // oEmbedify the last inserted message.
  $.embedly(linkset.links, {
    maxWidth: 400,
    wmode: 'transparent',
    elems: target
  });
};

/**
 * Transform text links into anchor hrefs.
 */
crosstalk.linkify = function(text) {
  var pieces = text.split(/[ ]+/);
  var recombine = [];
  var links = [];
  var images = [];
  for (var i = 0; i < pieces.length; i++) {
    var piece = pieces[i];
    if (piece.indexOf('http://') === 0) {

      // Images are embedded directly (without oEmbed).
      if (piece.match(/(\.jpg|\.png|\.gif)$/)) {
        images.push(piece);
      } else {
        links.push(piece);
      }
      piece = '<a target="_blank" href="' + piece + '">' + piece + '</a>';
    }
    recombine.push(piece);
    recombine.push(' ');
  }

  return {
    text: recombine.join(''),
    images: images,
    links: links
  };
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
  if (data.joiner.username == 'anonymous') {
    // Treat anonymous joiners as lurkers (they have no avatar).
    var countRef = $('#lurker-count');
    var count = parseInt(countRef.html());
    countRef.html(count + 1);
  } else {
    var id = 'av-' + data.joiner.username;

    // Do nothing if we already know about this contributor.
    if ($('#' + id).length > 0)
      return;
    $('.current-contributor-avatars')
        .append('<img id="' + id + '" src="' + data.joiner.avatar + '"/>');
  }
};