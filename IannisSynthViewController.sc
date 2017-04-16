IannisSynthViewController : CompositeView {
  var <synthName, <pagesView, <node, <metadata, 
  toolbarView, synthNameLabel;

  *new {arg node, metadata;
    ^super.new.init(node, metadata);
  }

  init {arg aNode, metad;
    node = aNode;
    metadata = metad;
    this.fixedWidth = 680;
    this.layout = VLayout();
    this.initToolbar();
  }

  initToolbar {
    synthNameLabel = StaticText();
    synthNameLabel.font = Font("Arial", 20);

    toolbarView = CompositeView();
    toolbarView.fixedHeight = 40;

    toolbarView.layout = HLayout(
      synthNameLabel, nil
    );

    this.layout.add(toolbarView);
  }

  initPagesView {arg name;
    var view = CompositeView();
    view.layout = VLayout(nil);
    pagesView = IannisTabbedView(name, view);

    this.layout.add(pagesView);
  }

  addPage {arg name;
    if (pagesView.isNil) {
      this.initPagesView(name)
    } {
      var view = CompositeView();
      view.layout = VLayout(nil);
      this.pagesView.addPage(name, view);
    };
  }

  getPageViewAtIndex {arg index; 
    ^this.pagesView.views[index].canvas;
  }

  addGroupViewToPageAtIndex {arg groupView, index;
    var newPageView = this.getPageViewAtIndex(index);
    var insertIndex = newPageView.children.size;
    newPageView.layout.insert(groupView, insertIndex);
  }

  switchPage {arg index;
    this.pagesView.switchPage(index);
  }

  synthName_ {arg newName;
    synthName = newName;
    synthNameLabel.string = newName;
  }
}
