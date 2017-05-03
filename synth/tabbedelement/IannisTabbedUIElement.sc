IannisTabbedUIElement : CompositeView {
  var tabsListView, containerView,
  <currentIndex;

  *new {arg initialName, initialView;
    ^super.new.init(initialName, initialView);
  }

  init {arg initName, initView;
    this.initTabsListView();
    this.initContainerView();

    this.layout = HLayout(
      tabsListView,
      containerView
    );

    this.addPage(initName, initView);
  }

  initTabsListView {
    tabsListView = ListView();
    tabsListView.fixedWidth = 100;
    tabsListView.selectionMode = \single;

    tabsListView.action = {arg lv;
      this.tabsListViewAction(lv);
    };
  }

  initContainerView {
    containerView = StackLayout();
  }

  addPage {arg name, view;
    var itemsToAdd = tabsListView.items.add(name);
    currentIndex = tabsListView.value?0;

    tabsListView.items = itemsToAdd;

    tabsListView.valueAction = currentIndex;

    containerView.add(view);
  }

  currentIndex_ {arg newValue;
    currentIndex = newValue;
    tabsListView.valueAction = currentIndex;
  }

  tabsListViewAction {arg listView;
    containerView.index = listView.value;
    currentIndex = listView.value;
  }

}
