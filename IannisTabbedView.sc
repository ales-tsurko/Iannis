IannisTabbedView : CompositeView {
  var currentIndex, tabsContainer, tabs, views;

  *new {arg initialName, initialView;
    ^super.new.init(initialName, initialView);
  }

  init {arg initName, initView;
    tabs = [];
    currentIndex = 0;

    tabsContainer = CompositeView();
    tabsContainer.fixedHeight = 30;
    tabsContainer.layout = HLayout();

    this.layout = VLayout(
      tabsContainer
    );

    this.addPage(initName, initView);
  }


  addPage {arg name, view;
    var button = Button();
    var content = ScrollView();

    button.fixedHeight = 20;
    button.fixedWidth = 110;
    button.states = [[name]];

    tabs = tabs.add(button);

    button.action = {arg button;
      if (button.value == 0) {
        this.switchPage(this.currentIndex(button));
      };
    };

    content.canvas = view;
    content.hasBorder = false;
    views = views.add(content);

    tabsContainer.layout.add(button);
    this.layout.add(content);

    this.switchPage(currentIndex);
  }

  currentIndex {arg button;
    currentIndex = tabs.detectIndex({arg item;
      item == button;
    });

    ^currentIndex;
  }

  switchPage {arg index;
    views.do({arg view, n;
      if (index == n) {
        view.visible = true;
      } {
        view.visible = false;
      };
    });
  }
}
