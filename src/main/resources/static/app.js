
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.noticeboard = ko.observable(new NoticeboardModel());

  self.toggleIgnore = function(code) {
    var ig = self.noticeboard().ignores;
    var found = false;
    for(var i = 0; i < ig.length; i++) {
        if (ig[i] == code) {
            ig.splice(i, 1);
            found = true;
            break;
        }
    }
    if (!found) {
        ig.push(code);
    }
    if (Modernizr.localstorage) {
        localStorage['ignores'] = JSON.stringify(ko.toJS(ig));
    }
  };

  self.connect = function() {
    stompClient.connect({}, function(frame) {
      stompClient.subscribe("/topic/hilma.*", function(message) {
        self.noticeboard().addNotice(JSON.parse(message.body));
        self.noticeboard().sort();
      });
      stompClient.subscribe("/user/queue/errors", function(message) {
        self.pushNotification("Error " + message.body);
      });
    }, function(error) {
      console.log("STOMP protocol error " + error);
    });
  };

  self.loadModel = function() {
    $.getJSON("/notices?sort=published,desc&size=100", function(data) {
      var notices = data._embedded.notices;
      for (var i = 0; i < notices.length; i++) {
        self.noticeboard().addNotice(notices[i]);
      }
      self.noticeboard().sort();
    })
    if (Modernizr.localstorage) {
        console.log("we have localStorage");
        var savedIgnores = localStorage['ignores'];
        if (savedIgnores != null) {
            self.noticeboard().ignores = ko.observableArray(JSON.parse(savedIgnores));
        }
    }
  };

  self.toggleRowVisibility = function(row) {
    self.noticeboard().toggleCpvVisibility(row.shortCpv);
  };
}

function NoticeboardModel() {
  var self = this;

  self.rows = ko.observableArray();
  self.ignores = ko.observableArray();

  self.addNotice = function(notice) {
    self.rows.push(new NoticeRow(notice, self.ignores));
  };

  self.sort = function() {
    self.rows.sort(function(left, right) {
      return left.published == right.published ? 0 : (left.published > right.published ? -1 : 1)
    });
  };

  self.toggleCpvVisibility = function(code) {
    var ig = self.ignores();
    var found = false;
    console.log(code);
    console.log(ig);
    for(var i = 0; i < self.ignores().length; i++) {
        if (ig[i] === code) {
            self.ignores().splice(i, 1);
            found = true;
            console.log("found, removed");
            break;
        }
    }
    if (!found) {
        self.ignores().push(code);
        console.log("didn't find and hid ");
    }
    if (Modernizr.localstorage) {
        localStorage['ignores'] = JSON.stringify(ko.toJS(self.ignores()));
    }
  };

};

function NoticeRow(data, ignores) {
  var self = this;
  self.id = data.id;
  self.ignores = ignores;
  self.name = data.name;
  self.link = data.link;
  self.type = data.type;
  self.published = toMoment(data.published);
  self.closes = toMoment(data.closes);
  self.cpv = data.cpv;
  self.shortCpv = data.cpv.substring(0, 2);
  self.cpvExplanation = codes[self.shortCpv];
  self.description = data.description;
  self.organization = data.organization;
  self.note = data.note;
  self.visible = ko.computed(function() {
    var ig = self.ignores();
    for (var i = 0; i < ig.length; i++) {
      if (ig[i] === self.shortCpv) {
        return false;
      }
    }
    return true;
  });
};

function toMoment(input) {
  if (Object.prototype.toString.call(input) === '[object Array]') {
    return moment({
      year: input[0], month: input[1] - 1, day: input[2],
      hour: input[3], minute: input[4]
    });
  } else if (input != null) {
    return moment(input);
  } else {
    return moment("not a real date");
  }
}

var codes = {
    '03': 'Maa-, karja-, kala- ja metsätaloustuotteet sekä vastaavat tuotteet.',
    '09': 'Öljytuotteet, polttoaineet, sähkö ja muut energian lähteet.',
    '14': 'Kaivostuotteet, perusmetallit ja vastaavat tuotteet.',
    '15': 'Elintarvikkeet, juomat, tupakka ja vastaavat tuotteet.',
    '16': 'Maatalouskoneet.',
    '18': 'Vaatteet, jalkineet, matkalaukkutuotteet ja tarvikkeet.',
    '19': 'Nahka ja tekstiilikankaat, muovi- ja kumimateriaalit.',
    '22': 'Painotuotteet ja vastaavat tuotteet.',
    '24': 'Kemialliset tuotteet.',
    '30': 'Toimisto- ja atk-laitteet ja -tarvikkeet, lukuun ottamatta kalusteita ja tietokoneohjelmatuotteita.',
    '31': 'Sähkökoneet, -kojeet, -laitteet ja -kulutustarvikkeet; valaistus.',
    '32': 'Radio-, televisio- ja viestintä- ja teleliikennelaitteet ja niihin liittyvät laitteet.',
    '33': 'Lääketieteelliset laitteet, farmaseuttiset valmisteet ja henkilökohtaiseen hygieniaan liittyvät tuotteet.',
    '34': 'Kuljetusvälineet ja kuljetuksessa käytettävät aputuotteet.',
    '35': 'Turva-, palontorjunta-, poliisi- ja maanpuolustusvälineet.',
    '37': 'Soittimet, urheiluvälineet, pelit, lelut, käsityö- ja taidetarvikkeet ja -varusteet.',
    '38': 'Laboratoriolaitteet, optiset ja tarkkuuslaitteet (lukuun ottamatta silmälaseja).',
    '39': 'Huonekalut (myös toimistokalusteet), sisustustavarat, kodinkoneet ja laitteet (valaistusta lukuun ottamatta) ja puhdistustuotteet.',
    '41': 'Puhdistettu vesi.',
    '42': 'Teollisuuskoneet.',
    '43': 'Kaivos- ja louhintakoneet, rakennuslaitteistot.',
    '44': 'Rakentamiseen liittyvät rakenteet ja tarvikkeet ja muut tuotteet (sähkölaitteita lukuun ottamatta).',
    '45': 'Rakennustyöt.',
    '48': 'Ohjelmatuotteet ja tietojärjestelmät.',
    '50': 'Korjaus- ja huoltopalvelut.',
    '51': 'Asennuspalvelut (ohjelmistoja lukuun ottamatta).',
    '55': 'Hotelli-, ravintola- ja vähittäiskauppapalvelut',
    '60': 'Kuljetuspalvelut (jätteen kuljetusta lukuun ottamatta).',
    '63': 'Kuljetusten tuki- ja apupalvelut; matkatoimistopalvelut.',
    '64': 'Posti- ja televiestintäpalvelut.',
    '65': 'Julkinen vesi- ja energiahuolto.',
    '66': 'Rahoitus- ja vakuutuspalvelut.',
    '70': 'Kiinteistöpalvelut.',
    '71': 'Arkkitehti-, rakennus-, insinööri- ja tarkastuspalvelut.',
    '72': 'Tietotekniset palvelut: neuvonta, ohjelmistojen kehittäminen, Internet ja tuki.',
    '73': 'Tutkimus- ja kehityspalvelut ja niihin liittyvät konsulttipalvelut.',
    '75': 'Julkishallinnon palvelut, maanpuolustus ja sosiaaliturvapalvelut.',
    '76': 'Öljy- ja kaasuteollisuuteen liittyvät palvelut.',
    '77': 'Maatalous-, metsätalous-, puutarha-, vesiviljely- ja mehiläistalousalan palvelut.',
    '79': 'Liike-elämän palvelut: laki, markkinointi, neuvonta, työhönotto, painatus ja turvallisuus.',
    '80': 'Yleissivistävän ja ammatillisen koulutuksen palvelut.',
    '85': 'Terveyspalvelut ja sosiaalitoimen palvelut.',
    '90': 'Viemäröinti-, jätehuolto- ja puhdistuspalvelut, ympäristöön liittyvät palvelut.',
    '92': 'Virkistys-, kulttuuri- ja urheilupalvelut.',
    '98': 'Muut yhteisöön liittyvät, yhteiskunnalliset ja henkilökohtaiset palvelut.'
};