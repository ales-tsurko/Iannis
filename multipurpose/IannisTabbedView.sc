IannisTabbedView : CompositeView {
  classvar <>isScrollable = true;
  var currentIndex, currentButton, previousButton, <tabsContainer, tabs, <views, stackLayout;

  *new {arg initialName, initialView;
    ^super.new.init(initialName, initialView);
  }

  init {arg initName, initView;
    tabs = [];
    currentIndex = 0;

    this.initTabsContainer();

    stackLayout = StackLayout();

    this.layout = VLayout(
      tabsContainer,
      stackLayout
    );

    this.addPage(initName, initView);
  }

  initTabsContainer {
    tabsContainer = ScrollView();
    tabsContainer.fixedHeight = 60;
    tabsContainer.hasBorder = false;
    tabsContainer.hasVerticalScroller = false;
    tabsContainer.canvas = CompositeView();
    tabsContainer.canvas.layout = HLayout(nil);
  }

  addPage {arg name, view;
    var button = CompositeView();
    var buttonLabel = StaticText();

    button.fixedHeight = 40;
    button.fixedWidth = 110;
    button.background = Color.gray(0.75);
    buttonLabel.string = name;
    button.layout = HLayout(nil, buttonLabel, nil);

    tabs = tabs.add(button);
    currentButton??{currentButton = button};
    previousButton??{previousButton = button};

    button.mouseDownAction = {arg but;
      currentButton = but;
      this.switchPage(this.currentIndex(but));
      previousButton = but;
    };

    // add content view
    if (IannisTabbedView.isScrollable) {
      var content = ScrollView();
      content.canvas = view;
      content.hasBorder = false;
      views = views.add(content);
      stackLayout.add(content);
    } {
      views = views.add(view);
      stackLayout.add(view);
    };

    tabsContainer.canvas.layout.insert(button, tabs.size-1);

    this.switchPage(currentIndex);
  }

  currentIndex {arg button;
    currentIndex = tabs.detectIndex(_ == button);

    ^currentIndex;
  }

  switchPage {arg index;
    stackLayout.index = index;
    this.highlightCurrentButton();
  }

  highlightCurrentButton {
    previousButton.background = Color.gray(0.75);
    currentButton.background = Color.gray(0.6);
  }
}
