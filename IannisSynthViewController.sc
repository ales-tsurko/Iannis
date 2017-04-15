IannisSynthViewController : CompositeView {
  var <synthName, <pages, tabsButtons;

  *new {
    ^super.new.init();
  }

  init {
    // должен быть тулбар, который будет недвижимым
    // на нем имя синтезатора, пресеты и т.п.
    // и должно быть scrollview в котором уже и будут параметры
    // кстати, переключения страниц в параметрах (табы) тоже должны
    // быть недвижимы вверху под тулбаром
  }

  addPage {arg name;
    var nameLabel = StaticText();
    var pageView = ScrollView();
    var tabButton = Button();
    var container = CompositeView();
  }

  switchPage {arg index;

  }

  synthName_ {arg newName;
  }
}
