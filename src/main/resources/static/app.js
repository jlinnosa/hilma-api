
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.noticeboard = ko.observable(new NoticeboardModel());
  self.notifications = ko.observableArray();

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
  }

  self.loadModel = function() {
    $.getJSON("/notices?sort=published,desc&size=100", function(data) {
      var notices = data._embedded.notices;
      for (var i = 0; i < notices.length; i++) {
        self.noticeboard().addNotice(notices[i]);
      }
      self.noticeboard().sort();
    })
  }
}

function NoticeboardModel() {
  var self = this;
  self.rows = ko.observableArray();
  self.addNotice = function(notice) {
    self.rows.push(new NoticeRow(notice));
  };
  self.sort = function() {
    self.rows.sort(function(left, right) {
      return left.published == right.published ? 0 : (left.published > right.published ? -1 : 1)
    });
  }
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
  self.cpvExplanation = codes[data.cpv.substring(0, 2)];
  self.description = data.description;
  self.organization = data.organization;
  self.note = data.note;
};

function toMoment(input) {
  if (Object.prototype.toString.call(input) === '[object Array]') {
    return moment({
      year: input[0], month: input[1] - 1, day: input[2],
      hour: input[3], minute: input[4]
    });
  } else if (input != null) {
    return moment(0).year(input.year).month(input.month).date(input.dayOfMonth)
        .hours(input.hour).minutes(input.minute).seconds(input.second);
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