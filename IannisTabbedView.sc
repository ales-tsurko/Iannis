IannisTabbedView : CompositeView {
  var currentIndex, <tabsContainer, tabs, <views, stackLayout;

  *new {arg initialName, initialView;
    ^super.new.init(initialName, initialView);
  }

  init {arg initName, initView;
    tabs = [];
    currentIndex = 0;

    tabsContainer = ScrollView();
    tabsContainer.fixedHeight = 40;
    tabsContainer.hasBorder = false;
    tabsContainer.hasVerticalScroller = false;
    tabsContainer.canvas = CompositeView();
    tabsContainer.canvas.layout = HLayout();

    stackLayout = StackLayout();

    this.layout = VLayout(
      tabsContainer,
      stackLayout
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

    tabsContainer.canvas.layout.add(button);
    stackLayout.add(content);

    this.switchPage(currentIndex);
  }

  currentIndex {arg button;
    currentIndex = tabs.detectIndex({arg item;
      item == button;
    });

    ^currentIndex;
  }

  switchPage {arg index;
    stackLayout.index = index;
  }
}
