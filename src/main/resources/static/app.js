
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.noticeboard = ko.observable(new NoticeboardModel());
  self.notifications = ko.observableArray();

  self.connect = function() {
    stompClient.connect({}, function(frame) {
      stompClient.subscribe("/topic/hilma.*", function(message) {
        self.noticeboard().addNotice(JSON.parse(message.body));
      });
      stompClient.subscribe("/user/queue/errors", function(message) {
        self.pushNotification("Error " + message.body);
      });
    }, function(error) {
      console.log("STOMP protocol error " + error);
    });
  }

  self.loadModel = function() {
    $.getJSON("/notices?sort=published,desc&size=100", function(data) {
      var notices = data._embedded.notices;
      for (var i = 0; i < notices.length; i++) {
        self.noticeboard().addNotice(notices[i]);
      }
    })
  }

  self.pushNotification = function(text) {
    self.notifications.push({notification: text});
    if (self.notifications().length > 5) {
      self.notifications.shift();
    }
  }
}

function NoticeboardModel() {
  var self = this;
  self.rows = ko.observableArray();
  self.addNotice = function(notice) {
    self.rows.push(new NoticeRow(notice));
  };
};

function NoticeRow(data) {
  var self = this;
  self.id = data.id;
  self.name = data.name;
  self.link = data.link;
  self.type = data.type;
  self.published = toMoment(data.published);
  self.closes = toMoment(data.closes);
  self.cpv = data.cpv;
  self.description = data.description;
  self.organization = data.organization;
  self.note = data.note;
};

function toMoment(input) {
  if (Object.prototype.toString.call(input) === '[object Array]') {
    return moment({
      year: input[0], month: input[1], day: input[2],
      hour: input[3], minute: input[4]
    });
  } else {
    return moment(input)
  }
}