IannisSynthViewController : CompositeView {
  var <synthName, <pagesView, <node, 
  <metadata, <presetsManagerController,
  <midiInManagerController,
  <parameterBinder,
  <elements,
  <synthDefName,
  mapView,
  toolbarView, synthNameLabel;

  *new {arg node, synthDefName;
    ^super.new.init(node, synthDefName);
  }

  init {arg aNode, aSynthDefName;
    node = aNode;
    synthDefName = aSynthDefName;
    metadata = SynthDescLib.getLib(\iannis_synth)[synthDefName.asSymbol].metadata;
    parameterBinder = ();
    elements = ();
    this.fixedWidth = 680;
    this.minHeight = 550;
    this.layout = VLayout();
    this.initToolbar();
    this.parse();

    // init parameters map view
    mapView = IannisSynthMapPage(this);
    this.pagesView.addPage("Map", mapView);
  }

  initToolbar {
    toolbarView = CompositeView();
    toolbarView.fixedHeight = 90;

    synthNameLabel = StaticText();
    synthNameLabel.font = Font("Arial", 20);

    presetsManagerController = IannisPresetsManagerController(this);
    midiInManagerController = IannisMIDIInManagerController(this);

    toolbarView.layout = VLayout(
      HLayout(synthNameLabel, nil),
      HLayout(presetsManagerController, nil, midiInManagerController)
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

  didFinishParsing {
    this.presetsManagerController.parentControllerDidFinishParsing();
  }
}
